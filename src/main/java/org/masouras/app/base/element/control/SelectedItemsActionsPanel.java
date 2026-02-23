package org.masouras.app.base.element.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class SelectedItemsActionsPanel<T> extends HorizontalLayout {
    private final Supplier<Set<T>> selectedItemsSupplier;
    private final List<Button> actionButtons;
    private final List<Component> leftSideComponents;

    private final HorizontalLayout leftSide = new HorizontalLayout();
    private final HorizontalLayout rightSide = new HorizontalLayout();

    public void init() {
        setStyle();

        actionButtons.forEach(rightSide::add);
        leftSideComponents.forEach(leftSide::add);

        add(leftSide, rightSide);
    }
    private void setStyle() {
        setWidthFull();
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);

        leftSide.setSpacing(true);
        leftSide.setAlignItems(Alignment.CENTER);
        rightSide.setSpacing(true);
        rightSide.setAlignItems(Alignment.CENTER);
    }

    public Set<T> getSelectedItems() {
        return selectedItemsSupplier.get();
    }
}
