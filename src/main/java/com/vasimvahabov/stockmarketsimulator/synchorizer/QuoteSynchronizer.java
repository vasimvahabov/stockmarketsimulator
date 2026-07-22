package com.vasimvahabov.stockmarketsimulator.synchorizer;

import com.vasimvahabov.stockmarketsimulator.config.FinnhubProps;
import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps;
import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.QuotePublishCheckpoint;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.service.QuotePublishCheckpointService;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.QuoteSynchronizerProps;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import com.vasimvahabov.stockmarketsimulator.synchorizer.websocket.QuoteWSHandler;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils.*;
import static com.vasimvahabov.stockmarketsimulator.util.CompletableFutureUtils.*;

@Slf4j
@Component
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

    QuotePublishCheckpointService checkpointService;

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

        List<Stock> stocks = stockService.findStocksList();
        IntStream.iterate(0, i -> i < stocks.size(), i -> i + wsSyncProps.batchSize()).forEach(start -> {
            int end = Math.min(start + wsSyncProps.batchSize(), stocks.size());
            int batchIndex = start / wsSyncProps.batchSize();
            long delay = batchIndex * wsSyncProps.batchDelay();

            List<Stock> batchStocks = stocks.subList(start, end);
            Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, wsSyncProps.batchUnit(), scheduledExecutor);

            CompletableFuture.runAsync(
                    () -> subscribeToQuotes(batchStocks, wsFinnhubProps, wsSyncProps),
                    delayedExecutor
            ).whenComplete(logFailure(log, "Failed to process batch"));
        });
    }

    private void subscribeToQuotes(List<Stock> batchStocks,
                                   FinnhubProps.WebSocket finnhubWSProps,
                                   QuoteSynchronizerProps.WebSocket wsSyncProps) {
        CompletableFuture<Void> sessionCloseFuture = new CompletableFuture<>();
        List<String> batchSymbols = batchStocks.stream().map(Stock::getSymbol).toList();

        BlockingQueue<ProducerRecord<String, QuoteWSResponse>> producerRecords = new LinkedBlockingQueue<>(10_000);

        QuoteWSHandler wsHandler = QuoteWSHandler.builder()
                .kafkaTopic(kafkaProps.getTopics().quotesRaw().name())
                .batchSymbols(batchSymbols)
                .objectMapper(objectMapper)
                .producerRecords(producerRecords)
                .sessionCloseFuture(sessionCloseFuture)
                .build();

        CompletableFuture<WebSocketSession> sessionFuture = wsClient
                .execute(wsHandler, buildWebSocketUri(finnhubWSProps))
                .orTimeout(finnhubWSProps.timeout(), finnhubWSProps.timeoutUnit());

        sessionFuture.whenComplete(logFailureAndCompleteExceptionally(
                log, "Failed to connect to Finnhub WebSocket server", sessionCloseFuture
        ));

        sessionFuture.thenAcceptAsync(
                wsSession -> closeSession(wsSession, sessionCloseFuture),
                CompletableFuture.delayedExecutor(
                        wsSyncProps.sessionDuration(),
                        wsSyncProps.sessionUnit(),
                        scheduledExecutor
                )
        ).whenComplete(logFailureAndCompleteExceptionally(
                log, "Failed to schedule close session", sessionCloseFuture
        ));

        long closeTimeoutMs = wsSyncProps.sessionDurationInMillis() + wsSyncProps.closeGracePeriodInMillis();
        sessionCloseFuture
                .orTimeout(closeTimeoutMs, TimeUnit.MILLISECONDS)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.info("Finnhub WebSocket session aborted due to an upstream failure : {}",
                                throwable.getMessage(), throwable);
                        return;
                    }
                    List<CompletableFuture<?>> futures = producerRecords.stream()
                            .map(kafkaTemplate::send)
                            .collect(Collectors.toUnmodifiableList());

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    checkpointService.saveQuotePublishCheckpoint(
                            QuotePublishCheckpoint
                                    .builder()
                                    .source("finnhub")
                                    .lastPublishedStockId(batchStocks.getLast().getId())
                                    .lastPublishedAt(Instant.now()).build()
                    );
                    log.info("Finnhub WebSocket connection closed gracefully");
                });
    }

    private String buildWebSocketUri(FinnhubProps.WebSocket wsProps) {
        return UriComponentsBuilder
                .fromUriString(wsProps.uri())
                .queryParam(wsProps.authQueryParam(), finnhubProps.getApiKey())
                .toUriString();
    }

    private void closeSession(WebSocketSession wsSession, CompletableFuture<Void> sessionCloseFuture) {
        try {
            wsSession.close(CloseStatus.GOING_AWAY.withReason("Client disconnected from quote updates"));
        } catch (Exception exception) {
            sessionCloseFuture.completeExceptionally(exception);
            log.error("Failed to close WebSocket session {}", exception.getMessage(), exception);
        }
    }

}
