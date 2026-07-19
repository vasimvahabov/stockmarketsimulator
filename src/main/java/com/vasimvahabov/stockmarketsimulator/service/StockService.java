package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;

import java.util.List;
import java.util.Map;

public interface StockService {

    void synchronizeByExchange(Exchange exchange);

    Map<String, Stock> findStocks();

    Map<String, Stock> findStocksBySymbols(List<String> symbols);

}
