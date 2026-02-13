package com.vasimvahabov.stockmarketsimulator.config;

import com.vasimvahabov.stockmarketsimulator.service.StockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.util.Currency;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestDataConfig {

    StockService stockService;

    @Bean
    @Profile("local")
    public CommandLineRunner fetchStock() {
        return args -> { stockService.persistStocksByCurrency(Currency.getInstance(Locale.US)); };
    }

}
