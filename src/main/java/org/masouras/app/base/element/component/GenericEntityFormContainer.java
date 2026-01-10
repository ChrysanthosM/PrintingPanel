package org.masouras.app.base.element.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.masouras.app.base.element.control.GenericEntityForm;

public final class GenericEntityFormContainer<T, ID> extends VerticalLayout {
    private final GenericEntityForm<T, ID> genericEntityForm;

    public GenericEntityFormContainer(GenericEntityForm<T, ID> genericEntityForm) {
        this.genericEntityForm = genericEntityForm;
        init();
    }
    private void init() {
        add(this.genericEntityForm);
        setSizeFull();
    }

    public void setOnSaveCallback(Runnable onSaveCallback) {
        genericEntityForm.setOnSaveCallback(onSaveCallback);
    }
    public void setEntity(T entity) {
        genericEntityForm.setEntity(entity);
    }
}
