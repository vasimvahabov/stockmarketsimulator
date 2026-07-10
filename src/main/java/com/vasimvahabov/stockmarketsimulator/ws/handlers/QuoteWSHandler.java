package com.vasimvahabov.stockmarketsimulator.ws.handlers;

import com.vasimvahabov.stockmarketsimulator.dto.request.QuoteWSRequest;
import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.vasimvahabov.stockmarketsimulator.config.kafka.KafkaProps.KafkaTopicProp;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteWSHandler extends TextWebSocketHandler {

    ObjectMapper objectMapper;

    List<String> batchSymbols;

    KafkaTemplate<String, QuoteWSResponse> kafkaTemplate;

    CompletableFuture<Void> onCloseFuture;

    KafkaTopicProp topicProp;

    @Override
    public void handleTextMessage(@Nonnull WebSocketSession session,
                                  @Nonnull TextMessage message) throws Exception {
        QuoteWSResponse response = objectMapper.readValue(message.getPayload(), QuoteWSResponse.class);
        log.info("Received quote response: {}", response);
        List<QuoteWSResponse.Data> responseData = response.data();

        if (responseData == null || responseData.isEmpty()) {
            return;
        }

        ProducerRecord<String, QuoteWSResponse> record = new ProducerRecord<>(
                topicProp.name(),
                null,
                Instant.now().toEpochMilli(),
                responseData.getFirst().symbol(),
                response
        );
        kafkaTemplate.send(record);
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws Exception {
        log.debug(
                "Connected to Finnhub WebSocket server at {}",
                session.getRemoteAddress()
        );
        batchSymbols.forEach(symbol -> {
            try {
                String payload = objectMapper.writeValueAsString(
                        new QuoteWSRequest("subscribe", symbol)
                );
                session.sendMessage(new TextMessage(payload));
                log.debug("Subscribed to {}", symbol);
            } catch (IOException exception) {
                log.error("Failed to subscribe to {}: {}", symbol, exception.getMessage(), exception);
            }
        });

    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session,
                                      @Nonnull CloseStatus status) {
        log.info("Closed connection to Finnhub WebSocket server {}", status.getReason());
        onCloseFuture.complete(null);
    }

}
