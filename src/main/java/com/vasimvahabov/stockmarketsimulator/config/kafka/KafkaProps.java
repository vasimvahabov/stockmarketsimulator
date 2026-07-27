package com.vasimvahabov.stockmarketsimulator.config.kafka;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
public record KafkaProps(
        @Nonnull String dltSuffix,
        @Nonnull BackoffProps backoff,
        @NotNull TopicProps topics
) {


    public record BackoffProps(
            int maxRetries,
            long initialIntervalMs,
            long multiplier,
            long maxIntervalMs
    ) {
    }

    public record TopicProps(
            @Nonnull TopicProp quotesRaw
    ) {
    }

    public record TopicProp(
            @Nonnull String name,
            int partitions,
            int replicas,
            @NotNull ConsumerProp consumer
    ) {
    }

    public record ConsumerProp(
            @Nonnull String groupId,
            @Nonnull String clientIdPrefix
    ) {

    }

}
