package com.vasimvahabov.stockmarketsimulator.repository;

import com.vasimvahabov.stockmarketsimulator.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandleRepository extends JpaRepository<Candle, Long> {
}
