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

    Queue<QuoteWSResponse> wsResponses = new ConcurrentLinkedQueue<>();

    @Override
    public void run(@Nonnull ApplicationArguments args) throws Exception {
        FinnhubProps.WebSocket wsPropsFinnhub = finnhubProps.getWebsocket();
        QuoteSynchronizerProps.WebSocket wsPropSync = syncProps.getWebSocket();

        Map<String, Stock> stockMap = stockService.fetchAllStocksAsMap();
        List<String> symbols = new ArrayList<>(stockMap.keySet());
        IntStream.iterate(0, i -> i < symbols.size(), i -> i + wsPropSync.batchSize()).forEach(start -> {
            int end = Math.min(start + wsPropSync.batchSize(), symbols.size());
            long delay = (start / wsPropSync.batchSize()) * wsPropSync.batchDelay();

            List<String> batchSymbols = symbols.subList(start, end);
            executor.schedule(
                    () -> connectToWebSocket(batchSymbols, stockMap, wsPropsFinnhub),
                    delay,
                    wsPropSync.batchDelayUnit()
            );
        });
    }

    private void connectToWebSocket(List<String> batchSymbols,
                                    Map<String, Stock> stocksMap,
                                    FinnhubProps.WebSocket finnhubWSProps) {
        CountDownLatch closeConnectionLatch = new CountDownLatch(1);
        QuoteSynchronizerProps.WebSocket syncPropsWs = syncProps.getWebSocket();

        CompletableFuture<WebSocketSession> sessionFuture = wsClient.execute(
                new QuoteWSHandler(objectMapper, batchSymbols, wsResponses, closeConnectionLatch),
                buildFinnhubWSUri(finnhubWSProps)
        );
        try {
            WebSocketSession wsSession = sessionFuture.get(finnhubWSProps.timeout(), finnhubWSProps.timeoutUnit());
            executor.schedule(() -> closeFinnhubWSConnection(wsSession),
                    syncPropsWs.sessionDuration(), syncPropsWs.sessionDurationUnit());

            long closeTimeoutMs = syncPropsWs.sessionDurationInMillis() + syncPropsWs.closeGracePeriodInMillis();
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

    private String buildFinnhubWSUri(FinnhubProps.WebSocket wsProps) {
        return UriComponentsBuilder
                .fromUriString(wsProps.uri())
                .queryParam(wsProps.authQueryParam(), finnhubProps.getApiKey())
                .toUriString();
    }

    private void closeFinnhubWSConnection(WebSocketSession wsSession) {
        try {
            wsSession.close(CloseStatus.NORMAL);
        } catch (IOException exception) {
            log.error("Failed to close WebSocket session {}", exception.getMessage(), exception);
        }
    }

}