package com.vasimvahabov.stockmarketsimulator.mapper;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Candle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CandleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "close", ignore = true)
    @Mapping(target = "timeInSeconds", expression = "java(quote.instant())")
    Candle quoteToCandle(QuoteResponse quote);

}
