package org.masouras.app.base.comp;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EmbeddedId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudService;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.*;

@RequiredArgsConstructor
public abstract class GenericCrudView<T, ID> extends VerticalLayout {
    private final Class<T> entityClass;
    private final GenericEntityForm<T, ID> form;
    private final GenericCrudService<T, ID> genericCrudService;

    private final Grid<T> grid = new Grid<>();
    private List<T> allItems;
    private HeaderRow filterRow;
    private final Map<Grid.Column<T>, String> columnProperties = new HashMap<>();
    private final Map<Grid.Column<T>, TextField> columnFilters = new HashMap<>();

    @PostConstruct
    private void init() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGrid();
        configureForm();

        addComponents();

        updateList();
    }

    private void addComponents() {
        add(new Button(new Icon(VaadinIcon.PLUS_CIRCLE), e -> addEntity()), getFormLayout());
    }

    private Component getFormLayout() {
        VerticalLayout layout = new VerticalLayout(grid, form);
        layout.setFlexGrow(2, grid);
        layout.setFlexGrow(1, form);
        layout.setSizeFull();
        return layout;
    }
    private void configureGrid() {
        grid.setSizeFull();
        grid.setEmptyStateText("No items found");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.asSingleSelect().addValueChangeListener(e -> editEntity(e.getValue()));
        grid.setMultiSort(true);
        this.filterRow = grid.appendHeaderRow();

        addGridColumns(grid);
        grid.addColumn(new ComponentRenderer<>(entity -> new HorizontalLayout(
                new Button(new Icon(VaadinIcon.EDIT), e -> editEntity(entity)),
                new Button(new Icon(VaadinIcon.TRASH), e -> showDeleteDialog(entity)))))
                .setHeader("Actions").setAutoWidth(true);
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

                                Grid.Column<T> col = grid.addColumn(entity -> {
                                            try {
                                                Object embedded = embeddedField.get(entity);
                                                return embedded == null ? StringUtils.EMPTY : subField.get(embedded);
                                            } catch (Exception e) {
                                                return StringUtils.EMPTY;
                                            }
                                        })
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

                    Grid.Column<T> col = grid.addColumn(entity -> {
                                try {
                                    return field.get(entity);
                                } catch (IllegalAccessException e) {
                                    return StringUtils.EMPTY;
                                }
                            })
                            .setHeader(formField.label())
                            .setSortable(true)
                            .setKey(field.getName());
                    addFilterForColumn(col, field.getName());
                });
    }

    private void addFilterForColumn(Grid.Column<T> col, String property) {
        TextField filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setClearButtonVisible(true);
        filter.setWidthFull();
        filter.addValueChangeListener(e -> applyColumnFilters());
        columnFilters.put(col, filter);
        columnProperties.put(col, property);
        filterRow.getCell(col).setComponent(filter);
    }

    private void applyColumnFilters() {
        List<T> filtered = allItems.stream()
                .filter(item -> columnFilters.entrySet().stream().allMatch(entry -> {
                    String filterText = entry.getValue().getValue();
                    if (StringUtils.isBlank(filterText)) return true;

                    String property = columnProperties.get(entry.getKey());
                    Object value = getNestedPropertyValue(item, property);
                    return value != null && value.toString().toLowerCase().contains(filterText.toLowerCase());
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
            }, (a, b) -> b);
        } catch (Exception e) {
            return null;
        }
    }

    private void configureForm() {
        form.setVisible(false);
        form.setOnSaveCallback(this::updateList);
    }

    private List<T> fetchItems() { return genericCrudService.findAll(); }

    private void showDeleteDialog(T entity) {
        Dialog dialog = new Dialog();
        dialog.add("Are you sure you want to delete this record?");
        dialog.add(new HorizontalLayout(
                getDeleteConfirmationButton(entity, dialog),
                new Button("Cancel", event -> dialog.close())));
        dialog.open();
    }
    private @NonNull Button getDeleteConfirmationButton(T entity, Dialog dialog) {
        Button confirm = new Button("Delete", event -> {
            deleteItem(entity);
            updateList();
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_WARNING);
        return confirm;
    }

    private void deleteItem(T entity) { genericCrudService.delete(entity); }

    protected void updateList() {
        allItems = fetchItems();

        grid.setItems(query -> {
            int offset = query.getOffset();
            int limit = query.getLimit();
            List<Sort.Order> orders = query.getSortOrders().stream()
                    .map(sortOrder -> {
                        String property = sortOrder.getSorted();
                        Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                        return new Sort.Order(direction, property);
                    })
                    .toList();
            Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(orders));
            return genericCrudService.list(pageable).stream();
        });
    }


    private void addEntity() {
        form.setEntity(newEntityInstance());
    }
    private T newEntityInstance() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create instance of " + entityClass, e);
        }
    }

    private void editEntity(T entity) {
        form.setEntity(entity);
    }
}
