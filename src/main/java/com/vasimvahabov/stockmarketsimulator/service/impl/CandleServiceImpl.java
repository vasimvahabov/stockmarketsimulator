package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.constant.Timeframe;
import com.vasimvahabov.stockmarketsimulator.entity.Candle;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import com.vasimvahabov.stockmarketsimulator.mapper.CandleMapper;
import com.vasimvahabov.stockmarketsimulator.repository.CandleRepository;
import com.vasimvahabov.stockmarketsimulator.service.CandleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandleServiceImpl implements CandleService {

    CandleRepository candleRepository;

    CandleMapper candleMapper;

    public void createCandles(List<Candle> candlesToCreate) {
        if (candlesToCreate.isEmpty()) {
            log.warn("No candles to create (candles list is empty)");
            return;
        }

        log.info("Creating {} candles", candlesToCreate.size());
        List<Candle> createdCandles = Collections.unmodifiableList(
                candleRepository.saveAll(candlesToCreate)
        );
        log.info("Successfully created {} candles", createdCandles.size());
    }

    public Optional<Candle> buildCandle(Stock stock, List<Quote> quotes, Timeframe timeframe) {
        try {
            log.info("Building candles for Stock {}", stock.getSymbol());
            Optional<Candle> candle = candleMapper.buildCandle(stock, quotes, timeframe);
            log.info("Built candles successfully for Stock {}", stock.getSymbol());
            return candle;
        } catch (Exception exception) {
            log.error("Exception on building candle for Stock {}: {}",
                    stock.getSymbol(), exception.getMessage(), exception);
            return Optional.empty();
        }
    }

}
