package com.vasimvahabov.stockmarketsimulator.dto.response;

public record StockResponse(

        String figi,

        String currency,

        String description,

        String displaySymbol,

        String mic,

        String symbol,

        String type
) {
}

