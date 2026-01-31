package org.masouras.app.base.element.component;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.persistence.EmbeddedId;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.masouras.app.base.element.util.VaadinGridUtils;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class GenericGridContainer<T> extends VerticalLayout {
    private final Class<T> type;
    private final PaginationBar paginationBar;
    @Getter
    private final GenericGridState<T> gridState;

    public GenericGridContainer(Class<T> type, GenericGridState.GridMode gridMode, int pageSize) {
        this.type = type;
        this.gridState = new GenericGridState<>(gridMode);
        this.paginationBar = (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) ? new PaginationBar(pageSize) : null;
        init();
    }

    // ---------------------------
    // PUBLIC API
    // ---------------------------
    public void addAddEntityListener(ComponentEventListener<GenericGridEvents.AddEntityEvent<T>> listener) {
        if (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) addListener(GenericGridEvents.AddEntityEvent.class, (ComponentEventListener) listener);
    }
    public void addEditEntityListener(ComponentEventListener<GenericGridEvents.EditEntityEvent<T>> listener) {
        if (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) addListener(GenericGridEvents.EditEntityEvent.class, (ComponentEventListener) listener);
    }
    public void addDeleteEntitiesListener(ComponentEventListener<GenericGridEvents.DeleteEntitiesEvent<T>> listener) {
        if (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) addListener(GenericGridEvents.DeleteEntitiesEvent.class, (ComponentEventListener) listener);
    }
    public void addRefreshListener(ComponentEventListener<GenericGridEvents.RefreshGridEvent<T>> listener) {
        addListener(GenericGridEvents.RefreshGridEvent.class, (ComponentEventListener) listener);
    }

    public void addPageChangeListener(ComponentEventListener<PaginationBar.PageChangeEvent> listener) {
        if (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) paginationBar.addPageChangeListener(listener);
    }
    public int getCurrentPage() {
        return (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) ? paginationBar.getCurrentPage() : 0;
    }
    public int getPageSize() {
        return (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) ? paginationBar.getPageSize() : Integer.MAX_VALUE;
    }

    // ---------------------------
    // SET GRID ITEMS (ENTITY + DTO)
    // ---------------------------
    @SuppressWarnings("unchecked")
    public void setGridItems(Object data) {
        switch (gridState.getGridMode()) {
            case ENTITY_MODE -> setEntityItems((Page<T>) data);
            case DTO_MODE -> setDtoItems((List<T>) data);
        }
    }
    private void setEntityItems(Page<T> page) {
        gridState.setAllItems(page.getContent());
        gridState.getGrid().setItems(gridState.getAllItems());
        if (CollectionUtils.isNotEmpty(gridState.getCurrentSortOrders())) {
            gridState.getGrid().sort(gridState.getCurrentSortOrders());
        }
        paginationBar.updatePaginationBar((int) page.getTotalElements(), page.getTotalPages());
    }
    private void setDtoItems(List<T> items) {
        gridState.getGrid().setItems(VaadinGridUtils.applyFilters(items, getFilterValues()));
        if (CollectionUtils.isNotEmpty(gridState.getCurrentSortOrders())) gridState.getGrid().sort(gridState.getCurrentSortOrders());
        gridState.setAllItems(items);
    }

    // ---------------------------
    // FILTER EXTRACTION
    // ---------------------------
    public Map<String, Object> getFilterValues() {
        return gridState.getColumnFilters().entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .filter(entry -> gridState.getColumnProperties().get(entry.getKey()) != null)
                .map(entry -> new AbstractMap.SimpleEntry<>(gridState.getColumnProperties().get(entry.getKey()), extractValue(entry.getValue())))
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private Object extractValue(Component component) {
        return component instanceof HasValue<?, ?> hv ? hv.getValue() : null;
    }

    // ---------------------------
    // INITIALIZATION
    // ---------------------------
    private void init() {
        if (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) {
            add(paginationBar);
        }

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
        addGridLastColumn();

        if (gridState.getGridMode() == GenericGridState.GridMode.ENTITY_MODE) {
            addGridAddEntityColumn();
        }

        addGridClearAllFiltersButton();
    }

    private void configureGridControl() {
        gridState.getGrid().setSizeFull();
        gridState.getGrid().setEmptyStateText("No items found");
        gridState.getGrid().setSelectionMode(Grid.SelectionMode.MULTI);
        gridState.getGrid().setMultiSort(true);

        gridState.getGrid().addSortListener(e -> {
            gridState.setCurrentSortOrders(e.getSortOrder());
            fireEvent(new GenericGridEvents.RefreshGridEvent<>(this));
        });
    }

    // ---------------------------
    // COLUMN GENERATION
    // ---------------------------

    private void addGridColumns() {
        addEmbeddedIdColumns();
        addAttributeColumns();
    }
    private void addEmbeddedIdColumns() {
        Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EmbeddedId.class))
                .forEach(embeddedField -> {
                    embeddedField.setAccessible(true);
                    Arrays.stream(embeddedField.getType().getDeclaredFields())
                            .filter(field -> field.isAnnotationPresent(FormField.class))
                            .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                            .forEach(field -> VaadinGridUtils.createGridColumn(
                                    gridState.getGrid(), embeddedField, field, this::addFilterForColumn
                            ));
                });
    }
    private void addAttributeColumns() {
        Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(field -> VaadinGridUtils.createGridColumn(
                        gridState.getGrid(), null, field, this::addFilterForColumn
                ));
    }
    private void addFilterForColumn(Grid.Column<T> col, String property) {
        Field field = VaadinGridUtils.resolveField(type, property);
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

    // ---------------------------
    // CLEAR FILTERS
    // ---------------------------
    private void addGridClearAllFiltersButton() {
        Button clearBtn = VaadinGridUtils.createButton(null, new Icon(VaadinIcon.CLOSE_CIRCLE), "Clear all filters",
                _ -> clearAllFilters(), ButtonVariant.LUMO_TERTIARY_INLINE);
        Grid.Column<T> firstCol = gridState.getGrid().getColumns().getFirst();
        gridState.getFilterRow().getCell(firstCol).setComponent(clearBtn);
    }

    private void clearAllFilters() {
        gridState.getColumnFilters().values().forEach(component -> {
            if (component instanceof TextField tf) tf.clear();
            if (component instanceof ComboBox<?> cb) cb.clear();
        });

        List<GridSortOrder<T>> sortOrders = gridState.getGrid().getSortOrder();
        fireEvent(new GenericGridEvents.RefreshGridEvent<>(this));
        gridState.getGrid().sort(sortOrders);
    }

    // ---------------------------
    // LAST COLUMN
    // ---------------------------
    private void addGridLastColumn() {
        switch (gridState.getGridMode()) {
            case ENTITY_MODE -> addGridLastColumnEntity();
            case DTO_MODE -> addGridLastColumnDTO();
        }
    }
    private void addGridLastColumnEntity() {
        Grid.Column<T> lastCol = gridState.getGrid()
                .addColumn(new ComponentRenderer<>(this::getEditDeleteRowButtons))
                .setHeader(VaadinGridUtils.createButton("Apply Filters/Reload", new Icon(VaadinIcon.REFRESH), "Reload Data",
                        _ -> fireEvent(new GenericGridEvents.RefreshGridEvent<>(this)), ButtonVariant.LUMO_TERTIARY_INLINE))
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        gridState.getFilterRow().getCell(lastCol).setComponent(
                VaadinGridUtils.createButton("Delete Selected", new Icon(VaadinIcon.TRASH), "Delete Selected Rows",
                        _ -> GenericGridDialogs.showBulkDeleteDialog(gridState.getGrid().getSelectedItems(),
                                entities -> fireEvent(new GenericGridEvents.DeleteEntitiesEvent<>(this, entities))
                        ), ButtonVariant.LUMO_WARNING));
    }

    private void addGridLastColumnDTO() {
        gridState.getGrid()
                .addComponentColumn(_ -> null)
                .setHeader(VaadinGridUtils.createButton("Apply Filters/Reload", new Icon(VaadinIcon.REFRESH), "Reload Data",
                        _ -> fireEvent(new GenericGridEvents.RefreshGridEvent<>(this)), ButtonVariant.LUMO_TERTIARY_INLINE))
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setFlexGrow(0);
    }

    private HorizontalLayout getEditDeleteRowButtons(T entity) {
        HorizontalLayout actions = new HorizontalLayout(
                VaadinGridUtils.createButton(null, new Icon(VaadinIcon.EDIT), "Edit Row",
                        _ -> fireEvent(new GenericGridEvents.EditEntityEvent<>(this, entity))),
                VaadinGridUtils.createButton(null, new Icon(VaadinIcon.TRASH), "Delete Row",
                        _ -> GenericGridDialogs.showDeleteDialog(entity, entities -> fireEvent(new GenericGridEvents.DeleteEntitiesEvent<>(this, entities)))));
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setSpacing(true);
        return actions;
    }

    // ---------------------------
    // ENTITY‑ONLY CRUD COLUMN
    // ---------------------------
    private void addGridAddEntityColumn() {
        Grid.Column<T> addCol = gridState.getGrid()
                .addColumn(_ -> StringUtils.EMPTY)
                .setHeader(VaadinGridUtils.createButton(null, new Icon(VaadinIcon.PLUS_CIRCLE), "Add Row",
                        _ -> fireEvent(new GenericGridEvents.AddEntityEvent<>(this)), ButtonVariant.LUMO_TERTIARY_INLINE))
                .setAutoWidth(true)
                .setFlexGrow(0);
        VaadinGridUtils.reorderColumnsSetFirst(gridState.getGrid(), addCol);
    }
}
