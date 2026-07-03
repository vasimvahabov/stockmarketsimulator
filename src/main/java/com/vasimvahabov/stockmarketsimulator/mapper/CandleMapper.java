package com.vasimvahabov.stockmarketsimulator.mapper;

import com.vasimvahabov.stockmarketsimulator.constant.Timeframe;
import com.vasimvahabov.stockmarketsimulator.entity.Candle;
import com.vasimvahabov.stockmarketsimulator.entity.Quote;
import com.vasimvahabov.stockmarketsimulator.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CandleMapper {

    default Optional<Candle> buildCandle(Stock stock, List<Quote> quotes, Timeframe timeframe) {
        if (quotes.isEmpty()) {
            return Optional.empty();
        }

        List<Quote> sortedQuotesByTimestamp = quotes.stream()
                .sorted(Comparator.comparing(Quote::getTimestampMs))
                .toList();

        Instant intervalStart = sortedQuotesByTimestamp.getFirst().getTimestampMs().truncatedTo(timeframe.getUnit());
        BigDecimal open = sortedQuotesByTimestamp.getFirst().getLastPrice();
        BigDecimal close = sortedQuotesByTimestamp.getLast().getLastPrice();

        BigDecimal high = BigDecimal.ZERO;
        BigDecimal low = BigDecimal.valueOf(Long.MAX_VALUE);
        BigDecimal volume = BigDecimal.ZERO;

        for (var quote : quotes) {
            BigDecimal lastPrice = quote.getLastPrice();
            high = high.max(lastPrice);
            low = low.min(lastPrice);
            volume = volume.add(quote.getVolume());
        }

        return Optional.of(Candle.builder()
                .timeframe(timeframe)
                .low(low)
                .high(high)
                .open(open)
                .close(close)
                .volume(volume)
                .timestampMs(intervalStart)
                .stock(stock).build()
        );
    }


}
