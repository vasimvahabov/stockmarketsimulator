package com.vasimvahabov.stockmarketsimulator.entity;

import com.vasimvahabov.stockmarketsimulator.constant.Timeframe;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candles")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Candle {

    @Id
    @GeneratedValue(
            generator = "gen_candle_id",
            strategy = GenerationType.SEQUENCE
    )
    @SequenceGenerator(
            name = "gen_candle_id",
            sequenceName = "seq_candle_id",
            allocationSize = 100
    )
    Long id;

    @Column(name = "timestamp_ms")
    Instant timestampMs;

    @Column(name = "open")
    BigDecimal open;

    @Column(name = "high")
    BigDecimal high;

    @Column(name = "low")
    BigDecimal low;

    @Column(name = "close")
    BigDecimal close;

    @Column(name = "volume")
    BigDecimal volume;

    @Column(name = "timeframe")
    Timeframe timeframe;

    @ManyToOne
    @JoinColumn(
            name = "stock_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_candle_stock")
    )
    Stock stock;

}
