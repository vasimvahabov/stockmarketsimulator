package com.vasimvahabov.stockmarketsimulator.config;

import com.vasimvahabov.stockmarketsimulator.constant.ExecutorThread;
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
                r -> new Thread(r, QUOTE_SYNC.getThread())
        );
    }

    @Bean(name = "quoteExecutor")
    public ExecutorService quoteExecutor() {
        return new ThreadPoolExecutor(
                quoteSyncProps.getPoolSize(),
                quoteSyncProps.getPoolSize(),
                quoteSyncProps.getAliveTime(),
                quoteSyncProps.getAliveUnit(),
                new LinkedBlockingQueue<>(quoteSyncProps.getQueueBound())
        );
    }
}
