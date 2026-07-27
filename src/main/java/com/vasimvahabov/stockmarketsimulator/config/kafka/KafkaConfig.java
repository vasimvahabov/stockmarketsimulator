package com.vasimvahabov.stockmarketsimulator.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps.TopicProp;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    NewTopic quotesRawTopic(KafkaProps kafkaProps) {
        TopicProp topicProp = kafkaProps.topics().quotesRaw();
        return TopicBuilder.name(topicProp.name())
                .replicas(topicProp.replicas())
                .partitions(topicProp.partitions())
                .build();
    }

    @Bean
    DefaultErrorHandler defaultErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate, KafkaProps kafkaProps) {
        KafkaProps.BackoffProps backoffProps = kafkaProps.backoff();
        ExponentialBackOff backOff = new ExponentialBackOffWithMaxRetries(backoffProps.maxRetries());
        backOff.setInitialInterval(backoffProps.initialIntervalMs());
        backOff.setMultiplier(backoffProps.multiplier());
        backOff.setMaxInterval(backoffProps.maxIntervalMs());

        ConsumerRecordRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {
            log.error(
                    "Retries exhausted after {} attempts for record - Topic: {}, Partition: {}, Offset: {}",
                    backOff.getMaxAttempts(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    exception
            );
            return new TopicPartition(
                    record.topic() + kafkaProps.dltSuffix(),
                    record.partition()
            );
        });
        return new DefaultErrorHandler(recoverer, backOff);

    }

}
