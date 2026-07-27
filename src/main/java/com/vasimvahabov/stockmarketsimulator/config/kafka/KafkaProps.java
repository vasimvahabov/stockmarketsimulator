package com.vasimvahabov.stockmarketsimulator.config.kafka;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
public record KafkaProps(
        @Nonnull String dltSuffix,
        @NotNull KafkaTopicProps topics
) {

    public record KafkaTopicProps(
            @Nonnull KafkaTopicProp quotesRaw
    ) {
    }

    public record KafkaTopicProp(
            @Nonnull String name,
            int partitions,
            int replicas,
            @NotNull KafkaConsumerProp consumer
    ) {
    }

    public record KafkaConsumerProp(
            @Nonnull String groupId,
            @Nonnull String clientIdPrefix
    ) {

    }

}
