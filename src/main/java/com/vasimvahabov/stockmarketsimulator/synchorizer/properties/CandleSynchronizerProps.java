package com.vasimvahabov.stockmarketsimulator.synchorizer.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "executor.candle")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandleSynchronizerProps {

    int poolSize;

    long aliveTime;

    TimeUnit aliveUnit;

    int queueBound;

    Scheduled scheduled;

    public record Scheduled(

            int poolSize,

            Long initialDelay,

            Long period,

            TimeUnit unit
    ) {
    }

}
