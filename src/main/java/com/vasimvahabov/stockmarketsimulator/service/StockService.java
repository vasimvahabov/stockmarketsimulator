package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;

public interface StockService {

    void synchronizeByExchange(Exchange exchange);

}
