package com.vasimvahabov.stockmarketsimulator.scheduler;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.scheduler.config.StockSynchronizerConfig;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

import static com.vasimvahabov.stockmarketsimulator.constant.Exchange.*;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockSynchronizer implements ApplicationRunner {

    StockService stockService;
    StockSynchronizerConfig synchronizerConfig;
    ScheduledExecutorService executorService;

    Exchange EXCHANGE = US;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        executorService.scheduleAtFixedRate(
                () -> stockService.synchronizeByExchange(EXCHANGE),
                synchronizerConfig.getInitialDelaySec(),
                synchronizerConfig.getPeriodSec(),
                synchronizerConfig.getTimeUnit()
        );
        log.info(
                "Stock sync executor started [delay={}s, period={}s, timeunit={}, exchange={}]",
                synchronizerConfig.getInitialDelaySec(),
                synchronizerConfig.getPeriodSec(),
                synchronizerConfig.getTimeUnit(),
                EXCHANGE
        );
    }
}
