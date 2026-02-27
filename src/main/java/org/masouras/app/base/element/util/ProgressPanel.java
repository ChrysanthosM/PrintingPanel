package org.masouras.app.base.element.util;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

public final class ProgressPanel extends VerticalLayout {
    private final ProgressBar progressBar = new ProgressBar(0, 1, 0);
    private final Span status = new Span("Waiting...");
    private int total = 0;

    public ProgressPanel() {
        setPadding(true);
        setSpacing(true);
        add(progressBar, status);
        setVisible(false);
    }

    public void start(int totalItems) {
        this.total = totalItems;
        progressBar.setValue(0);
        status.setText("0 / " + totalItems);
        setVisible(true);
    }

    public void update(int current) {
        if (total > 0) {
            progressBar.setValue((double) current / total);
            status.setText(current + " / " + total);
        }
    }

    public void finish() {
        progressBar.setValue(1);
        status.setText("Completed " + total + " items");
    }
}
