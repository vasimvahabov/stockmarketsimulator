package com.vasimvahabov.stockmarketsimulator.scheduler.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConfigurationProperties("executor.stock")
public class StockSynchronizerConfig {

    Long initialDelaySec;

    Long periodSec;

    TimeUnit timeUnit;

}
