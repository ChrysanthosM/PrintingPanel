package org.masouras.app.base.element.control;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.masouras.app.base.element.component.GenericDtoGridContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericDtoView<T> extends VerticalLayout {
    private final String title;
    private final Class<T> dtoClass;

    @Autowired
    private GenericContainerFactory genericContainerFactory;

    private GenericDtoGridContainer<T> genericDtoGridContainer;

    @PostConstruct
    private void init() {
        genericDtoGridContainer = genericContainerFactory.createGenericDtoGridContainer(dtoClass);
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
        add(new H2(title), genericDtoGridContainer);
    }

    private void bindComponents() {
        genericDtoGridContainer.addRefreshListener(_ -> updateList());
    }

    private void updateList() {
        genericDtoGridContainer.setGridItems(loadAllItems());
    }
    protected abstract List<T> loadAllItems();
}
