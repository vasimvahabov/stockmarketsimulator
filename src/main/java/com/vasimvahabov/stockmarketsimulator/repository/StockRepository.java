package com.vasimvahabov.stockmarketsimulator.repository;

import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

}
