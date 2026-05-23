package com.vasimvahabov.stockmarketsimulator.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Exchange {

    US(944, "US", Currency.getInstance("USD")),

    PL(985, "PL", Currency.getInstance("PLN")),

    GB(826, "GB", Currency.getInstance("GBP")),

    CA(124, "CA", Currency.getInstance("CAD"));

    int numericCode;

    String code;

    Currency currency;

    private static final Map<Integer, Exchange> BY_NUMERIC_CODE = Arrays.stream(values())
            .collect(Collectors.toMap(Exchange::getNumericCode, Function.identity()));

    public static Exchange findByNumericCode(int numericCode) {
        return Optional.ofNullable(BY_NUMERIC_CODE.get(numericCode))
                .orElseThrow(() -> new IllegalArgumentException("Unknown exchange: " + numericCode));
    }

}
