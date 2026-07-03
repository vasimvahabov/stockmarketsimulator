package com.vasimvahabov.stockmarketsimulator.synchorizer;

import com.vasimvahabov.stockmarketsimulator.constant.Timeframe;
import com.vasimvahabov.stockmarketsimulator.entity.Candle;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.service.CandleService;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.CandleSynchronizerProps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class CandleSynchronizer implements ApplicationRunner {

    @Qualifier("candleScheduledExecutor")
    ScheduledExecutorService scheduledExecutor;

    @Qualifier("candleExecutor")
    ExecutorService executor;

    CandleService candleService;

    QuoteService quoteService;

    CandleSynchronizerProps syncProps;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long initialDelay = syncProps.getScheduled().initialDelay() != null
                ? syncProps.getScheduled().initialDelay() : millisUntilMidnightUTC();
        scheduledExecutor.schedule(
                this::startDailySynchronization,
                initialDelay,
                syncProps.getScheduled().unit()
        );
    }

    private void startDailySynchronization() {
        try {
            Instant yesterday = yesterday();
            Map<Stock, List<Quote>> quotesSinceYesterday = quoteService.retrieveQuotesAsMapSinceTimestampMs(yesterday);
            List<CompletableFuture<Optional<Candle>>> candleFutures = quotesSinceYesterday
                    .entrySet()
                    .stream()
                    .map(quoteEntry ->
                            CompletableFuture.supplyAsync(
                                    () -> candleService.buildCandle(
                                            quoteEntry.getKey(), quoteEntry.getValue(), Timeframe.DAILY
                                    ), executor)
                    ).toList();

            CompletableFuture.allOf(candleFutures.toArray(new CompletableFuture[0]))
                    .thenRunAsync(
                            () -> candleService.createCandles(
                                    candleFutures.stream()
                                            .map(CompletableFuture::join)
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .toList()
                            ), executor
                    ).whenComplete((_, throwable) -> {
                        if (throwable != null) {
                            log.error("Exception on collecting Candles: {}", throwable.getMessage(), throwable);
                        }
                    });
        } catch (Exception exception) {
            log.error("Exception on starting daily synchronization: {}", exception.getMessage(), exception);
        }
    }

}
