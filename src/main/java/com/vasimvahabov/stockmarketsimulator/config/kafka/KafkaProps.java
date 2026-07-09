package com.vasimvahabov.stockmarketsimulator.config.kafka;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "kafka")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaProps {

    @NotEmpty
    KafkaTopicProps topics;

    public record KafkaTopicProps(
            @Nonnull KafkaTopicProp quotesRaw
    ) {
    }

    public record KafkaTopicProp(
            @Nonnull String name,
            int partitions,
            int replicas
    ) {
    }

}
