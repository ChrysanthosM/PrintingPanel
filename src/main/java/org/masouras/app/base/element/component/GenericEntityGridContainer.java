package org.masouras.app.base.element.component;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.*;
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
    public static class EditEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        @Getter private final T entity;
        public EditEvent(GenericEntityGridContainer<T> source, T entity) {
            super(source, false);
            this.entity = entity;
        }
    }
    public static class DeleteEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        @Getter private final T entity;
        public DeleteEvent(GenericEntityGridContainer<T> source, T entity) {
            super(source, false);
            this.entity = entity;
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
        add(gridState.getGrid(), this.paginationBar);
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }
    public void addEditListener(ComponentEventListener<EditEvent<T>> listener) { addListener(EditEvent.class, (ComponentEventListener) listener); }
    public void addDeleteListener(ComponentEventListener<DeleteEvent<T>> listener) { addListener(DeleteEvent.class, (ComponentEventListener) listener); }
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
        gridState.getGrid().setSizeFull();
        gridState.getGrid().setEmptyStateText("No items found");
        gridState.getGrid().addThemeVariants(GridVariant.LUMO_NO_BORDER);
        gridState.getGrid().asSingleSelect().addValueChangeListener(e -> fireEvent(new EditEvent<>(this, e.getValue())));
        gridState.getGrid().setMultiSort(true);
        gridState.getGrid().addSortListener(e -> {
            gridState.setCurrentSortOrders(e.getSortOrder());
            fireEvent(new RefreshEvent<>(this));
        });

        addGridColumns(gridState.getGrid());
        addClearAllFiltersButton();

        gridState.getGrid().addColumn(new ComponentRenderer<>(entity -> {
                    HorizontalLayout actions = new HorizontalLayout(
                            new Button(new Icon(VaadinIcon.EDIT), _ -> fireEvent(new EditEvent<>(this, entity))),
                            new Button(new Icon(VaadinIcon.TRASH), _ -> showDeleteDialog(entity))
                    );
                    actions.setWidthFull();
                    actions.setJustifyContentMode(JustifyContentMode.END);
                    actions.setSpacing(true);
                    return actions;
                }))
                .setHeader("Edit/Delete Row").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
    }
    private void addClearAllFiltersButton() {
        Grid.Column<T> clearCol = gridState.getGrid().addColumn(_ -> StringUtils.EMPTY)
                .setHeader(StringUtils.EMPTY)
                .setAutoWidth(true)
                .setFlexGrow(0);
        Button clearBtn = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE), _ -> clearAllFilters());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        clearBtn.getElement().setProperty("title", "Clear all filters");
        gridState.getFilterRow().getCell(clearCol).setComponent(clearBtn);
    }
    private void clearAllFilters() {
        gridState.getColumnFilters().values().forEach(component -> {
            if (component instanceof TextField textField) textField.clear();
            if (component instanceof ComboBox<?> comboBox) comboBox.clear();
        });
        List<GridSortOrder<T>> sortOrders = gridState.getGrid().getSortOrder();
        gridState.getGrid().setItems(gridState.getAllItems());
        gridState.getGrid().sort(sortOrders);
    }
    private void addGridColumns(Grid<T> grid) {
        addGridColumnsEmbeddedIds(grid);
        addGridColumnsAttributes(grid);
    }
    private void addGridColumnsEmbeddedIds(Grid<T> grid) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EmbeddedId.class))
                .forEach(embeddedField -> {
                    embeddedField.setAccessible(true);
                    Arrays.stream(embeddedField.getType().getDeclaredFields())
                            .forEach(subField -> {
                                subField.setAccessible(true);
                                String propertyPath = embeddedField.getName() + "." + subField.getName();
                                Grid.Column<T> col = grid.addColumn(entity -> GenericComponentUtils.getEmbeddedFieldValueOr(embeddedField, subField, entity, StringUtils.EMPTY))
                                        .setHeader(subField.getName())
                                        .setSortable(true)
                                        .setKey(propertyPath);
                                addFilterForColumn(col, propertyPath);
                            });
                });
    }
    private void addGridColumnsAttributes(Grid<T> grid) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(field -> {
                    field.setAccessible(true);
                    FormField formField = field.getAnnotation(FormField.class);
                    Grid.Column<T> col = grid.addColumn(entity -> GenericComponentUtils.getFieldValueOr(field, entity, StringUtils.EMPTY))
                            .setHeader(formField.label())
                            .setSortable(true)
                            .setKey(field.getName());
                    addFilterForColumn(col, field.getName());
                });
    }

    private void addFilterForColumn(Grid.Column<T> col, String property) {
        Field field = GenericComponentUtils.resolveField(entityClass, property);
        if (field == null) return;

        Component filterComponent;
        if (field.getType().isEnum()) {
            ComboBox<Object> combo = new ComboBox<>();
            combo.setItems(field.getType().getEnumConstants());
            combo.setPlaceholder("Filter");
            combo.setClearButtonVisible(true);
            combo.setWidthFull();
            combo.addValueChangeListener(_ -> applyColumnFilters());
            filterComponent = combo;
        } else {
            TextField filter = new TextField();
            filter.setPlaceholder("Filter");
            filter.setClearButtonVisible(true);
            filter.setWidthFull();
            filter.addValueChangeListener(_ -> applyColumnFilters());
            filterComponent = filter;
        }

        gridState.getColumnFilters().put(col, filterComponent);
        gridState.getColumnProperties().put(col, property);
        gridState.getFilterRow().getCell(col).setComponent(filterComponent);
    }

    private void applyColumnFilters() {
        List<T> filtered = gridState.getAllItems().stream()
                .filter(item -> gridState.getColumnFilters().entrySet().stream().allMatch(entry -> {
                    com.vaadin.flow.component.Component comp = entry.getValue();
                    String property = gridState.getColumnProperties().get(entry.getKey());
                    Object value = GenericComponentUtils.getNestedPropertyValue(item, property);

                    if (comp instanceof TextField tf) {
                        String filterText = tf.getValue();
                        if (StringUtils.isBlank(filterText)) return true;
                        return value != null && value.toString().toLowerCase().contains(filterText.toLowerCase());
                    }
                    if (comp instanceof ComboBox<?> cb) {
                        Object selected = cb.getValue();
                        if (selected == null) return true;
                        return value != null && value.equals(selected);
                    }
                    return true;
                }))
                .toList();

        gridState.getGrid().setItems(filtered);
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
            fireEvent(new DeleteEvent<>(this, entity));;
            fireEvent(new RefreshEvent<>(this));
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_WARNING);
        return confirm;
    }
}
