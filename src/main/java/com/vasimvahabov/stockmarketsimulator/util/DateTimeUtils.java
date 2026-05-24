package com.vasimvahabov.stockmarketsimulator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    public static long millisUntilMidnightUTC() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime midnight = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (now.compareTo(midnight) > 0) {
            midnight = midnight.plusDays(1);
        }
        return Duration.between(now, midnight).toMillis();
    }

}
