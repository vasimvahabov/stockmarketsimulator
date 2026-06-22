package com.vasimvahabov.stockmarketsimulator.synchorizer;

import com.vasimvahabov.stockmarketsimulator.config.FinnhubProps;
import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.QuoteSynchronizerProps;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import com.vasimvahabov.stockmarketsimulator.ws.handlers.QuoteWSHandler;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils.toMillis;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteSynchronizer implements ApplicationRunner {

    FinnhubProps finnhubProps;

    ObjectMapper objectMapper;

    StockService stockService;

    QuoteService quoteService;

    QuoteSynchronizerProps syncProps;

    @Qualifier("quoteWSClient")
    WebSocketClient wsClient;

    @Qualifier("quoteExecutor")
    ExecutorService executor;

    @Qualifier("quoteScheduledExecutor")
    ScheduledExecutorService scheduledExecutor;

    @Override
    public void run(@Nonnull ApplicationArguments args) throws Exception {
        QuoteSynchronizerProps.WebSocket wsSyncProps = syncProps.getScheduled().webSocket();
        long initialDelayMillis = toMillis(wsSyncProps.initialDelay(), wsSyncProps.unit());
        Executor delayedExecutor = CompletableFuture.delayedExecutor(initialDelayMillis, TimeUnit.MILLISECONDS, scheduledExecutor);
        CompletableFuture.runAsync(() -> startSynchronization(wsSyncProps), delayedExecutor)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error(
                                "Failed to schedule Quote synchronizer executor [delay={}ms, timeunit={}]: {}",
                                initialDelayMillis,
                                TimeUnit.MILLISECONDS,
                                throwable.getMessage(),
                                throwable
                        );
                    } else {
                        log.info(
                                "Scheduled Quote synchronizer executor [delay={}ms, timeunit={}]",
                                initialDelayMillis,
                                TimeUnit.MILLISECONDS
                        );
                    }
                });
    }

    private void startSynchronization(QuoteSynchronizerProps.WebSocket wsSyncProps) {
        FinnhubProps.WebSocket wsFinnhubProps = finnhubProps.getWebsocket();

        Map<String, Stock> stocksMap = stockService.retrieveStocksAsMap();
        List<String> symbols = List.copyOf(stocksMap.keySet());
        IntStream.iterate(0, i -> i < symbols.size(), i -> i + wsSyncProps.batchSize()).forEach(start -> {
            int end = Math.min(start + wsSyncProps.batchSize(), symbols.size());
            long delay = (start / wsSyncProps.batchSize()) * wsSyncProps.batchDelay();

            List<String> batchSymbols = symbols.subList(start, end);
            Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, wsSyncProps.batchUnit(), scheduledExecutor);

            CompletableFuture.runAsync(
                    () -> subscribeToQuotes(batchSymbols, stocksMap, wsFinnhubProps, wsSyncProps),
                    delayedExecutor
            ).whenComplete((_, throwable) -> {
                if (throwable != null) {
                    log.error(
                            "Failed to process batch: {}",
                            throwable.getMessage(),
                            throwable
                    );
                }
            });
        });
    }

    private void subscribeToQuotes(List<String> batchSymbols,
                                   Map<String, Stock> stocksMap,
                                   FinnhubProps.WebSocket finnhubWSProps,
                                   QuoteSynchronizerProps.WebSocket wsSyncProps) {
        Queue<QuoteWSResponse> wsResponses = new ConcurrentLinkedQueue<>();
        CompletableFuture<Void> connectionCloseFuture = new CompletableFuture<>();

        CompletableFuture<WebSocketSession> sessionFuture = wsClient.execute(
                new QuoteWSHandler(objectMapper, batchSymbols, wsResponses, connectionCloseFuture),
                buildWebSocketUri(finnhubWSProps)
        ).orTimeout(finnhubWSProps.timeout(), finnhubWSProps.timeoutUnit());

        sessionFuture.whenComplete((_, throwable) -> {
            if (throwable != null) {
                log.error(
                        "Failed to connect to Finnhub WebSocket server: {}",
                        throwable.getMessage(),
                        throwable
                );
                connectionCloseFuture.completeExceptionally(throwable);
            }
        });

        sessionFuture.thenAcceptAsync(
                wsSession -> unsubscribe(wsSession, connectionCloseFuture),
                CompletableFuture.delayedExecutor(
                        wsSyncProps.sessionDuration(),
                        wsSyncProps.sessionUnit(),
                        scheduledExecutor
                )
        ).whenComplete((_, throwable) -> {
            if (throwable != null) {
                log.error("Failed to schedule unsubscribe: {}", throwable.getMessage(), throwable);
                connectionCloseFuture.completeExceptionally(throwable);
            }
        });

        long closeTimeoutMs = wsSyncProps.sessionDurationInMillis() + wsSyncProps.closeGracePeriodInMillis();
        connectionCloseFuture
                .orTimeout(closeTimeoutMs, TimeUnit.MILLISECONDS)
                .thenRunAsync(
                        () -> quoteService.createQuotes(List.copyOf(wsResponses), stocksMap), executor
                ).whenComplete((_, throwable) -> {
                    if (throwable == null) {
                        log.info("Closed Finnhub WebSocket connection gracefully");
                    } else {
                        log.error(
                                "Finnhub WebSocket session aborted due to an upstream failure: {}",
                                throwable.getMessage(),
                                throwable
                        );
                    }
                });
    }

    private String buildWebSocketUri(FinnhubProps.WebSocket wsProps) {
        return UriComponentsBuilder
                .fromUriString(wsProps.uri())
                .queryParam(wsProps.authQueryParam(), finnhubProps.getApiKey())
                .toUriString();
    }

    private void unsubscribe(WebSocketSession wsSession, CompletableFuture<Void> connectionCloseFuture) {
        try {
            wsSession.close(CloseStatus.NORMAL);
        } catch (Exception exception) {
            connectionCloseFuture.completeExceptionally(exception);
            log.error("Failed to close WebSocket session {}", exception.getMessage(), exception);
        }
    }

}
