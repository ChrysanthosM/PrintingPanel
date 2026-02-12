package org.masouras.app.base.element.control;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.masouras.app.base.element.component.GenericEntityFormContainer;
import org.masouras.app.base.element.component.GenericGridContainer;
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

    @Autowired private GenericContainerFactory genericContainerFactory;
    @Autowired private GenericCrudServiceFactory genericCrudServiceFactory;
    @Autowired private GenericEntityFormFactory genericEntityFormFactory;

    private GenericCrudService<T, ID> genericCrudService;

    private GenericGridContainer<T> genericGridContainer;
    private GenericEntityFormContainer<T, ID> genericEntityFormContainer;


    @PostConstruct
    private void init() {
        genericCrudService = genericCrudServiceFactory.getGenericCrudService(entityClass);
        genericGridContainer = genericContainerFactory.createEntityGrid(entityClass, pageSize);
        genericEntityFormContainer = genericContainerFactory.createGenericEntityFormContainer(genericEntityFormFactory.getGenericEntityForm(entityClass, genericCrudService));
        initMain();
    }
    private void initMain() {
        setStyle();

        addComponents();
        bindComponents();

        updateList();
    }

    private void setStyle() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    private void addComponents() {
        add(new H2(title), genericGridContainer, genericEntityFormContainer);
    }

    private void bindComponents() {
        genericGridContainer.addPageChangeListener(_ -> updateList());
        genericEntityFormContainer.setOnSaveCallback(this::updateList);
        genericGridContainer.addRefreshListener(_ -> updateList());

        genericGridContainer.addAddEntityListener(_ -> addEntity());
        genericGridContainer.addEditEntityListener(e -> editEntity(e.getEntity()));
        genericGridContainer.addDeleteEntitiesListener(e -> deleteItems(e.getEntities()));
    }

    private void updateList() {
        Page<T> page = genericCrudService.findAll(PageRequest.of(genericGridContainer.getCurrentPage(), genericGridContainer.getPageSize()),
                VaadinSpringBridge.buildSpecification(entityClass, genericGridContainer.getFilterValues(), genericGridContainer.getGridState().getCurrentSortOrders()));
        genericGridContainer.setGridItems(page);
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
