package com.vasimvahabov.stockmarketsimulator.synchorizer.properties;

import com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConfigurationProperties("executor.quote.scheduled")
public class QuoteSynchronizerProps {

    WebSocket webSocket;

    public record WebSocket(

            int poolSize,

            long sessionDuration,

            TimeUnit sessionUnit,

            long gracePeriod,

            TimeUnit graceUnit,

            int batchSize,

            long batchDelay,

            TimeUnit batchUnit

    ) {

        public long sessionDurationInMillis() {
            return DateTimeUtils.toMillis(sessionDuration, sessionUnit);
        }

        public long closeGracePeriodInMillis() {
            return DateTimeUtils.toMillis(gracePeriod, graceUnit);
        }

    }
}
