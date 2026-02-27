package org.masouras.app.base.element.util;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import lombok.Setter;

public final class ProgressPanel extends VerticalLayout {
    private final ProgressBar progressBar = new ProgressBar(0, 1, 0);
    private final Span status = new Span("Waiting...");
    private int total = 0;
    private int latestCurrent = 0;

    @Setter private Runnable cancelCallback;
    private final Button cancelButton = VaadinButtonFactory.createButton("Cancel", new Icon(VaadinIcon.CLOSE), "Cancel",
            _ -> cancel(), ButtonVariant.LUMO_SMALL);

    public ProgressPanel() {
        setPadding(true);
        setSpacing(true);
        add(progressBar, status, cancelButton);
        setVisible(false);
    }

    public void start(int totalItems) {
        total = totalItems;
        latestCurrent = 0;
        progressBar.setValue(latestCurrent);
        status.setText(latestCurrent + " / " + total);
        setVisible(true);
        if (cancelCallback != null) cancelButton.setVisible(true);
    }

    public void update(int current) {
        latestCurrent = current;
        if (total > 0) {
            progressBar.setValue((double) latestCurrent / total);
            status.setText(latestCurrent + " / " + total);
        }
    }

    public void finish(boolean normally) {
        progressBar.setValue(1);
        status.setText("Completed " + (normally ? total : latestCurrent) + " items");
        cancelButton.setVisible(false);
    }

    public void cancel() {
        cancelButton.setVisible(false);
        cancelCallback.run();
    }
}
