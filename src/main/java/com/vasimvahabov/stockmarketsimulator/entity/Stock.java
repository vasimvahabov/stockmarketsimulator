package com.vasimvahabov.stockmarketsimulator.entity;

import com.vasimvahabov.stockmarketsimulator.constant.Exchange;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.mapping.PrimaryKey;

import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "stocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uniq_symbol",
                columnNames = "symbol"
        )
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Stock {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "gen_stock_id", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "gen_stock_id", sequenceName = "seq_stock_id")
    Long id;

    @Column(name = "figi")
    String figi;

    @Column(name = "exchange")
    Exchange exchange;

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
