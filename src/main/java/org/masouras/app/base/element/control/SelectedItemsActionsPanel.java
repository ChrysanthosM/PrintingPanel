package org.masouras.app.base.element.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;
import org.masouras.app.base.element.util.ProgressPanel;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class SelectedItemsActionsPanel<T> extends VerticalLayout {
    private final Supplier<Set<T>> selectedItemsSupplier;
    private final List<Button> actionButtons;
    private final List<Component> leftSideComponents;
    private final ProgressPanel progressPanel;

    private final HorizontalLayout topBar = new HorizontalLayout();
    private final HorizontalLayout leftSide = new HorizontalLayout();
    private final HorizontalLayout rightSide = new HorizontalLayout();

    public void init() {
        configureLayout();

        leftSideComponents.forEach(leftSide::add);
        actionButtons.forEach(rightSide::add);

        topBar.add(leftSide, rightSide);
        add(topBar);
        if (progressPanel != null) add(progressPanel);
    }

    private void configureLayout() {
        setWidthFull();
        setSpacing(true);
        setPadding(true);

        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.setAlignItems(Alignment.CENTER);

        leftSide.setSpacing(true);
        rightSide.setSpacing(true);
    }

    public Set<T> getSelectedItems() {
        return selectedItemsSupplier.get();
    }
}
