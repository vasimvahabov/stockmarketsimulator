package com.vasimvahabov.stockmarketsimulator.config;

import com.vasimvahabov.stockmarketsimulator.constant.ExecutorThread;
import com.vasimvahabov.stockmarketsimulator.scheduler.properties.StockSynchronizerProps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExecutorConfig {

    StockSynchronizerProps stockSyncProps;

    @Bean(name = "stockScheduledExecutor", destroyMethod = "close")
    public ScheduledExecutorService stockScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(
                stockSyncProps.getThreadPoolSize(),
                r -> new Thread(r, ExecutorThread.STOCK_SYNC.getThread())
        );
    }

}



