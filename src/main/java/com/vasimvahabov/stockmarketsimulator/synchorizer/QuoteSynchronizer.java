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

import java.io.IOException;
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

    @Qualifier("quoteScheduledExecutor")
    ScheduledExecutorService executor;

    @Override
    public void run(@Nonnull ApplicationArguments args) throws Exception {
        QuoteSynchronizerProps.WebSocket wsSyncProps = syncProps.getWebSocket();
        long initialDelayMillis = toMillis(wsSyncProps.initialDelay(), wsSyncProps.unit());
        executor.schedule(
                () -> startSynchronization(wsSyncProps),
                initialDelayMillis,
                TimeUnit.MILLISECONDS
        );
        log.info(
                "Quote synchronizer executor scheduled [delay={}ms, timeunit={}]",
                initialDelayMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private void startSynchronization(QuoteSynchronizerProps.WebSocket wsSyncProps) {
        FinnhubProps.WebSocket wsFinnhubProps = finnhubProps.getWebsocket();

        Map<String, Stock> stocksMap = stockService.retrieveStocksAsMap();
        List<String> symbols = List.copyOf(stocksMap.keySet());
        IntStream.iterate(0, i -> i < symbols.size(), i -> i + wsSyncProps.batchSize()).forEach(start -> {
            int end = Math.min(start + wsSyncProps.batchSize(), symbols.size());
            long delay = (start / wsSyncProps.batchSize()) * wsSyncProps.batchDelay();

            List<String> batchSymbols = symbols.subList(start, end);
            executor.schedule(
                    () -> subscribeToQuotes(batchSymbols, stocksMap, wsFinnhubProps, wsSyncProps),
                    delay,
                    wsSyncProps.batchUnit()
            );
        });
    }

    private void subscribeToQuotes(List<String> batchSymbols,
                                   Map<String, Stock> stocksMap,
                                   FinnhubProps.WebSocket finnhubWSProps,
                                   QuoteSynchronizerProps.WebSocket wsSyncProps) {
        Queue<QuoteWSResponse> wsResponses = new ConcurrentLinkedQueue<>();

        CountDownLatch closeConnectionLatch = new CountDownLatch(1);

        CompletableFuture<WebSocketSession> sessionFuture = wsClient.execute(
                new QuoteWSHandler(objectMapper, batchSymbols, wsResponses, closeConnectionLatch),
                buildWebSocketUri(finnhubWSProps)
        );
        try {
            WebSocketSession wsSession = sessionFuture.get(finnhubWSProps.timeout(), finnhubWSProps.timeoutUnit());
            executor.schedule(() -> unsubscribe(wsSession),
                    wsSyncProps.sessionDuration(), wsSyncProps.sessionUnit());

            long closeTimeoutMs = wsSyncProps.sessionDurationInMillis() + wsSyncProps.closeGracePeriodInMillis();
            boolean isConnectionClosed = closeConnectionLatch.await(closeTimeoutMs, TimeUnit.MILLISECONDS);
            if (isConnectionClosed) {
                log.info("Closed Finnhub WebSocket connection gracefully");
            } else {
                log.warn("Timed out to gracefully close Finnhub WebSocket connection");
            }
            quoteService.create(wsResponses.stream().toList(), stocksMap);
        } catch (
                ExecutionException |
                InterruptedException |
                CancellationException |
                TimeoutException exception) {
            log.error("Failed to connect to Finnhub WebSocket server: {}",
                    exception.getMessage(), exception);
        }

    }

    private String buildWebSocketUri(FinnhubProps.WebSocket wsProps) {
        return UriComponentsBuilder
                .fromUriString(wsProps.uri())
                .queryParam(wsProps.authQueryParam(), finnhubProps.getApiKey())
                .toUriString();
    }

    private void unsubscribe(WebSocketSession wsSession) {
        try {
            wsSession.close(CloseStatus.NORMAL);
        } catch (IOException exception) {
            log.error("Failed to close WebSocket session {}", exception.getMessage(), exception);
        }
    }

}