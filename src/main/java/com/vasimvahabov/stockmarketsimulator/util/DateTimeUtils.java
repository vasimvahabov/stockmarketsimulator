package com.vasimvahabov.stockmarketsimulator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

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

    public static long toMillis(long duration, TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }

    public static Instant yesterday() {
        return Instant.now().minus(1, ChronoUnit.DAYS);
    }

}
