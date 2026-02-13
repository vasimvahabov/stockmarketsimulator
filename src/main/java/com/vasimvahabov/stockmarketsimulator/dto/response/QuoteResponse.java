package com.vasimvahabov.stockmarketsimulator.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(
        @JsonProperty("c") BigDecimal currentPrice,
        @JsonProperty("h") BigDecimal high,
        @JsonProperty("l") BigDecimal low,
        @JsonProperty("o") BigDecimal open,
        @JsonProperty("pc") BigDecimal previousClosePrice,
        @JsonProperty("t") Long timeInSeconds
) {

    public Instant instant() {
        return Instant.ofEpochSecond(timeInSeconds);
    }

}
