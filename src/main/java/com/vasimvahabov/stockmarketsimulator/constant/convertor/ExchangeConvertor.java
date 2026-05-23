package com.vasimvahabov.stockmarketsimulator.constant.convertor;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import jakarta.annotation.Nonnull;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExchangeConvertor implements AttributeConverter<Exchange, Integer> {

    @Override
    public Integer convertToDatabaseColumn(@Nonnull Exchange attribute) {
        return attribute.getNumericCode();
    }

    @Override
    public Exchange convertToEntityAttribute(@Nonnull Integer dbData) {
        return Exchange.findByNumericCode(dbData);
    }

}
