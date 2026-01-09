package org.masouras.app.base.comp.control;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.masouras.app.base.comp.GenericEntityForm;

public class GenericEntityFormContainer<T, ID> extends VerticalLayout {
    private final GenericEntityForm<T, ID> genericEntityForm;

    public GenericEntityFormContainer(GenericEntityForm<T, ID> genericEntityForm) {
        this.genericEntityForm = genericEntityForm;
        init();
    }
    private void init() {
        add(genericEntityForm);
        setSizeFull();
    }

    public void setOnSaveCallback(Runnable onSaveCallback) {
        genericEntityForm.setOnSaveCallback(onSaveCallback);
    }

    public void setEntity(T entity) {
        genericEntityForm.setEntity(entity);
    }
}
