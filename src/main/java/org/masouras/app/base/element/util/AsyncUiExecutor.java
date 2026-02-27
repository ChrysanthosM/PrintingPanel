package org.masouras.app.base.element.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@UtilityClass
public class AsyncUiExecutor {

    public static void runWithUiLock(Runnable backgroundTask, Consumer<Throwable> errorHandler, Runnable successHandler) {
        CompletableFuture
                .runAsync(backgroundTask)
                .whenComplete((_, err) -> asyncCompleted(successHandler, errorHandler, err));
    }
    private static void asyncCompleted(Runnable successHandler, Consumer<Throwable> errorHandler, Throwable err) {
        if (err != null) {
            errorHandler.accept(err.getCause() != null ? err.getCause() : err);
        } else {
            successHandler.run();
        }
    }
}
