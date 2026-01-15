package org.masouras.app.base.element.component;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
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

public final class GenericEntityGridContainer<T> extends VerticalLayout {
    public static class AddEntityEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        public AddEntityEvent(GenericEntityGridContainer<T> source) {
            super(source, false);
        }
    }
    public static class EditEntityEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        @Getter private final T entity;
        public EditEntityEvent(GenericEntityGridContainer<T> source, T entity) {
            super(source, false);
            this.entity = entity;
        }
    }
    public static class DeleteEntitiesEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        @Getter private final List<T> entities;
        public DeleteEntitiesEvent(GenericEntityGridContainer<T> source, List<T> entities) {
            super(source, false);
            this.entities = entities;
        }
    }
    public static class RefreshEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        public RefreshEvent(GenericEntityGridContainer<T> source) {
            super(source, false);
        }
    }

    private final Class<T> entityClass;
    private final PaginationBar paginationBar;

    @Getter private final GenericEntityGridState<T> gridState = new GenericEntityGridState<>();

    public GenericEntityGridContainer(Class<T> entityClass, int pageSize) {
        this.entityClass = entityClass;
        this.paginationBar = new PaginationBar(pageSize);
        init();
    }
    private void init() {
        configureGrid();
        add(gridState.getGrid(), paginationBar);
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }
    public void addAddEntityListener(ComponentEventListener<AddEntityEvent<T>> listener) { addListener(AddEntityEvent.class, (ComponentEventListener) listener); }
    public void addEditEntityListener(ComponentEventListener<EditEntityEvent<T>> listener) { addListener(EditEntityEvent.class, (ComponentEventListener) listener); }
    public void addDeleteEntitiesListener(ComponentEventListener<DeleteEntitiesEvent<T>> listener) { addListener(DeleteEntitiesEvent.class, (ComponentEventListener) listener); }
    public void addRefreshListener(ComponentEventListener<RefreshEvent<T>> listener) { addListener(RefreshEvent.class, (ComponentEventListener) listener); }
    public void addPageChangeListener(ComponentEventListener<PaginationBar.PageChangeEvent> listener) { paginationBar.addPageChangeListener(listener); }
    public int getCurrentPage() { return paginationBar.getCurrentPage(); }
    public int getPageSize() { return paginationBar.getPageSize(); }


    public void setGridItems(Page<T> page) {
        gridState.setAllItems(page.getContent());
        gridState.getGrid().setItems(gridState.getAllItems());
        if (CollectionUtils.isNotEmpty(gridState.getCurrentSortOrders())) gridState.getGrid().sort(gridState.getCurrentSortOrders());
        paginationBar.updatePaginationBar((int) page.getTotalElements(), page.getTotalPages());
    }

    private void configureGrid() {
        configureGridControl();
        addGridColumns();
        addGridFilterRow();

        addGridEditDeleteColumn();

        addGridAddEntityColumn();
        addGridClearAllFiltersButton();
    }
    private void configureGridControl() {
        gridState.getGrid().setSizeFull();
        gridState.getGrid().setEmptyStateText("No items found");
//        gridState.getGrid().addThemeVariants(GridVariant.LUMO_NO_BORDER);
        gridState.getGrid().setSelectionMode(Grid.SelectionMode.MULTI);
        gridState.getGrid().setMultiSort(true);
        gridState.getGrid().addSortListener(e -> {
            gridState.setCurrentSortOrders(e.getSortOrder());
            fireEvent(new RefreshEvent<>(this));
        });
    }

    private void addGridColumns() {
        addGridColumnsEmbeddedIds();
        addGridColumnsAttributes();
    }
    private void addGridColumnsEmbeddedIds() {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EmbeddedId.class))
                .forEach(embeddedField -> {
                    embeddedField.setAccessible(true);
                    Arrays.stream(embeddedField.getType().getDeclaredFields())
                            .filter(subField -> subField.isAnnotationPresent(FormField.class))
                            .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                            .forEach(formField -> VaadinGridUtils.createGridColumn(
                                    gridState.getGrid(), formField, embeddedField.getName() + "." + formField.getName(),
                                    (entity, field) -> VaadinGridUtils.getEmbeddedFieldValueOr(embeddedField, field, entity, StringUtils.EMPTY), this::addFilterForColumn));
                });
    }
    private void addGridColumnsAttributes() {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(formField -> VaadinGridUtils.createGridColumn(
                        gridState.getGrid(), formField, formField.getName(),
                        (entity, field) -> VaadinGridUtils.getFieldValueOr(field, entity, StringUtils.EMPTY), this::addFilterForColumn));
    }

    private void addGridFilterRow() {
        gridState.setFilterRow(gridState.getGrid().appendHeaderRow());
        gridState.getColumnProperties().keySet().stream()
                .map(column -> Map.entry(column, gridState.getColumnFilters().get(column)))
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> gridState.getFilterRow().getCell(entry.getKey()).setComponent(entry.getValue()));
    }
    private void addFilterForColumn(Grid.Column<T> col, String property) {
        Field field = VaadinGridUtils.resolveField(entityClass, property);
        if (field == null) return;
        gridState.getColumnFilters().put(col, VaadinGridUtils.createFilterComponent(field));
        gridState.getColumnProperties().put(col, property);
    }

    private void addGridClearAllFiltersButton() {
        Button clearBtn = VaadinGridUtils.createButton(null, new Icon(VaadinIcon.CLOSE_CIRCLE), "Clear all filters",
                _ -> clearAllFilters(), ButtonVariant.LUMO_TERTIARY_INLINE);
        gridState.getFilterRow().getCell(gridState.getGrid().getColumns().getFirst()).setComponent(clearBtn);
    }
    private void clearAllFilters() {
        gridState.getColumnFilters().values().forEach(component -> {
            if (component instanceof TextField textField) textField.clear();
            if (component instanceof ComboBox<?> comboBox) comboBox.clear();
        });
        List<GridSortOrder<T>> sortOrders = gridState.getGrid().getSortOrder();
        fireEvent(new RefreshEvent<>(this));
        gridState.getGrid().sort(sortOrders);
    }

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

    private void addGridAddEntityColumn() {
        Grid.Column<T> addCol = gridState.getGrid().addColumn(_ -> StringUtils.EMPTY)
                .setHeader(VaadinGridUtils.createButton(null, new Icon(VaadinIcon.PLUS_CIRCLE), "Add Row",
                        _ -> fireEvent(new AddEntityEvent<>(this)), ButtonVariant.LUMO_TERTIARY_INLINE))
                .setAutoWidth(true)
                .setFlexGrow(0);
        VaadinGridUtils.reorderColumnsSetFirst(gridState.getGrid(), addCol);
    }

    private void addGridEditDeleteColumn() {
        Grid.Column<T> editDeleteCol = gridState.getGrid().addColumn(new ComponentRenderer<>(entity -> {
                    HorizontalLayout actions = new HorizontalLayout(
                            VaadinGridUtils.createButton(null, new Icon(VaadinIcon.EDIT), "Edit Row",
                                    _ -> fireEvent(new EditEntityEvent<>(this, entity))),
                            VaadinGridUtils.createButton(null, new Icon(VaadinIcon.TRASH), "Delete Row",
                                    _ -> showDeleteDialog(entity)));
                    actions.setWidthFull();
                    actions.setJustifyContentMode(JustifyContentMode.END);
                    actions.setSpacing(true);
                    return actions;
                }))
                .setHeader(VaadinGridUtils.createButton("Apply Filters/Reload", new Icon(VaadinIcon.REFRESH), "Reload Data",
                        _ -> fireEvent(new RefreshEvent<>(this)), ButtonVariant.LUMO_TERTIARY_INLINE))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        gridState.getFilterRow().getCell(editDeleteCol).setComponent(VaadinGridUtils.createButton("Delete Selected", new Icon(VaadinIcon.TRASH), "Delete Selected Rows",
                _ -> showBulkDeleteDialog(), ButtonVariant.LUMO_WARNING));
    }


    private void showDeleteDialog(T entity) {
        Dialog dialog = new Dialog();
        dialog.add("Are you sure you want to delete this record?");
        dialog.add(new HorizontalLayout(
                VaadinGridUtils.createButton("Delete", new Icon(VaadinIcon.TRASH), "Delete",
                        _ -> {
                            fireEvent(new DeleteEntitiesEvent<>(this, List.of(entity)));
                            dialog.close();
                        }, ButtonVariant.LUMO_WARNING),
                VaadinGridUtils.createButton("Cancel", new Icon(VaadinIcon.LEVEL_RIGHT), "Cancel", _ -> dialog.close())
        ));
        dialog.open();
    }
    private void showBulkDeleteDialog() {
        Set<T> selected = gridState.getGrid().getSelectedItems();
        if (CollectionUtils.isEmpty(selected)) return;

        Dialog dialog = new Dialog();
        dialog.add("Delete " + selected.size() + " selected item" + (selected.size() == 1 ? StringUtils.EMPTY : "s") + "?");
        dialog.add(new HorizontalLayout(
                VaadinGridUtils.createButton("Delete", new Icon(VaadinIcon.TRASH), "Delete",
                        _ -> {
                            fireEvent(new DeleteEntitiesEvent<>(this, new ArrayList<>(selected)));
                            dialog.close();
                        }, ButtonVariant.LUMO_WARNING),
                VaadinGridUtils.createButton("Cancel", new Icon(VaadinIcon.LEVEL_RIGHT), "Cancel", _ -> dialog.close())
        ));
        dialog.open();
    }
}
