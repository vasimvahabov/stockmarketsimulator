package com.vasimvahabov.stockmarketsimulator.util;

import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class CompletableFutureUtils {

    public static <T> BiConsumer<T, Throwable> logFailure(Logger log, String message) {
        return (_, throwable) -> {
            if (throwable != null) {
                log.error("{}: {}", message, throwable.getMessage(), throwable);
            }
        };
    }

    public static <T> BiConsumer<T, Throwable> logFailureAndCompleteExceptionally(
            Logger log, String message, CompletableFuture<?> future
    ) {
        return (_, throwable) -> {
            if (throwable != null) {
                log.error("{}: {}", message, throwable.getMessage(), throwable);
                future.completeExceptionally(throwable);
            }
        };
    }

}
