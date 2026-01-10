package org.masouras.app.base.element.control;

import org.masouras.app.base.element.component.GenericEntityFormContainer;
import org.masouras.app.base.element.component.GenericEntityGridContainer;
import org.springframework.stereotype.Component;

@Component
public class GenericContainerFactory {
    public <T> GenericEntityGridContainer<T> createGenericEntityGridContainer(Class<T> entityClass, int pageSize) {
        return new GenericEntityGridContainer<>(entityClass, pageSize);
    }
    public <T, ID> GenericEntityFormContainer<T, ID> createGenericEntityFormContainer(GenericEntityForm<T, ID> form) {
        return new GenericEntityFormContainer<>(form);
    }
}
