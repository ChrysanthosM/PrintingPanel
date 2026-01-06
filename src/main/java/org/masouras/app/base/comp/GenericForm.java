package org.masouras.app.base.comp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public abstract class GenericForm<T> extends FormLayout {
    protected final Runnable onChange;
    protected T entity;

    public GenericForm(Runnable onChange) {
        this.onChange = onChange;
        init();
    }
    private void init() {
        loadComponents();
        add(getFormComponents());
        add(new HorizontalLayout(
                new Button(new Icon(VaadinIcon.DISC), e -> saveEntity()),
                new Button("Cancel", e -> setEntity(null))
        ));
    }

    public void setEntity(T entity) {
        this.entity = entity;
        if (entity == null) {
            setVisible(false);
            clearFields();
            return;
        }
        populateFields(entity);
        setVisible(true);
    }

    protected abstract Component[] getFormComponents();
    protected abstract void loadComponents();
    protected abstract void populateFields(T entity);
    protected abstract void clearFields();
    protected abstract void saveEntity();
}
