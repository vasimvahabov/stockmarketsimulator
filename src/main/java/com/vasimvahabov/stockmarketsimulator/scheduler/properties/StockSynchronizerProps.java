package com.vasimvahabov.stockmarketsimulator.scheduler.properties;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConfigurationProperties("executor.stock")
public class StockSynchronizerProps {

    Long initialDelay;

    Long period;

    TimeUnit unit;

    int threadPoolSize;


}
