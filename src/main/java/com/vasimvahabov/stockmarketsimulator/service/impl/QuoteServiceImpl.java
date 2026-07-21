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
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

    StockService stockService;

    @Override
    public void createQuotes(@NonNull List<ConsumerRecord<String, QuoteWSResponse>> records) {
        try {
            Map<String, Stock> stockMap = stockService.findStocksMap();
            List<Quote> quotes = records.stream()
                    .filter(record -> record.value().data()!=null)
                    .flatMap(record -> record.value().data().stream())
                    .map(data -> quoteMapper.wsResponseToEntity(data, stockMap))
                    .toList();
            log.info("Creating {} quotes", quotes.size());
            quoteRepository.saveAll(quotes);
            log.info("Successfully created {} quotes", quotes.size());
        } catch (Exception exception) {
            log.error("Failed to create quotes: {}", exception.getMessage(), exception);
            throw exception;
        }
    }

    @Override
    public Map<Stock, List<Quote>> findQuotesGroupedByStockSince(Instant timestampMs) {
        log.info("Retrieving quotes since timestamp: {}", timestampMs);
        Map<Stock, List<Quote>> quoteMap = quoteRepository
                .findAllByTimestampMsGreaterThanEqual(timestampMs)
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Quote::getStock, Collectors.toUnmodifiableList()),
                        Collections::unmodifiableMap
                ));

        log.info("Retrieved quotes successfully since timestamp: {}", timestampMs);
        return quoteMap;
    }

}


