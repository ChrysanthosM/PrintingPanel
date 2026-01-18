package org.masouras.app.base.element.control;

public sealed interface GenericEntityFormStrategy<T, ID> permits GenericEntityForm {
    Class<T> getEntityClass();
    GenericEntityForm<T, ID> getGenericEntityForm();
}
