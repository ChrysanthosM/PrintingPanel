package org.masouras.app.base.element.component;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.persistence.EmbeddedId;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.masouras.app.base.element.control.GenericComponentUtils;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.util.*;

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
    private final String title;
    private final PaginationBar paginationBar;

    @Getter private final GenericEntityGridState<T> gridState = new GenericEntityGridState<>();

    private boolean clearingNow = false;

    public GenericEntityGridContainer(Class<T> entityClass, String title, int pageSize) {
        this.title = title;
        this.entityClass = entityClass;
        this.paginationBar = new PaginationBar(pageSize);
        init();
    }
    private void init() {
        configureGrid();
        add(new H2(title), gridState.getGrid(), paginationBar);
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }
    public void addEntityListener(ComponentEventListener<AddEntityEvent<T>> listener) { addListener(AddEntityEvent.class, (ComponentEventListener) listener); }
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

        addGridClearAllFiltersButton();
        addGridEditDeleteColumn();
        addGridAddEntityColumn();
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
                            .forEach(formField -> GenericComponentUtils.createGridColumn(
                                    gridState.getGrid(), formField, embeddedField.getName() + "." + formField.getName(),
                                    (entity, field) -> GenericComponentUtils.getEmbeddedFieldValueOr(embeddedField, field, entity, StringUtils.EMPTY), this::addFilterForColumn));
                });
    }
    private void addGridColumnsAttributes() {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(formField -> GenericComponentUtils.createGridColumn(
                        gridState.getGrid(), formField, formField.getName(),
                        (entity, field) -> GenericComponentUtils.getFieldValueOr(field, entity, StringUtils.EMPTY), this::addFilterForColumn));
    }

    private void addGridFilterRow() {
        gridState.setFilterRow(gridState.getGrid().appendHeaderRow());
        gridState.getColumnProperties().keySet().stream()
                .map(column -> Map.entry(column, gridState.getColumnFilters().get(column)))
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> gridState.getFilterRow().getCell(entry.getKey()).setComponent(entry.getValue()));
    }
    private void addFilterForColumn(Grid.Column<T> col, String property) {
        Field field = GenericComponentUtils.resolveField(entityClass, property);
        if (field == null) return;
        gridState.getColumnFilters().put(col, GenericComponentUtils.createFilterComponent(field, _ -> applyColumnFilters()));
        gridState.getColumnProperties().put(col, property);
    }
    private void applyColumnFilters() {
        if (clearingNow) return;
        List<T> filtered = gridState.getAllItems().stream()
                .filter(item -> gridState.getColumnFilters().entrySet().stream().allMatch(entry -> {
                    Object value = GenericComponentUtils.getNestedPropertyValue(item, gridState.getColumnProperties().get(entry.getKey()));
                    if (entry.getValue() instanceof TextField tf) {
                        String filterText = tf.getValue();
                        if (StringUtils.isBlank(filterText)) return true;
                        return value != null && value.toString().toLowerCase().contains(filterText.toLowerCase());
                    }
                    if (entry.getValue() instanceof ComboBox<?> cb) {
                        Object selected = cb.getValue();
                        if (selected == null) return true;
                        return value != null && value.equals(selected);
                    }
                    return true;
                }))
                .toList();
        gridState.getGrid().setItems(filtered);
    }

    private void addGridAddEntityColumn() {
        Grid.Column<T> addCol = gridState.getGrid().addColumn(_ -> StringUtils.EMPTY)
                .setHeader(new Span())
                .setAutoWidth(true)
                .setFlexGrow(0);
        Button addBtn = new Button(new Icon(VaadinIcon.PLUS_CIRCLE), _ -> fireEvent(new AddEntityEvent<>(this)));
        addBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        addBtn.getElement().setProperty("title", "Add Row");
        gridState.getFilterRow().getCell(addCol).setComponent(addBtn);
        reorderColumnsSetFirst(addCol);
    }
    private void reorderColumnsSetFirst(Grid.Column<T> addCol) {
        List<Grid.Column<T>> newOrder = new ArrayList<>();
        newOrder.add(addCol);
        gridState.getGrid().getColumns().stream().filter(c -> c != addCol).forEach(newOrder::add);
        gridState.getGrid().setColumnOrder(newOrder);
    }

    private void addGridClearAllFiltersButton() {
        Grid.Column<T> clearCol = gridState.getGrid().addColumn(_ -> StringUtils.EMPTY)
                .setHeader(new Span())
                .setAutoWidth(true)
                .setFlexGrow(0);
        Button clearBtn = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE), _ -> clearAllFilters());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        clearBtn.getElement().setProperty("title", "Clear all filters");
        gridState.getFilterRow().getCell(clearCol).setComponent(clearBtn);
    }
    private void clearAllFilters() {
        clearingNow = true;
        try {
            gridState.getColumnFilters().values().forEach(component -> {
                if (component instanceof TextField textField) textField.clear();
                if (component instanceof ComboBox<?> comboBox) comboBox.clear();
            });
            List<GridSortOrder<T>> sortOrders = gridState.getGrid().getSortOrder();
            gridState.getGrid().setItems(gridState.getAllItems());
            gridState.getGrid().sort(sortOrders);
        } finally {
            clearingNow = false;
        }
    }

    private void addGridEditDeleteColumn() {
        Grid.Column<T> editDeleteCol = gridState.getGrid().addColumn(new ComponentRenderer<>(entity -> {
                    HorizontalLayout actions = new HorizontalLayout(
                            new Button(new Icon(VaadinIcon.EDIT), _ -> fireEvent(new EditEntityEvent<>(this, entity))),
                            new Button(new Icon(VaadinIcon.TRASH), _ -> showDeleteDialog(entity))
                    );
                    actions.setWidthFull();
                    actions.setJustifyContentMode(JustifyContentMode.END);
                    actions.setSpacing(true);
                    return actions;
                }))
                .setHeader("Edit/Delete")
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        gridState.getFilterRow().getCell(editDeleteCol).setComponent(createBulkDeleteButton());
    }
    private void showDeleteDialog(T entity) {
        Dialog dialog = new Dialog();
        dialog.add("Are you sure you want to delete this record?");
        dialog.add(new HorizontalLayout(
                getDeleteConfirmationButton(entity, dialog),
                new Button("Cancel", _ -> dialog.close())));
        dialog.open();
    }
    private @NonNull Button getDeleteConfirmationButton(T entity, Dialog dialog) {
        Button confirm = new Button("Delete", _ -> {
            fireEvent(new DeleteEntitiesEvent<>(this, List.of(entity)));
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_WARNING);
        return confirm;
    }

    private Button createBulkDeleteButton() {
        Button bulkDelete = new Button("Delete Selected", new Icon(VaadinIcon.TRASH));
        bulkDelete.addThemeVariants(ButtonVariant.LUMO_WARNING);
        bulkDelete.addClickListener(_ -> showBulkDeleteDialog());
        return bulkDelete;
    }
    private void showBulkDeleteDialog() {
        Set<T> selected = gridState.getGrid().getSelectedItems();
        if (CollectionUtils.isEmpty(selected)) return;

        Dialog dialog = new Dialog();
        dialog.add("Delete " + selected.size() + " selected item" + (selected.size() == 1 ? StringUtils.EMPTY : "s") + "?");
        dialog.add(new HorizontalLayout(
                getBulkDeleteDialogButton(selected, dialog),
                new Button("Cancel", _ -> dialog.close())));
        dialog.open();
    }
    private @NonNull Button getBulkDeleteDialogButton(Set<T> selected, Dialog dialog) {
        Button confirm = new Button("Delete", _ -> {
            fireEvent(new DeleteEntitiesEvent<>(this, new ArrayList<>(selected)));
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_WARNING);
        return confirm;
    }
}
