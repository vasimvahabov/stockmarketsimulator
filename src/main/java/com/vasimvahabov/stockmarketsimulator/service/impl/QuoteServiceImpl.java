package com.vasimvahabov.stockmarketsimulator.service.impl;

import com.vasimvahabov.stockmarketsimulator.dto.response.QuoteResponse;
import com.vasimvahabov.stockmarketsimulator.service.CandleService;
import com.vasimvahabov.stockmarketsimulator.service.QuoteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuoteServiceImpl implements QuoteService {

    RestClient restClient;
    CandleService candleService;

    @Override
    public QuoteResponse fetchQuoteBySymbol(@NonNull String symbol) {
        var uri = String.format("/quote?symbol=%s", symbol);

        var quote = restClient.get()
                .uri(uri)
                .exchange((_, response) -> {
                    if(!response.getStatusCode().isError()) {
                        return response.bodyTo(QuoteResponse.class);
                    }
                    throw new RestClientException("Exception occurred: %s".formatted(response.getStatusText()));
                });
        candleService.create(quote);
        return null;
    }
}


