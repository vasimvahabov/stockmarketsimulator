package com.vasimvahabov.stockmarketsimulator.config;

import java.util.concurrent.*;

import com.vasimvahabov.stockmarketsimulator.constant.ExecutorThread;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Bean(name = "stockExecutor", destroyMethod = "close")
    public ScheduledExecutorService stockExecutor() {
        return new ScheduledThreadPoolExecutor(
                1, r -> new Thread(r, ExecutorThread.STOCK_SYNC.getThread())
        );
    }

}



