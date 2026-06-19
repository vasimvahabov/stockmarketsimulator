package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface QuoteService {

    void createQuotes(@Nonnull List<QuoteWSResponse> wsResponses, @Nonnull Map<String, Stock> stocksMap);

    List<Quote> retrieveQuotesSinceTimestampMs(Instant timestampMs);

    Map<Stock, List<Quote>> retrieveQuotesAsMapSinceTimestampMs(Instant timestampMs);

}
