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

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericCrudView<T> extends VerticalLayout {
    protected final Grid<T> grid = new Grid<>();
    protected final GenericEntityForm<T> form;

    @PostConstruct
    private void init() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGrid();
        configureForm();

        add(new Button(new Icon(VaadinIcon.FILE_ADD), e -> addEntity()), getFormLayout());
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

    private void configureForm() {
        form.setVisible(false);
    }

    protected abstract void addGridColumns(Grid<T> grid);

    protected abstract List<T> fetchItems();

    protected abstract void deleteItem(T entity);

    protected abstract T createNewEntity();

    protected void updateList() {
        grid.setItems(fetchItems());
    }

    private void addEntity() {
        form.setEntity(createNewEntity());
    }

    private void editEntity(T entity) {
        form.setEntity(entity);
    }
}
