package org.masouras.app.base.comp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudService;

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericCrudView<T, ID> extends VerticalLayout {
    private final Class<T> entityClass;
    private final GenericEntityForm<T, ID> form;
    private final GenericCrudService<T, ID> genericCrudService;

    private final Grid<T> grid = new Grid<>();

    @PostConstruct
    private void init() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGrid();
        configureForm();

        add(new Button(new Icon(VaadinIcon.PLUS_CIRCLE), e -> addEntity()), getFormLayout());
        updateList();
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
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.asSingleSelect().addValueChangeListener(e -> editEntity(e.getValue()));
        addGridColumns(grid);
        grid.addColumn(new ComponentRenderer<>(entity -> new HorizontalLayout(
                new Button(new Icon(VaadinIcon.EDIT), e -> editEntity(entity)),
                new Button(new Icon(VaadinIcon.TRASH), e -> {
                    deleteItem(entity);
                    updateList();
                })))).setHeader("Actions").setAutoWidth(true);
    }
    protected abstract void addGridColumns(Grid<T> grid);


    private void configureForm() {
        form.setVisible(false);
        form.setOnSaveCallback(this::updateList);
    }

    private List<T> fetchItems() { return genericCrudService.findAll(); }

    private void deleteItem(T entity) { genericCrudService.delete(entity); }

    protected void updateList() {
        grid.setItems(fetchItems());
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
