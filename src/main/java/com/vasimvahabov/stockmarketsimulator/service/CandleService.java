package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.constant.Timeframe;
import com.vasimvahabov.stockmarketsimulator.entity.Candle;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;

import java.util.List;
import java.util.Optional;

public interface CandleService {

    void createCandles(List<Candle> candlesToCreate);

    Optional<Candle> buildCandle(Stock stock, List<Quote> quotes, Timeframe timeframe);
}
