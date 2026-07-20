package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.mapper.StockMapper;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    StockMapper stockMapper;

    StockRepository stockRepository;

    @Override
    public void createStocks(Exchange exchange, List<StockResponse> responses) {
        Map<String, Stock> stockMap = findStocks();

        List<Stock> stocksToSave = responses
                .stream()
                .map(response ->
                        Optional.ofNullable(stockMap.get(response.symbol()))
                                .map(stock -> stockMapper.responseToEntity(stock, response))
                                .orElse(stockMapper.responseToEntity(response, exchange))
                )
                .toList();
        if (!stocksToSave.isEmpty()) {
            stockRepository.saveAll(stocksToSave);
            log.info("Persisted {} stocks for exchange {}", stocksToSave.size(), exchange);
        }
    }

    @Override
    public Map<String, Stock> findStocks() {
        return stockRepository.findAll()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Stock::getSymbol, Function.identity()
                ));
    }

    @Override
    public Map<String, Stock> findStocksBySymbols(List<String> symbols) {
        return stockRepository.findAllBySymbolIn(symbols)
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Stock::getSymbol,
                        Function.identity()
                ));
    }

}
