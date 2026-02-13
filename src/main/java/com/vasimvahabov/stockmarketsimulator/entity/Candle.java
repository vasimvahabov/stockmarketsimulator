package com.vasimvahabov.stockmarketsimulator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candles")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Candle {

    @Id
    @Column(name = "id")
    @GeneratedValue(
            generator = "gen_candle_id",
            strategy = GenerationType.SEQUENCE
    )
    @SequenceGenerator(
            name = "gen_candle_id",
            sequenceName = "seq_candle_id", allocationSize = 1
    )
    Long id;

    @Column(name = "high")
    BigDecimal high;

    @Column(name = "low")
    BigDecimal low;

    @Column(name = "open")
    BigDecimal open;

    @Column(name = "close")
    BigDecimal close;

    @Column(name = "time_in_seconds")
    Instant timeInSeconds;

    @Override
    public String toString() {
        return "Candle{" +
                "id=" + id +
                ", high=" + high +
                ", low=" + low +
                ", open=" + open +
                ", close=" + close +
                ", timeInSeconds=" + timeInSeconds +
                '}';
    }
}
