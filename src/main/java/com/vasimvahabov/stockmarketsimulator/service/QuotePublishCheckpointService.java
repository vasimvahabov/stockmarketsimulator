package com.vasimvahabov.stockmarketsimulator.service;

import com.vasimvahabov.stockmarketsimulator.entity.QuotePublishCheckpoint;

public interface QuotePublishCheckpointService {

    QuotePublishCheckpoint findByDataSource(Integer dataSource);

    void saveQuotePublishCheckpoint(QuotePublishCheckpoint quotePublishCheckpoint);

}
