package com.vasimvahabov.stockmarketsimulator.synchorizer.properties;

import com.vasimvahabov.stockmarketsimulator.util.DateTimeUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConfigurationProperties("executor.quote")
public class QuoteSynchronizerProps {

    WebSocket webSocket;

    public record WebSocket(

            int threadPoolSize,

            long sessionDuration,

            TimeUnit sessionDurationUnit,

            long closeGracePeriod,

            TimeUnit closeGracePeriodUnit,

            int batchSize,

            long batchDelay,

            TimeUnit batchDelayUnit

    ) {

        public long sessionDurationInMillis() {
            return DateTimeUtils.toMillis(sessionDuration, sessionDurationUnit);
        }

        public long closeGracePeriodInMillis() {
            return DateTimeUtils.toMillis(closeGracePeriod, closeGracePeriodUnit);
        }

    }
}
