package com.vasimvahabov.stockmarketsimulator.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record QuoteWSResponse(

        String type,

        Data data

) {

    public record Data(

            @JsonProperty("s") String symbol,

            @JsonProperty("p") BigDecimal lastPrice,

            @JsonProperty("t") long timeStampMs,

            @JsonProperty("v") float volume

    ) {

    }
}
