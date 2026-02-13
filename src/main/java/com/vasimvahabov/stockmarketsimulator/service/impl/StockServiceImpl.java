package com.vasimvahabov.stockmarketsimulator.service.impl;


import com.vasimvahabov.stockmarketsimulator.dto.response.StockResponse;
import com.vasimvahabov.stockmarketsimulator.mapper.StockMapper;
import com.vasimvahabov.stockmarketsimulator.service.StockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.vasimvahabov.stockmarketsimulator.repository.StockRepository;
import java.util.Currency;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockServiceImpl implements StockService {

    RestClient restClient;
    StockMapper stockMapper;
    StockRepository stockRepository;

    @Override
    public void persistStocksByCurrency(Currency currency) {
        stockRepository.saveAll(fetchStocksByCurrency(currency).stream()
                .map(stockMapper::responseToEntity).toList());
    }

    private List<StockResponse> fetchStocksByCurrency(@NonNull Currency currency) {
        var currencySymbol = currency.getSymbol();
        var adjustedCurrencyCode = currencySymbol.substring(0, currencySymbol.length() - 1);
        var uri = String.format("/stock/symbol?exchange=%s", adjustedCurrencyCode);

        return restClient.get()
                .uri(uri)
                .exchange((request, response) -> {
                    if (!response.getStatusCode().isError()) {
                        return response.bodyTo(new ParameterizedTypeReference<List<StockResponse>>() {
                        });
                    }
                    throw new RestClientException("Exception occurred: %s".formatted(response.getStatusText()));
                });
    }

}
