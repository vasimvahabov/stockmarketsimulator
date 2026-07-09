package com.vasimvahabov.stockmarketsimulator.config.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps.KafkaTopicProp;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConfig {

    KafkaProps kafkaProps;

    @Bean
    NewTopic quotesRawTopic() {
        KafkaTopicProp topicProp = kafkaProps.getTopics().quotesRaw();
        return TopicBuilder.name(topicProp.name())
                .replicas(topicProp.replicas())
                .partitions(topicProp.partitions())
                .build();
    }

}
