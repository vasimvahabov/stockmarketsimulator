package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Quote_;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.mapper.QuoteMapper;
import com.vasimvahabov.stockmarketsimulator.repository.QuoteRepository;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteServiceImpl implements QuoteService {

    QuoteRepository quoteRepository;

    QuoteMapper quoteMapper;

    EntityManager entityManager;

    public void create(@NonNull List<QuoteWSResponse> wsResponses, @NonNull Map<String, Stock> stocksMap) {
        try {
            log.info("Starting quote creation process with {} responses", wsResponses.size());
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

            log.info("Preparing to create {} quotes", quotesToCreate.size());
            List<Quote> savedQuotes = quoteRepository.saveAll(quotesToCreate);
            log.info("Successfully created {} quotes", savedQuotes.size());
        } catch (Exception exception) {
            log.error("Failed to save quotes to database: {}", exception.getMessage(), exception);
        }
    }

    @Override
    public List<Quote> retrieveQuotesSinceTimestampMs(Instant timestampMs) {
        return Collections.unmodifiableList(quoteRepository.findByTimestampMsGreaterThanEqual(timestampMs));
    }

    @Override
    public Map<Stock, List<Quote>> retrieveQuotesAsMapSinceTimestampMs(Instant timestampMs) {
        log.info("Retrieving quotes since timestamp: {}", timestampMs);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Quote> query = builder.createQuery(Quote.class);
        Root<Quote> root = query.from(Quote.class);
        query.select(root);
        query.where(builder.greaterThanOrEqualTo(root.get(Quote_.TIMESTAMP_MS), timestampMs));
        Map<Stock, List<Quote>> quotesMap = entityManager.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingByConcurrent(
                                Quote::getStock,
                                Collectors.mapping(
                                        Function.identity(),
                                        Collectors.toUnmodifiableList()
                                )
                        ),
                        Collections::unmodifiableMap
                ));
        log.info("Retrieved quotes successfully since timestamp: {}", timestampMs);
        return quotesMap;
    }

}


