package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteResponse;
import com.vasimvahabov.stockmarketsimulator.mapper.CandleMapper;
import com.vasimvahabov.stockmarketsimulator.repository.CandleRepository;
import com.vasimvahabov.stockmarketsimulator.service.CandleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandleServiceImpl implements CandleService {

    CandleRepository candleRepository;
    CandleMapper candleMapper;

    @Override
    public void create(QuoteResponse quote) {
        log.info("Candle: {}", candleMapper.quoteToCandle(quote));
    }

}
