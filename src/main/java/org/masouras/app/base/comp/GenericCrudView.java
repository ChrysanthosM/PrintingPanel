package org.masouras.app.base.comp;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.masouras.app.base.comp.control.GenericEntityFormContainer;
import org.masouras.app.base.comp.control.GenericEntityGridContainer;
import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RequiredArgsConstructor
public abstract class GenericCrudView<T, ID> extends VerticalLayout {
    private final int pageSize;
    private final Class<T> entityClass;
    private final GenericEntityForm<T, ID> genericEntityForm;
    private final GenericCrudService<T, ID> genericCrudService;

    private GenericEntityGridContainer<T> genericEntityGridContainer;
    private GenericEntityFormContainer<T, ID> genericEntityFormContainer;

    @PostConstruct
    private void init() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        addComponents();
        bindComponents();

        updateList();
    }
    private void addComponents() {
        genericEntityGridContainer = new GenericEntityGridContainer<>(entityClass, pageSize);
        genericEntityFormContainer = new GenericEntityFormContainer<>(genericEntityForm);
        add(new Button(new Icon(VaadinIcon.PLUS_CIRCLE), _ -> addEntity()),
                genericEntityGridContainer,
                genericEntityFormContainer
        );
    }

    private void bindComponents() {
        genericEntityGridContainer.addPageChangeListener(_ -> updateList());
        genericEntityFormContainer.setOnSaveCallback(this::updateList);

        genericEntityGridContainer.addEditListener(e -> editEntity(e.getEntity()));
        genericEntityGridContainer.addDeleteListener(e -> deleteItem(e.getEntity()));
        genericEntityGridContainer.addRefreshListener(_ -> updateList());
    }

    protected void updateList() {
        Page<T> page = genericCrudService.list(PageRequest.of(genericEntityGridContainer.getCurrentPage(), genericEntityGridContainer.getPageSize(), GenericComponentUtils.toSpringSort(genericEntityGridContainer.getCurrentSortOrders())));
        genericEntityGridContainer.setGridItems(page);
    }

    private void addEntity() {
        genericEntityFormContainer.setEntity(newEntityInstance());
    }
    private T newEntityInstance() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create instance of " + entityClass, e);
        }
    }
    private void editEntity(T entity) {
        genericEntityFormContainer.setEntity(entity);
    }
    private void deleteItem(T entity) { genericCrudService.delete(entity); }
}
