package com.vasimvahabov.stockmarketsimulator.mapper;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import org.mapstruct.*;

import java.time.Instant;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuoteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastPrice", source = "data.lastPrice")
    @Mapping(target = "volume", source = "data.volume")
    @Mapping(
            target = "timeStampMs",
            source = "data.timeStampMs",
            qualifiedByName = "millisToInstant"
    )
    @Mapping(
            target = "stock",
            source = "data.symbol",
            qualifiedByName = "stockBySymbol"
    )
    Quote wsResponseToEntity(QuoteWSResponse.Data data, @Context Map<String, Stock> stocksMap);

    @Named("stockBySymbol")
    default Stock stockBySymbol(String symbol, @Context Map<String, Stock> stocksMap) {
        return stocksMap.get(symbol);
    }

    @Named("millisToInstant")
    default Instant millisToInstant(long millis) {
        return Instant.ofEpochMilli(millis);
    }

}
