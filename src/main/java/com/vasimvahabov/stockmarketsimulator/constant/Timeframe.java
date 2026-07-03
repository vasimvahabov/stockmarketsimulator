package com.vasimvahabov.stockmarketsimulator.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Timeframe {

    DAILY(1, "1d", ChronoUnit.DAYS),

    HOURLY(2, "1h", ChronoUnit.HOURS),

    MINUTE_1(3, "1m", ChronoUnit.MINUTES),

    MINUTE_5(4, "5m", ChronoUnit.MINUTES),

    MINUTE_15(5, "15m", ChronoUnit.MINUTES),

    MINUTE_30(6, "30m", ChronoUnit.MINUTES);

    int id;

    String code;

    ChronoUnit unit;

    public static Timeframe byId(int id) {
        return Arrays.stream(Timeframe.values())
                .filter(timeframe -> timeframe.id == id)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown timeframe: " + id)
                );
    }

}
