package org.masouras.app.base.comp.control;

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
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.util.*;

public class GenericEntityGridContainer<T, ID> extends VerticalLayout {
    public static class EditEvent<T> extends ComponentEvent<GenericEntityGridContainer<T, ?>> {
        @Getter private final T entity;
        public EditEvent(GenericEntityGridContainer<T, ?> source, T entity) {
            super(source, false);
            this.entity = entity;
        }
    }
    public static class DeleteEvent<T> extends ComponentEvent<GenericEntityGridContainer<T, ?>> {
        @Getter private final T entity;
        public DeleteEvent(GenericEntityGridContainer<T, ?> source, T entity) {
            super(source, false);
            this.entity = entity;
        }
    }
    public static class RefreshEvent<T> extends ComponentEvent<GenericEntityGridContainer<T, ?>> {
        public RefreshEvent(GenericEntityGridContainer<T, ?> source) {
            super(source, false);
        }
    }


    private final Class<T> entityClass;
    private final PaginationBar paginationBar;

    private final Grid<T> grid = new Grid<>();;
    private List<T> allItems;
    private HeaderRow filterRow;
    private final Map<Grid.Column<T>, String> columnProperties = new HashMap<>();
    private final Map<Grid.Column<T>, Component> columnFilters = new HashMap<>();
    @Getter private List<GridSortOrder<T>> currentSortOrders = new ArrayList<>();

    public GenericEntityGridContainer(Class<T> entityClass, int pageSize) {
        this.entityClass = entityClass;
        this.paginationBar = new PaginationBar(pageSize);
        init();
    }
    private void init() {
        configureGrid();
        add(grid, paginationBar);
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
        this.allItems = page.getContent();
        grid.setItems(this.allItems);
        if (CollectionUtils.isNotEmpty(currentSortOrders)) grid.sort(currentSortOrders);
        paginationBar.updatePaginationBar((int) page.getTotalElements(), page.getTotalPages());
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setEmptyStateText("No items found");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.asSingleSelect().addValueChangeListener(e -> fireEvent(new EditEvent<>(this, e.getValue())));
        grid.setMultiSort(true);
        grid.addSortListener(e -> {
            currentSortOrders = e.getSortOrder();
            fireEvent(new RefreshEvent<>(this));
        });
        filterRow = grid.appendHeaderRow();

        addGridColumns(grid);
        addClearAllFiltersButton();

        grid.addColumn(new ComponentRenderer<>(entity -> {
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
        Grid.Column<T> clearCol = grid.addColumn(_ -> StringUtils.EMPTY)
                .setHeader(StringUtils.EMPTY)
                .setAutoWidth(true)
                .setFlexGrow(0);
        Button clearBtn = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE), _ -> clearAllFilters());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        clearBtn.getElement().setProperty("title", "Clear all filters");
        filterRow.getCell(clearCol).setComponent(clearBtn);
    }
    private void clearAllFilters() {
        columnFilters.values().forEach(component -> {
            if (component instanceof TextField textField) textField.clear();
            if (component instanceof ComboBox<?> comboBox) comboBox.clear();
        });
        List<GridSortOrder<T>> sortOrders = grid.getSortOrder();
        grid.setItems(allItems);
        grid.sort(sortOrders);
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
                                Grid.Column<T> col = grid.addColumn(entity -> getEmbeddedFieldValue(embeddedField, subField, entity))
                                        .setHeader(subField.getName())
                                        .setSortable(true)
                                        .setKey(propertyPath);
                                addFilterForColumn(col, propertyPath);
                            });
                });
    }
    private Object getEmbeddedFieldValue(Field embeddedField, Field subField, T entity) {
        try {
            Object embedded = embeddedField.get(entity);
            return embedded == null ? StringUtils.EMPTY : subField.get(embedded);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }
    private void addGridColumnsAttributes(Grid<T> grid) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(field -> {
                    field.setAccessible(true);
                    FormField formField = field.getAnnotation(FormField.class);
                    Grid.Column<T> col = grid.addColumn(entity -> getFieldValue(field, entity))
                            .setHeader(formField.label())
                            .setSortable(true)
                            .setKey(field.getName());
                    addFilterForColumn(col, field.getName());
                });
    }
    private Object getFieldValue(Field field, T entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            return StringUtils.EMPTY;
        }
    }

    private void addFilterForColumn(Grid.Column<T> col, String property) {
        Field field = resolveField(property);
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

        columnFilters.put(col, filterComponent);
        columnProperties.put(col, property);
        filterRow.getCell(col).setComponent(filterComponent);
    }
    private Field resolveField(String propertyPath) {
        try {
            String[] parts = propertyPath.split("\\.");
            Class<?> currentClass = entityClass;
            Field field = null;
            for (String part : parts) {
                field = currentClass.getDeclaredField(part);
                field.setAccessible(true);
                currentClass = field.getType();
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private void applyColumnFilters() {
        List<T> filtered = allItems.stream()
                .filter(item -> columnFilters.entrySet().stream().allMatch(entry -> {
                    Component comp = entry.getValue();
                    String property = columnProperties.get(entry.getKey());
                    Object value = getNestedPropertyValue(item, property);

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

        grid.setItems(filtered);
    }
    @SuppressWarnings("unchecked")
    private Object getNestedPropertyValue(T item, String propertyPath) {
        try {
            return Arrays.stream(propertyPath.split("\\.")).reduce(item, (current, part) -> {
                if (current == null) return null;
                try {
                    Field field = current.getClass().getDeclaredField(part);
                    field.setAccessible(true);
                    return (T) field.get(current);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }, (_, b) -> b);
        } catch (Exception e) {
            return null;
        }
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
