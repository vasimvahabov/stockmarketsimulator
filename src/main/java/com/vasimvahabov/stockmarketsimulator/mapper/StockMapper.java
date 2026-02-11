package com.vasimvahabov.stockmarketsimulator.mapper;

import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockMapper {

    Stock responseToEntity(StockResponse response);

}
