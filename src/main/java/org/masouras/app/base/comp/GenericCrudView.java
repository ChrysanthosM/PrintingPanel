package org.masouras.app.base.comp;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.Span;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final Map<Grid.Column<T>, Component> columnFilters = new HashMap<>();
    private List<GridSortOrder<T>> currentSortOrders = new ArrayList<>();

    private int currentPage, totalItems, totalPages;
    private final int pageSize = 10;
    private Span paginationInfo;
    private Button firstBtn, prevBtn, nextBtn, lastBtn;

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
        add(new Button(new Icon(VaadinIcon.PLUS_CIRCLE), e -> addEntity()),
                buildGridContainer(),
                getFormLayout()
        );
    }
    private Component getFormLayout() {
        VerticalLayout layout = new VerticalLayout(form);
        layout.setSizeFull();
        return layout;
    }
    private VerticalLayout buildGridContainer() {
        VerticalLayout gridContainer = new VerticalLayout();
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.setWidthFull();
        gridContainer.add(grid);
        gridContainer.add(buildPaginationBar());
        return gridContainer;
    }
    private HorizontalLayout buildPaginationBar() {
        this.firstBtn = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT), _ -> goToPage(0));
        this.prevBtn = new Button(new Icon(VaadinIcon.ANGLE_LEFT),  _ -> goToPage(currentPage - 1));
        this.nextBtn = new Button(new Icon(VaadinIcon.ANGLE_RIGHT),  _ -> goToPage(currentPage + 1));
        this.lastBtn = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT), _ -> goToPage(totalPages - 1));

        this.firstBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        this.prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        this.nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        this.lastBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        this.paginationInfo = new Span();

        HorizontalLayout bar = new HorizontalLayout(this.firstBtn, this.prevBtn, this.paginationInfo, this.nextBtn, this.lastBtn);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setJustifyContentMode(JustifyContentMode.CENTER);
        return bar;
    }
    private void goToPage(int page) {
        if (page < 0 || page >= totalPages) return;
        currentPage = page;
        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setEmptyStateText("No items found");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.asSingleSelect().addValueChangeListener(e -> editEntity(e.getValue()));
        grid.setMultiSort(true);
        grid.addSortListener(e -> {
            currentSortOrders = e.getSortOrder();
            updateList();
        });
        filterRow = grid.appendHeaderRow();

        addGridColumns(grid);
        addClearAllFiltersButton();

        grid.addColumn(new ComponentRenderer<>(entity -> {
                    HorizontalLayout actions = new HorizontalLayout(
                            new Button(new Icon(VaadinIcon.EDIT), _ -> editEntity(entity)),
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
                new Button("Cancel", _ -> dialog.close())));
        dialog.open();
    }
    private @NonNull Button getDeleteConfirmationButton(T entity, Dialog dialog) {
        Button confirm = new Button("Delete", _ -> {
            deleteItem(entity);
            updateList();
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_WARNING);
        return confirm;
    }

    private void deleteItem(T entity) { genericCrudService.delete(entity); }

    protected void updateList() {
        Sort sort = toSpringSort(currentSortOrders);
        Page<T> page = genericCrudService.list(PageRequest.of(currentPage, pageSize, sort));
        totalItems = (int) page.getTotalElements();
        totalPages = page.getTotalPages();
        allItems = page.getContent();
        grid.setItems(allItems);
        updatePaginationBar();
    }
    private void updatePaginationBar() {
        int start = currentPage * pageSize + 1;
        int end = Math.min((currentPage + 1) * pageSize, totalItems);

        paginationInfo.setText("Rows: " + start + "–" + end + " of " + totalItems);
        firstBtn.setEnabled(currentPage > 0);
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(currentPage < totalPages - 1);
        lastBtn.setEnabled(currentPage < totalPages - 1);
    }
    private Sort toSpringSort(List<GridSortOrder<T>> orders) {
        return Sort.by(orders.stream()
                .map(o -> new Sort.Order(
                        o.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC,
                        o.getSorted().getKey()
                ))
                .toList()
        );
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
