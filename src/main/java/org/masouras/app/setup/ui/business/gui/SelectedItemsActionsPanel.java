package org.masouras.app.setup.ui.business.gui;

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

    public void init() {
        setSpacing(true);
        setPadding(true);
        actionButtons.forEach(this::add);
    }

    public Set<T> getSelectedItems() {
        return selectedItemsSupplier.get();
    }
}
