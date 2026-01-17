package org.masouras.app.base.element.control;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.masouras.app.base.element.component.GenericEntityFormContainer;
import org.masouras.app.base.element.component.GenericEntityGridContainer;
import org.masouras.app.base.element.util.VaadinSpringBridge;
import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericCrudView<T, ID> extends VerticalLayout {
    private final String title;
    private final int pageSize;
    private final Class<T> entityClass;
    private final GenericCrudService<T, ID> genericCrudService;
    private final GenericEntityForm<T, ID> genericEntityForm;

    @Autowired private GenericContainerFactory genericContainerFactory;
    private GenericEntityGridContainer<T> genericEntityGridContainer;
    private GenericEntityFormContainer<T, ID> genericEntityFormContainer;

    @PostConstruct
    private void init() {
        genericEntityGridContainer = genericContainerFactory.createGenericEntityGridContainer(entityClass, pageSize);
        genericEntityFormContainer = genericContainerFactory.createGenericEntityFormContainer(genericEntityForm);
        initMain();
    }

    private void initMain() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        addComponents();
        bindComponents();

        updateList();
    }

    private void addComponents() {
        add(new H2(title), genericEntityGridContainer, genericEntityFormContainer);
    }

    private void bindComponents() {
        genericEntityGridContainer.addPageChangeListener(_ -> updateList());
        genericEntityFormContainer.setOnSaveCallback(this::updateList);
        genericEntityGridContainer.addRefreshGridEntitiesListener(_ -> updateList());

        genericEntityGridContainer.addAddEntityListener(_ -> addEntity());
        genericEntityGridContainer.addEditEntityListener(e -> editEntity(e.getEntity()));
        genericEntityGridContainer.addDeleteEntitiesListener(e -> deleteItems(e.getEntities()));
    }

    private void updateList() {
        Page<T> page = genericCrudService.findAll(PageRequest.of(genericEntityGridContainer.getCurrentPage(), genericEntityGridContainer.getPageSize()),
                VaadinSpringBridge.buildSpecification(entityClass, genericEntityGridContainer.getFilterValues(), genericEntityGridContainer.getGridState().getCurrentSortOrders()));
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
    private void deleteItems(List<T> entities) {
        genericCrudService.deleteAll(entities);
        updateList();
    }
}
