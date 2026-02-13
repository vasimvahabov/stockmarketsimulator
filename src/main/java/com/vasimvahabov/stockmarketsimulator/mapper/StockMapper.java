package com.vasimvahabov.stockmarketsimulator.mapper;

import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockMapper {

    @Mapping(target = "id", ignore = true)
    Stock responseToEntity(StockResponse response);

}
