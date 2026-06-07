package com.vasimvahabov.stockmarketsimulator.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ExecutorThread {

    STOCK_SYNC("stock-sync-thread"),

    QUOTE_SYNC("quote-sync-thread");

    final String thread;

}
