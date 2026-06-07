package com.vasimvahabov.stockmarketsimulator.ws.handlers;

import com.vasimvahabov.stockmarketsimulator.dto.request.QuoteWSRequest;
import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteWSResponse;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteWSHandler extends TextWebSocketHandler {

    ObjectMapper objectMapper;

    List<String> batchSymbols;

    Queue<QuoteWSResponse> wsResponses;

    CountDownLatch closeConnectionLatch;

    @Override
    public void handleTextMessage(@Nonnull WebSocketSession session,
                                  @Nonnull TextMessage message) throws IOException {
        QuoteWSResponse wsResponse = objectMapper.readValue(message.getPayload(), QuoteWSResponse.class);
        wsResponses.offer(wsResponse);
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws Exception {
        log.info(
                "Connected to Finnhub WebSocket server at {}",
                session.getRemoteAddress()
        );
        batchSymbols.forEach(symbol -> {
            try {
                String payload = objectMapper.writeValueAsString(
                        new QuoteWSRequest("subscribe", symbol)
                );
                session.sendMessage(new TextMessage(payload));
                log.info("Subscribed to {}", symbol);
            } catch (IOException exception) {
                log.error("Failed to subscribe to {}: {}", symbol, exception.getMessage(), exception);
            }
        });
    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session,
                                      @Nonnull CloseStatus status) {
        log.info("Closed connection to Finnhub WebSocket server {}", status.getReason());
        closeConnectionLatch.countDown();
    }

}
