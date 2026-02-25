package org.masouras.app.base.element.util;

import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;
import org.masouras.app.base.element.component.GenericGridContainer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@UtilityClass
public class AsyncUiExecutor {

    public static void runWithUiLock(GenericGridContainer<?> componentToDisable,
                                     Runnable backgroundTask,
                                     Consumer<Throwable> errorHandler, Runnable successHandler) {
        UI ui = UI.getCurrent();
        if (ui == null) throw new IllegalStateException("UI is null");

        ui.access(() -> componentToDisable.setEnabled(false));
        CompletableFuture
                .runAsync(backgroundTask)
                .whenComplete((_, err) -> ui.access(() -> asyncCompleted(componentToDisable, errorHandler, successHandler, err)));
    }
    private static void asyncCompleted(GenericGridContainer<?> componentToDisable, Consumer<Throwable> errorHandler, Runnable successHandler, Throwable err) {
        try {
            runSuccessOrError(errorHandler, successHandler, err);
        } finally {
            componentToDisable.setEnabled(true);
        }
    }
    private static void runSuccessOrError(Consumer<Throwable> errorHandler, Runnable successHandler, Throwable err) {
        if (err != null) {
            errorHandler.accept(err.getCause() != null ? err.getCause() : err);
        } else {
            successHandler.run();
        }
    }
}
