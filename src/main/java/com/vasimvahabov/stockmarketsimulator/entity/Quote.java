package com.vasimvahabov.stockmarketsimulator.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quotes")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Quote {

    @Id
    @Column(name = "id")
    @GeneratedValue(
            generator = "gen_quote_id",
            strategy = GenerationType.SEQUENCE
    )
    @SequenceGenerator(
            name = "gen_quote_id",
            sequenceName = "seq_quote_id",
            allocationSize = 100
    )
    Long id;

    @Column(name = "last_price")
    BigDecimal lastPrice;

    @Column(name = "timestamp_ms")
    Instant timestampMs;

    @Column(name = "volume")
    BigDecimal volume;

    @ManyToOne
    @JoinColumn(
            name = "stock_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_quote_stock")
    )
    Stock stock;

}
