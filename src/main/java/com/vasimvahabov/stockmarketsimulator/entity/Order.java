package com.vasimvahabov.stockmarketsimulator.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.*;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @Column(name = "id")
    @GeneratedValue(
            generator = "gen_order_id",
            strategy = GenerationType.SEQUENCE
    )
    @SequenceGenerator(
            name = "gen_order_id",
            sequenceName = "seq_order_id",
            allocationSize = 1
    )
    Long id;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "price")
    BigDecimal price;

    @Column(name = "is_buy_order")
    Boolean isBuyOrder;

    @Column(name = "placed_at")
    LocalDateTime placedAt;

    @ManyToOne
    @JoinColumn(
            name = "trader_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_order_trader")
    )
    Trader trader;

    @ManyToOne
    @JoinColumn(
            name = "stock_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_order_stock")
    )
    Stock stock;

}
