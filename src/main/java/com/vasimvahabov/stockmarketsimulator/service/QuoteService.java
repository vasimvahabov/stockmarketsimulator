package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import jakarta.annotation.Nonnull;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface QuoteService {

    void createQuotes(@Nonnull List<ConsumerRecord<String, QuoteWSResponse>> records);

    Map<Stock, List<Quote>> findQuotesGroupedByStockSince(Instant timestampMs);

}
