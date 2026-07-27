package com.vasimvahabov.stockmarketsimulator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static <T> BiConsumer<T, Throwable> logCompletion(
            Logger log, String successMessage, String failureMessage
    ) {
        return (_, throwable) -> {
            if (throwable != null) {
                log.error("{}: {}", failureMessage, throwable.getMessage(), throwable);
                return;
            }
            log.debug("{}", successMessage);
        };
    }

    public static <T> BiConsumer<T, Throwable> logCompletion(
            Logger log, Supplier<String> successMessage, Supplier<String> failureMessage
    ) {
        return (_, throwable) -> {
            if (throwable != null) {
                log.error("{}: {}", failureMessage.get(), throwable.getMessage(), throwable);
                return;
            }
            log.debug("{}", successMessage.get());
        };
    }

}
