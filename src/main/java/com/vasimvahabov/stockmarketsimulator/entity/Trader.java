package com.vasimvahabov.stockmarketsimulator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "traders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trader {

  @Id
  @Column(name = "id")
  @GeneratedValue(
    generator = "gen_trader_id",
    strategy = GenerationType.SEQUENCE
  )
  @SequenceGenerator(name = "gen_trader_id", sequenceName = "seq_trader_id")
  Long id;

  @Column(name = "name")
  String name;

  @Column(name = "email")
  String email;

  @Column(name = "balance")
  BigDecimal balance;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "trader")
  List<Order> orders;
}
