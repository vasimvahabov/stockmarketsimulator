package com.vasimvahabov.stockmarketsimulator.service;

import jakarta.annotation.Nonnull;
import java.util.Currency;

public interface StockService {

    void fetchStocksByCurrency(@Nonnull Currency currency);

}
