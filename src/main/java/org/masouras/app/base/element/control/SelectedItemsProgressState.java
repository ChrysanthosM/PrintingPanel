package org.masouras.app.base.element.control;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.masouras.app.base.element.component.GenericGridContainer;
import org.masouras.app.base.element.util.ProgressPanel;
import org.masouras.app.base.element.util.VaadinNotificationFactory;
import org.masouras.app.business.printing.SelectedItemsProgressService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public final class SelectedItemsProgressState<T> {
    private final SelectedItemsProgressService selectedItemsProgressService;
    @Getter private final GenericGridContainer<T> genericGridContainer;

    @Getter private final ProgressPanel progressPanel = new ProgressPanel();
    private final AtomicBoolean progressCancelled = new AtomicBoolean(false);

    @Getter private String printingJobID;
    private UI currentUI;
    private Registration pollRegistration;

    @Getter private List<T> selectedItemsCached;

    public void progressStart(boolean cancelable) {
        if (!cancelable) genericGridContainer.setEnabled(false);
        progressCancelled.set(false);

        printingJobID = selectedItemsProgressService.startJob();
        selectedItemsCached = new UnmodifiableList<>(new ArrayList<>(genericGridContainer.getGridState().getGrid().getSelectedItems()));
        progressPanel.start(selectedItemsCached.size());
        if (cancelable) progressPanel.setCancelCallback(this::progressMustCancel);

        currentUI = UI.getCurrent();
        currentUI.setPollInterval(500);
        pollRegistration = currentUI.addPollListener(_ -> progressPanel.update(selectedItemsProgressService.getCurrent(printingJobID)));
    }

    public boolean progressIsNotCancelled() {
        return !progressCancelled.get();
    }
    private void progressMustCancel() {
        progressCancelled.set(true);
    }

    public void progressIncrement() {
        selectedItemsProgressService.increment(printingJobID);
    }

    public void progressEndedOK() { currentUI.access(() -> progressEndedMain(null)); }
    public void progressEndedError(@Nullable Throwable throwable) { currentUI.access(() -> progressEndedMain(throwable)); }
    private void progressEndedMain(Throwable throwable) {
        currentUI.setPollInterval(-1);
        pollRegistration.remove();
        progressPanel.finish(throwable == null && progressIsNotCancelled());
        selectedItemsProgressService.endJob(printingJobID);

        if (throwable == null) genericGridContainer.refreshGrid();
        genericGridContainer.setEnabled(true);

        if (throwable != null) {
            VaadinNotificationFactory.showErrorNotification("An error occurred while processing selected items.", throwable);
        }
    }
}
