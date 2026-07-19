package com.vasimvahabov.stockmarketsimulator.synchorizer.kafka;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteKafkaConsumer {

    QuoteService quoteService;

    @KafkaListener(
            clientIdPrefix = "${kafka.topics.quotes-raw.consumer.client-id-prefix}",
            groupId = "${kafka.topics.quotes-raw.consumer.group-id}",
            batch = "true",
            ackMode = "BATCH"
    )
    public void consume(List<ConsumerRecord<String, QuoteWSResponse>> records) {
        try {
            log.info("Consuming {} records", records.size());
            quoteService.createQuotes(records);
            log.info("Successfully processed consumed {} records", records.size());
        } catch (Exception exception) {
            log.error("Failed to process consumed {} records", records.size());
        }
    }

}
