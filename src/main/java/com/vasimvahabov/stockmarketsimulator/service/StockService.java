package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;

import java.util.Map;

public interface StockService {

    void synchronizeByExchange(Exchange exchange);

    Map<String, Stock> fetchAllStocksAsMap();

}
