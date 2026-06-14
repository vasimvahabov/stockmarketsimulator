package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.mapper.QuoteMapper;
import com.vasimvahabov.stockmarketsimulator.repository.QuoteRepository;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteServiceImpl implements QuoteService {

    QuoteRepository quoteRepository;
    QuoteMapper quoteMapper;

    public void create(@NonNull List<QuoteWSResponse> wsResponses, @NonNull Map<String, Stock> stocksMap) {
        log.info("Starting quote creation process with {} responses", wsResponses.size());
        List<Quote> quotesToCreate = wsResponses
                .stream()
                .flatMap(wsResponse -> Optional.ofNullable(wsResponse.data())
                                .orElseGet(Collections::emptyList).stream())
                .map(data -> quoteMapper.wsResponseToEntity(data, stocksMap))
                .toList();

        if(quotesToCreate.isEmpty()) {
            log.warn("No quotes to create from {} responses", wsResponses.size());
            return;
        }

        log.info("Preparing to create {} quotes", quotesToCreate.size());
        List<Quote> savedQuotes = quoteRepository.saveAll(quotesToCreate);
        log.info("Successfully created {} quotes", savedQuotes.size());
    }

    @Override
    public List<Quote> retrieveQuotesSinceTimestampMs(Instant timestampMs) {
        return Collections.unmodifiableList(quoteRepository.findByTimestampMsGreaterThanEqual(timestampMs));
    }

}


