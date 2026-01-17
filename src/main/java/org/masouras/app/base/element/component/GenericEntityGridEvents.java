package org.masouras.app.base.element.component;


import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class GenericEntityGridEvents {
    public static class AddEntityEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        public AddEntityEvent(GenericEntityGridContainer<T> source) {
            super(source, false);
        }
    }

    public static class EditEntityEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        @Getter private final T entity;
        public EditEntityEvent(GenericEntityGridContainer<T> source, T entity) {
            super(source, false);
            this.entity = entity;
        }
    }

    public static class DeleteEntitiesEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        @Getter private final List<T> entities;
        public DeleteEntitiesEvent(GenericEntityGridContainer<T> source, List<T> entities) {
            super(source, false);
            this.entities = entities;
        }
    }

    public static class RefreshGridEntitiesEvent<T> extends ComponentEvent<GenericEntityGridContainer<T>> {
        public RefreshGridEntitiesEvent(GenericEntityGridContainer<T> source) {
            super(source, false);
        }
    }
}
