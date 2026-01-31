package org.masouras.app.base.element.control;

import org.masouras.app.base.element.component.*;
import org.springframework.stereotype.Component;

@Component
public class GenericContainerFactory {

    /**
     * Creates a unified grid container in ENTITY mode
     * (pagination + CRUD + server‑side filtering).
     */
    public <T> GenericGridContainer<T> createEntityGrid(Class<T> entityClass, int pageSize) {
        return new GenericGridContainer<>(entityClass, GenericGridState.GridMode.ENTITY_MODE, pageSize);
    }

    /**
     * Creates a unified grid container in DTO mode
     * (no pagination + in‑memory filtering + no CRUD).
     */
    public <T> GenericGridContainer<T> createDtoGrid(Class<T> dtoClass) {
        return new GenericGridContainer<>(dtoClass, GenericGridState.GridMode.DTO_MODE, 0);
    }

    /**
     * Your form container stays separate because it is not part of the grid unification.
     */
    public <T, ID> GenericEntityFormContainer<T, ID> createGenericEntityFormContainer(GenericEntityForm<T, ID> form) {
        return new GenericEntityFormContainer<>(form);
    }
}
