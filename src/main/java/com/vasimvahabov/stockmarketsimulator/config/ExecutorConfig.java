package com.vasimvahabov.stockmarketsimulator.config;

import com.vasimvahabov.stockmarketsimulator.constant.ExecutorThread;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.CandleSynchronizerProps;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.QuoteSynchronizerProps;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.StockSynchronizerProps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

import static com.vasimvahabov.stockmarketsimulator.constant.ExecutorThread.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExecutorConfig {

    StockSynchronizerProps stockSyncProps;

    QuoteSynchronizerProps quoteSyncProps;

    CandleSynchronizerProps candleSyncProps;

    @Bean(name = "stockScheduledExecutor", destroyMethod = "close")
    public ScheduledExecutorService stockScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(
                stockSyncProps.getPoolSize(),
                r -> new Thread(r, STOCK_SYNC.getThread())
        );
    }

    @Bean(name = "quoteScheduledExecutor")
    public ScheduledExecutorService quoteScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(
                quoteSyncProps.getScheduled().webSocket().poolSize(),
                new AppThreadFactory(QUOTE_SYNC)
        );
    }

    @Bean(name = "quoteExecutor")
    public ExecutorService quoteExecutor() {
        return new ThreadPoolExecutor(
                quoteSyncProps.getPoolSize(),
                quoteSyncProps.getPoolSize(),
                quoteSyncProps.getAliveTime(),
                quoteSyncProps.getAliveUnit(),
                new LinkedBlockingQueue<>(quoteSyncProps.getQueueBound()),
                new AppThreadFactory(QUOTE_SYNC)
        );
    }

    @Bean(name = "candleScheduledExecutor")
    public ScheduledExecutorService candleScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(
                candleSyncProps.getScheduled().poolSize(),
                new AppThreadFactory(CANDLE_SYNC)
        );
    }

    @Bean(name = "candleExecutor")
    public ExecutorService candleExecutor() {
        return new ThreadPoolExecutor(
                candleSyncProps.getPoolSize(),
                candleSyncProps.getPoolSize(),
                candleSyncProps.getAliveTime(),
                candleSyncProps.getAliveUnit(),
                new LinkedBlockingQueue<>(candleSyncProps.getQueueBound()),
                new AppThreadFactory(CANDLE_SYNC)
        );
    }

    private final static class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("Uncaught exception in thread '{}': {}", t.getName(), e.getMessage(), e);
        }
    }

    public record AppThreadFactory(ExecutorThread executorThread) implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, executorThread.getThread());
            thread.setUncaughtExceptionHandler(new AppUncaughtExceptionHandler());
            return thread;
        }
    }

}
