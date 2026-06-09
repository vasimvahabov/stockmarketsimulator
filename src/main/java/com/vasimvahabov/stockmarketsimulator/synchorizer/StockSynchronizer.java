package com.vasimvahabov.stockmarketsimulator.synchorizer;

import com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils;
import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.synchorizer.properties.StockSynchronizerProps;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
    StockSynchronizerProps synchronizerProps;

    @Qualifier("stockScheduledExecutor")
    ScheduledExecutorService scheduledExecutor;

    Exchange EXCHANGE = US;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final long initialDelayMillis = synchronizerProps.getInitialDelay() != null ?
                synchronizerProps.getInitialDelay() : DateTimeUtils.millisUntilMidnightUTC();
        scheduledExecutor.scheduleAtFixedRate(
                () -> stockService.synchronizeByExchange(EXCHANGE),
                initialDelayMillis,
                synchronizerProps.getPeriod(),
                synchronizerProps.getUnit()
        );
        log.info(
                "Stock sync executor started [delay={}ms, period={}ms, timeunit={}, exchange={}]",
                initialDelayMillis,
                synchronizerProps.getPeriod(),
                synchronizerProps.getUnit(),
                EXCHANGE
        );
    }

}
