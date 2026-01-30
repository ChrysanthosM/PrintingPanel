package org.masouras.app.base.element.component;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.masouras.app.base.element.component.GenericGridEvents.RefreshGridDtoEvent;
import org.masouras.app.base.element.util.VaadinGridUtils;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class GenericDtoGridContainer<T> extends VerticalLayout {
    private final Class<T> dtoClass;

    @Getter
    private final GenericGridState<T> gridState = new GenericGridState<>();

    public GenericDtoGridContainer(Class<T> dtoClass) {
        this.dtoClass = dtoClass;
        init();
    }

    public void addRefreshListener(ComponentEventListener<RefreshGridDtoEvent<T>> listener) {
        addListener(RefreshGridDtoEvent.class, (ComponentEventListener) listener);
    }

    public void setGridItems(List<T> items) {
        gridState.setAllItems(items);
        gridState.getGrid().setItems(VaadinGridUtils.applyFilters(items, getFilterValues()));
        if (CollectionUtils.isNotEmpty(gridState.getCurrentSortOrders())) gridState.getGrid().sort(gridState.getCurrentSortOrders());
    }

    public Map<String, Object> getFilterValues() {
        return gridState.getColumnFilters().entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .filter(entry -> gridState.getColumnProperties().get(entry.getKey()) != null)
                .map(entry -> new AbstractMap.SimpleEntry<>(gridState.getColumnProperties().get(entry.getKey()), extractValue(entry.getValue())))
                .filter(entry -> entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private Object extractValue(Component component) {
        return component instanceof HasValue<?, ?> hv ? hv.getValue() : null;
    }

    private void init() {
        configureGrid();
        add(gridState.getGrid());
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }

    private void configureGrid() {
        configureGridControl();
        addGridColumns();
        addGridFilterRow();
        addGridClearAllFiltersButton();
    }

    private void configureGridControl() {
        gridState.getGrid().setSizeFull();
        gridState.getGrid().setEmptyStateText("No items found");
        gridState.getGrid().setSelectionMode(Grid.SelectionMode.NONE);
        gridState.getGrid().setMultiSort(true);

        gridState.getGrid().addSortListener(e -> {
            gridState.setCurrentSortOrders(e.getSortOrder());
            fireEvent(new RefreshGridDtoEvent<>(this));
        });
    }

    private void addGridColumns() {
        Arrays.stream(dtoClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(field -> VaadinGridUtils.createGridColumn(
                        gridState.getGrid(),
                        null,
                        field,
                        this::addFilterForColumn
                ));
    }

    private void addFilterForColumn(Grid.Column<T> col, String property) {
        Field field = VaadinGridUtils.resolveField(dtoClass, property);
        if (field == null) return;

        gridState.getColumnFilters().put(col, VaadinGridUtils.createFilterComponent(field));
        gridState.getColumnProperties().put(col, property);
    }

    private void addGridFilterRow() {
        gridState.setFilterRow(gridState.getGrid().appendHeaderRow());

        gridState.getColumnProperties().forEach((column, _) -> {
            Component filter = gridState.getColumnFilters().get(column);
            if (filter != null) {
                gridState.getFilterRow().getCell(column).setComponent(filter);
            }
        });
    }

    private void addGridClearAllFiltersButton() {
        Button clearBtn = VaadinGridUtils.createButton(
                null,
                new Icon(VaadinIcon.CLOSE_CIRCLE),
                "Clear all filters",
                _ -> clearAllFilters(),
                ButtonVariant.LUMO_TERTIARY_INLINE
        );

        Grid.Column<T> firstCol = gridState.getGrid().getColumns().getFirst();
        gridState.getFilterRow().getCell(firstCol).setComponent(clearBtn);
    }

    private void clearAllFilters() {
        gridState.getColumnFilters().values().forEach(component -> {
            if (component instanceof TextField tf) tf.clear();
            if (component instanceof ComboBox<?> cb) cb.clear();
        });

        List<GridSortOrder<T>> sortOrders = gridState.getGrid().getSortOrder();
        fireEvent(new RefreshGridDtoEvent<>(this));
        gridState.getGrid().sort(sortOrders);
    }
}
