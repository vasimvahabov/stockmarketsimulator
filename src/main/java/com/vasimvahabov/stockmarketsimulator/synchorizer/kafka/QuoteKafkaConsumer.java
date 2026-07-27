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

import static com.vasimvahabov.stockmarketsimulator.constant.KafkaConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteKafkaConsumer {

    QuoteService quoteService;

    @KafkaListener(
            clientIdPrefix = "${kafka.topics.quotes-raw.consumer.client-id-prefix}",
            groupId = "${kafka.topics.quotes-raw.consumer.group-id}",
            topics = "${kafka.topics.quotes-raw.name}",
            batch = BATCH_ENABLED,
            ackMode = ACK_MODE_BATCH,
            concurrency = "${kafka.topics.quotes-raw.consumer.concurrency}"
    )
    public void consumeRawQuotes(List<ConsumerRecord<String, QuoteWSResponse>> records) {
        int recordsSize = records.size();
        String topic = records.getFirst().topic();
        int partition = records.getFirst().partition();
        long firstOffset = records.getFirst().offset();
        long lastOffset = records.getLast().offset();
        try {
            log.info(
                    "Consuming {} records - Topic: {}, Partition: {}, Offset Range: {} to {}",
                    recordsSize,
                    topic,
                    partition,
                    firstOffset,
                    lastOffset
            );

            quoteService.createQuotes(records);
            log.info(
                    "Successfully consumed {} records - Topic: {}, Partition: {}, Offset Range: {} to {}",
                    recordsSize,
                    topic,
                    partition,
                    firstOffset,
                    lastOffset
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to consume {} records - First record - Topic: {}, Partition: {}, Offset Range: {} to {}",
                    recordsSize,
                    topic,
                    partition,
                    firstOffset,
                    lastOffset,
                    exception
            );
            throw exception;
        }
    }

}
