package com.vasimvahabov.stockmarketsimulator.entity;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stocks")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Stock {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "gen_stock_id", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "gen_stock_id", sequenceName = "seq_stock_id")
    Long id;

    @Column(name = "figi")
    String figi;

    @Column(name = "currency")
    String currency;

    @Column(name = "description")
    String description;

    @Column(name = "display_symbol")
    String displaySymbol;

    @Column(name = "mic")
    String mic;

    @Column(name = "symbol")
    String symbol;

    // Fix
    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
    List<Order> orders;

}
