package com.vasimvahabov.stockmarketsimulator.constant.convertor;

import com.vasimvahabov.stockmarketsimulator.constant.Timeframe;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TimeframeConverter implements AttributeConverter<Timeframe, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Timeframe attribute) {
        return attribute.getId();
    }

    @Override
    public Timeframe convertToEntityAttribute(Integer dbData) {
        return Timeframe.byId(dbData);
    }

}
