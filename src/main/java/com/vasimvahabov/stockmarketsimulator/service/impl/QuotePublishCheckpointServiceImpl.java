package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.entity.QuotePublishCheckpoint;
import com.vasimvahabov.stockmarketsimulator.repository.QuotePublishCheckpointRepository;
import com.vasimvahabov.stockmarketsimulator.service.QuotePublishCheckpointService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuotePublishCheckpointServiceImpl implements QuotePublishCheckpointService {

    QuotePublishCheckpointRepository checkpointRepository;

    @Override
    public QuotePublishCheckpoint findByDataSource(Integer dataSource) {
        return checkpointRepository.findByDataSource(dataSource);
    }

    @Override
    public void saveQuotePublishCheckpoint(QuotePublishCheckpoint checkpoint) {
        log.info("Saving {} quote publish checkpoint", checkpoint.getDataSource());
        checkpointRepository.save(checkpoint);
        log.info("Successfully saved {} quote publish checkpoint", checkpoint.getDataSource());
    }

}
