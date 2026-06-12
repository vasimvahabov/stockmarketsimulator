package com.vasimvahabov.stockmarketsimulator.repository;

import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByTimestampMsGreaterThanEqual(Instant timestampMs);

}
