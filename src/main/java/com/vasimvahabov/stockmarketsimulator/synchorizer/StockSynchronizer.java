package com.vasimvahabov.stockmarketsimulator.synchorizer;

import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.vasimvahabov.stockmarketsimulator.constant.Exchange.*;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockSynchronizer implements ApplicationRunner {

    StockService stockService;
    
    StockSynchronizerProps synchronizerProps;

    RestClient restClient;

    @Qualifier("stockScheduledExecutor")
    ScheduledExecutorService scheduledExecutor;

    Exchange EXCHANGE = US;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final long initialDelayMillis = synchronizerProps.getInitialDelay() != null ?
                synchronizerProps.getInitialDelay() : DateTimeUtils.millisUntilMidnightUTC();
        scheduledExecutor.scheduleAtFixedRate(
                () -> synchronizeByExchange(EXCHANGE),
                initialDelayMillis,
                synchronizerProps.getPeriod(),
                synchronizerProps.getUnit()
        );
        log.info(
                "Stock synchronizer executor started [delay={}ms, period={}ms, timeunit={}, exchange={}]",
                initialDelayMillis,
                synchronizerProps.getPeriod(),
                TimeUnit.MILLISECONDS,
                EXCHANGE
        );
    }

    private void synchronizeByExchange(Exchange exchange) {
        log.info("Synchronizing stocks for exchange {}", exchange);

        var uri = String.format("/stock/symbol?exchange=%s", exchange.getCode());
        log.info("Fetching stocks for exchange {} with uri {}", exchange.getCode(), uri);

        var stocks = restClient.get()
                .uri(uri)
                .exchange((_, response) -> {
                    if (!response.getStatusCode().isError()) {
                        return response.bodyTo(new ParameterizedTypeReference<List<StockResponse>>() {
                        });
                    }
                    throw new RestClientException("Exception occurred: %s".formatted(response.getStatusText()));
                });
        log.info("Fetched {} stocks for currency {} with uri {}", stocks.size(), exchange.getCode(), uri);

        stockService.createStocks(exchange, stocks);
        log.info("Synchronized stocks for exchange {}", exchange.getCode());
    }
}
