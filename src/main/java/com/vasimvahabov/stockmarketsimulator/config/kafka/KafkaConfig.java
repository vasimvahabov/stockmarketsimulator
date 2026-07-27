package com.vasimvahabov.stockmarketsimulator.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps.KafkaTopicProp;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    NewTopic quotesRawTopic(KafkaProps kafkaProps) {
        KafkaTopicProp topicProp = kafkaProps.getTopics().quotesRaw();
        return TopicBuilder.name(topicProp.name())
                .replicas(topicProp.replicas())
                .partitions(topicProp.partitions())
                .build();
    }

    @Bean
    DefaultErrorHandler defaultErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOffWithMaxRetries(10);
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(10_000);

        ConsumerRecordRecoverer recoverer = (record, exception) -> log.error(
                "Retries exhausted for record - Topic: {}, Partition: {}, Offset: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                exception
        );
        return new DefaultErrorHandler(recoverer, backOff);

    }

}
