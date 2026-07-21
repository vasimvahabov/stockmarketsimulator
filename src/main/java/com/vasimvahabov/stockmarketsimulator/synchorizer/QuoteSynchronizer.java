package com.vasimvahabov.stockmarketsimulator.synchorizer;

import com.vasimvahabov.stockmarketsimulator.config.FinnhubProps;
import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps;
import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.QuoteSynchronizerProps;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import com.vasimvahabov.stockmarketsimulator.synchorizer.websocket.QuoteWSHandler;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils.*;
import static com.vasimvahabov.stockmarketsimulator.util.CompletableFutureUtils.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteSynchronizer implements ApplicationRunner {

    FinnhubProps finnhubProps;

    ObjectMapper objectMapper;

    StockService stockService;

    QuoteSynchronizerProps syncProps;

    KafkaProps kafkaProps;

    @Qualifier("quoteWSClient")
    WebSocketClient wsClient;

    @Qualifier("quoteScheduledExecutor")
    ScheduledExecutorService scheduledExecutor;

    KafkaTemplate<String, QuoteWSResponse> kafkaTemplate;

    @Override
    public void run(@Nonnull ApplicationArguments args) throws Exception {
        QuoteSynchronizerProps.WebSocket wsSyncProps = syncProps.getScheduled().webSocket();
        long initialDelayMillis = toMillis(wsSyncProps.initialDelay(), wsSyncProps.unit());
        Executor delayedExecutor = CompletableFuture.delayedExecutor(initialDelayMillis, TimeUnit.MILLISECONDS, scheduledExecutor);
        String executorDetails = "[delay=%dms, timeunit=%s]".formatted(initialDelayMillis, TimeUnit.MILLISECONDS);

        CompletableFuture.runAsync(() -> startSynchronization(wsSyncProps), delayedExecutor)
                .whenComplete(logCompletion(log,
                        () -> "Scheduled Quote synchronizer executor " + executorDetails,
                        () -> "Failed to schedule Quote synchronizer executor " + executorDetails
                ));
    }

    private void startSynchronization(QuoteSynchronizerProps.WebSocket wsSyncProps) {
        FinnhubProps.WebSocket wsFinnhubProps = finnhubProps.getWebsocket();

        Map<String, Stock> stocksMap = stockService.findStocksMap();
        List<String> symbols = List.copyOf(stocksMap.keySet());
        IntStream.iterate(0, i -> i < symbols.size(), i -> i + wsSyncProps.batchSize()).forEach(start -> {
            int end = Math.min(start + wsSyncProps.batchSize(), symbols.size());
            int batchIndex = start / wsSyncProps.batchSize();
            long delay = batchIndex * wsSyncProps.batchDelay();

            List<String> batchSymbols = symbols.subList(start, end);
            Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, wsSyncProps.batchUnit(), scheduledExecutor);

            CompletableFuture.runAsync(
                    () -> subscribeToQuotes(batchSymbols, wsFinnhubProps, wsSyncProps),
                    delayedExecutor
            ).whenComplete(logFailure(log, "Failed to process batch"));
        });
    }

    private void subscribeToQuotes(List<String> batchSymbols,
                                   FinnhubProps.WebSocket finnhubWSProps,
                                   QuoteSynchronizerProps.WebSocket wsSyncProps) {
        CompletableFuture<Void> sessionClosedFuture = new CompletableFuture<>();

        CompletableFuture<WebSocketSession> sessionFuture = wsClient
                .execute(new QuoteWSHandler(
                                objectMapper,
                                batchSymbols,
                                kafkaTemplate,
                                sessionClosedFuture,
                                kafkaProps.getTopics().quotesRaw()
                        ), buildWebSocketUri(finnhubWSProps)
                ).orTimeout(finnhubWSProps.timeout(), finnhubWSProps.timeoutUnit());

        sessionFuture.whenComplete(logFailureAndCompleteExceptionally(
                log, "Failed to connect to Finnhub WebSocket server", sessionClosedFuture
        ));

        sessionFuture.thenAcceptAsync(
                wsSession -> unsubscribe(wsSession, sessionClosedFuture),
                CompletableFuture.delayedExecutor(
                        wsSyncProps.sessionDuration(),
                        wsSyncProps.sessionUnit(),
                        scheduledExecutor
                )
        ).whenComplete(logFailureAndCompleteExceptionally(
                log, "Failed to schedule unsubscribe", sessionClosedFuture
        ));

        long closeTimeoutMs = wsSyncProps.sessionDurationInMillis() + wsSyncProps.closeGracePeriodInMillis();
        sessionClosedFuture
                .orTimeout(closeTimeoutMs, TimeUnit.MILLISECONDS)
                .whenComplete(logCompletion(
                        log,
                        "Closed Finnhub WebSocket connection gracefully",
                        "Finnhub WebSocket session aborted due to an upstream failure"
                ));
    }

    private String buildWebSocketUri(FinnhubProps.WebSocket wsProps) {
        return UriComponentsBuilder
                .fromUriString(wsProps.uri())
                .queryParam(wsProps.authQueryParam(), finnhubProps.getApiKey())
                .toUriString();
    }

    private void unsubscribe(WebSocketSession wsSession, CompletableFuture<Void> connectionCloseFuture) {
        try {
            wsSession.close(CloseStatus.GOING_AWAY.withReason("Client unsubscribed from quote updates"));
        } catch (Exception exception) {
            connectionCloseFuture.completeExceptionally(exception);
            log.error("Failed to close WebSocket session {}", exception.getMessage(), exception);
        }
    }

}
