package com.vasimvahabov.stockmarketsimulator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "quote_publish_checkpoint")
public class QuotePublishCheckpoint {

    @Id
    @Column(name = "source")
    String source;

    @Column(name = "last_published_stock_id")
    Long lastPublishedStockId;

    @Column(name = "last_published_at")
    Instant lastPublishedAt;

}
