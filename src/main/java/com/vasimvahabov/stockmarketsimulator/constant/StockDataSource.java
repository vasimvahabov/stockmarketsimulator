package com.vasimvahabov.stockmarketsimulator.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum StockDataSource {

    FINNHUB(1, "Finnhub");

    int id;

    String name;

    public static StockDataSource byId(int id) {
        return Arrays.stream(StockDataSource.values())
                .filter(dataSource -> dataSource.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock datasource: " + id));
    }

}
