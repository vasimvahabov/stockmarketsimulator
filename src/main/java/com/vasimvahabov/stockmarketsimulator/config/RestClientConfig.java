package com.vasimvahabov.stockmarketsimulator.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;

@Getter
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestClientConfig {

    FinnhubProps finnhubProps;

    @Bean
    RestClient finnhubClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        FinnhubProps.Rest restProps = finnhubProps.getRest();

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(restProps.baseUrl())
                .apiVersionInserter(ApiVersionInserter.usePathSegment(1))
                .defaultApiVersion(restProps.apiVersion())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(restProps.authHeader(), finnhubProps.getApiKey())
                .build();
    }

}
