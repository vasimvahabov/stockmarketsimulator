package com.vasimvahabov.stockmarketsimulator.repository;

import com.vasimvahabov.stockmarketsimulator.entity.QuotePublishCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotePublishCheckpointRepository extends JpaRepository<QuotePublishCheckpoint, String> {

    QuotePublishCheckpoint findByDataSource(Integer dataSource);

}
