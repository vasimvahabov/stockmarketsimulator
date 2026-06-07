package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Map;

public interface QuoteService {

    void create(@Nonnull List<QuoteWSResponse> wsResponses, @Nonnull Map<String, Stock> stocksMap);

}
