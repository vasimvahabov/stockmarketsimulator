package com.vasimvahabov.stockmarketsimulator.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps.KafkaTopicProp;
import org.springframework.kafka.config.TopicBuilder;

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

}
