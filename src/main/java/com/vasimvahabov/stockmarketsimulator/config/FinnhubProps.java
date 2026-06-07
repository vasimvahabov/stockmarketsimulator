package com.vasimvahabov.stockmarketsimulator.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.TimeUnit;

@Getter
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConfigurationProperties(prefix = "finnhub")
public class FinnhubProps {

    @NotBlank
    String apiKey;

    @NotNull
    WebSocket websocket;

    @NotNull
    Rest rest;

    public record Rest(
            @NotBlank String baseUrl,
            @NotBlank String authHeader,
            @NotBlank String apiVersion
    ) {
    }

    public record WebSocket(
            @NotBlank String uri,
            @NotBlank String authQueryParam,
            @NotBlank long timeout,
            @NotBlank TimeUnit timeoutUnit
    ) {
    }
}
