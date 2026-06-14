package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.mapper.StockMapper;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.vasimvahabov.stockmarketsimulator.repository.StockRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockServiceImpl implements StockService {

    RestClient restClient;

    StockMapper stockMapper;

    StockRepository stockRepository;

    EntityManager entityManager;

    @Override
    @Transactional
    public void synchronizeByExchange(Exchange exchange) {
        log.info("Synchronizing stocks for exchange {}", exchange);
        Map<String, Stock> stocksBySymbol = stockRepository
                .findByExchange(exchange)
                .stream()
                .collect(Collectors.toMap(
                        Stock::getSymbol,
                        Function.identity()
                ));

        List<Stock> stocksToSave = fetchByExchange(exchange)
                .stream()
                .map(response ->
                        Optional.ofNullable(stocksBySymbol.get(response.symbol()))
                                .map(stock -> stockMapper.responseToEntity(stock, response))
                                .orElse(stockMapper.responseToEntity(response, exchange))
                )
                .toList();
        if (!stocksToSave.isEmpty()) {
            stockRepository.saveAll(stocksToSave);
            log.info("Persisted {} stocks for exchange {}", stocksToSave.size(), exchange);
        }
        log.info("Stocks for exchange {} synchronized", exchange.getCode());
    }

    @Override
    public Map<String, Stock> retrieveStocksAsMap() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Stock> query = builder.createQuery(Stock.class);
        Root<Stock> root = query.from(Stock.class);
        query.select(root);
        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Stock::getSymbol, Function.identity()
                ));
    }

    private List<StockResponse> fetchByExchange(Exchange exchange) {
        var uri = String.format("/stock/symbol?exchange=%s", exchange.getCode());
        log.info("Fetching stocks for exchange {} with uri {}", exchange.getCode(), uri);

        var stocks = restClient.get()
                .uri(uri)
                .exchange((_, response) -> {
                    if (!response.getStatusCode().isError()) {
                        return response.bodyTo(new ParameterizedTypeReference<List<StockResponse>>() {
                        });
                    }
                    throw new RestClientException("Exception occurred: %s".formatted(response.getStatusText()));
                });

        log.info("Fetched {} stocks for currency {} with uri {}", stocks.size(), exchange.getCode(), uri);
        return stocks;
    }

}
