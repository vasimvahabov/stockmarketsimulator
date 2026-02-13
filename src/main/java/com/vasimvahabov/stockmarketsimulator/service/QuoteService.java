package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteResponse;
import jakarta.annotation.Nonnull;

public interface QuoteService {

    QuoteResponse fetchQuoteBySymbol(@Nonnull String symbol);

}
