package org.masouras.app.base.element.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class SelectedItemsActionsPanel<T> extends VerticalLayout {
    private final SelectedItemsProgressState<T> selectedItemsProgressState;
    private final List<Button> actionButtons;
    private final List<Component> leftSideComponents;

    private final HorizontalLayout topBar = new HorizontalLayout();
    private final HorizontalLayout leftSide = new HorizontalLayout();
    private final HorizontalLayout rightSide = new HorizontalLayout();

    public void init() {
        configureLayout();

        leftSideComponents.forEach(leftSide::add);
        actionButtons.forEach(rightSide::add);

        topBar.add(leftSide, rightSide);
        add(topBar, selectedItemsProgressState.getProgressPanel());
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
}
