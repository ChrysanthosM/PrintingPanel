package org.masouras.app.base.comp;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudService;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public abstract class GenericCrudView<T, ID> extends VerticalLayout {
    private final Class<T> entityClass;
    private final GenericEntityForm<T, ID> form;
    private final GenericCrudService<T, ID> genericCrudService;

    private final Grid<T> grid = new Grid<>();
    private List<T> allItems;

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

        TextField search = new TextField(e -> applyFilter(e.getValue()));
        search.setPlaceholder("Search...");
        search.setClearButtonVisible(true);
        search.setWidthFull();
        add(search);
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
        addGridColumns(grid);
        grid.addColumn(new ComponentRenderer<>(entity -> new HorizontalLayout(
                new Button(new Icon(VaadinIcon.EDIT), e -> editEntity(entity)),
                new Button(new Icon(VaadinIcon.TRASH), e -> showDeleteDialog(entity)))))
                .setHeader("Actions").setAutoWidth(true);
    }
    private void addGridColumns(Grid<T> grid) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FormField.class).order()))
                .forEach(field -> {
                    field.setAccessible(true);
                    FormField formField = field.getAnnotation(FormField.class);
                    grid.addColumn(entity -> {
                                try {
                                    return field.get(entity);
                                } catch (IllegalAccessException e) {
                                    return StringUtils.EMPTY;
                                }
                            })
                            .setHeader(formField.label())
                            .setSortable(true);
                });
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
            return genericCrudService.list(PageRequest.of(offset / limit, limit)).stream();
        });
    }
    private void applyFilter(String filterText) {
        if (StringUtils.isBlank(filterText)) {
            grid.setItems(allItems);
            return;
        }
        grid.setItems(allItems.stream()
                .filter(item -> matchesFilter(item, filterText.toLowerCase()))
                .toList());
    }
    private boolean matchesFilter(T item, String filter) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(FormField.class))
                .anyMatch(field -> {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(item);
                        return value != null && value.toString().toLowerCase().contains(filter);
                    } catch (Exception ignored) {
                        return false;
                    }
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
