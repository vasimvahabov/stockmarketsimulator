package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.mapper.QuoteMapper;
import com.vasimvahabov.stockmarketsimulator.repository.QuoteRepository;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteServiceImpl implements QuoteService {

    QuoteRepository quoteRepository;

    QuoteMapper quoteMapper;

    public void createQuotes(@NonNull List<QuoteWSResponse> wsResponses, @NonNull Map<String, Stock> stocksMap) {
        try {
            List<Quote> quotesToCreate = wsResponses
                    .stream()
                    .flatMap(wsResponse -> Optional.ofNullable(wsResponse.data())
                            .orElseGet(Collections::emptyList).stream())
                    .map(data -> quoteMapper.wsResponseToEntity(data, stocksMap))
                    .toList();

            if (quotesToCreate.isEmpty()) {
                log.warn("No quotes to create from {} responses", wsResponses.size());
                return;
            }
            log.info("Creating quotes: {} quotes", quotesToCreate.size());
            List<Quote> savedQuotes = quoteRepository.saveAll(quotesToCreate);
            log.info("Successfully created {} quotes", savedQuotes.size());
        } catch (Exception exception) {
            log.error("Failed to save quotes to database: {}", exception.getMessage(), exception);
        }
    }


    @Override
    public Map<Stock, List<Quote>> findQuotesGroupedByStockSince(Instant timestampMs) {
        log.info("Retrieving quotes since timestamp: {}", timestampMs);
        Map<Stock, List<Quote>> quotes = quoteRepository
                .findAllByTimestampMsGreaterThanEqual(timestampMs)
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Quote::getStock, Collectors.toUnmodifiableList()),
                        Collections::unmodifiableMap
                ));

        log.info("Retrieved quotes successfully since timestamp: {}", timestampMs);
        return quotes;
    }

}


