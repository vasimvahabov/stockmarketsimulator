package com.vasimvahabov.stockmarketsimulator.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "finnhub")
public class FinnhubConfig {

    @NotBlank
    String baseUrl;

    @NotBlank
    String apiVersion;

    @NotBlank
    String apiKey;

}

