package com.vasimvahabov.stockmarketsimulator.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestClientConfig {

    FinnhubConfig finnhubConfig;

    @Bean
    RestClient finnhubClient() {
        return RestClient.builder()
                .baseUrl(finnhubConfig.getBaseUrl())
                .apiVersionInserter(ApiVersionInserter.usePathSegment(1))
                .defaultApiVersion(finnhubConfig.getApiVersion())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Finnhub-Token", finnhubConfig.getApiKey())
                .build();
    }

}
