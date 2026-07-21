package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;

import java.util.List;
import java.util.Map;

public interface StockService {

    void createStocks(Exchange exchange, List<StockResponse> responses);

    Map<String, Stock> findStocksMap();

    List<Stock> findStocksList();

    Map<String, Stock> findStocksBySymbols(List<String> symbols);

}
