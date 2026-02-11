package com.vasimvahabov.stockmarketsimulator.constant.convertor;

import com.vasimvahabov.stockmarketsimulator.constant.Role;
import jakarta.annotation.Nonnull;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.validation.constraints.NotNull;

@Converter(autoApply = true)
public class RoleConvertor implements AttributeConverter<Role, Integer> {

    @Override
    public Integer convertToDatabaseColumn(@NotNull Role attribute) {
        return attribute.getId();
    }

    @Override
    public Role convertToEntityAttribute(@Nonnull Integer dbData) {
        return Role.roleById(dbData);
    }

}
