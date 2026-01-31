package org.masouras.app.base.element.component;


import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class GenericGridEvents {
    public static class AddEntityEvent<T> extends ComponentEvent<GenericGridContainer<T>> {
        public AddEntityEvent(GenericGridContainer<T> source) {
            super(source, false);
        }
    }

    public static class EditEntityEvent<T> extends ComponentEvent<GenericGridContainer<T>> {
        @Getter private final T entity;
        public EditEntityEvent(GenericGridContainer<T> source, T entity) {
            super(source, false);
            this.entity = entity;
        }
    }

    public static class DeleteEntitiesEvent<T> extends ComponentEvent<GenericGridContainer<T>> {
        @Getter private final List<T> entities;
        public DeleteEntitiesEvent(GenericGridContainer<T> source, List<T> entities) {
            super(source, false);
            this.entities = entities;
        }
    }

    public static class RefreshGridEvent<T> extends ComponentEvent<GenericGridContainer<T>> {
        public RefreshGridEvent(GenericGridContainer<T> source) {
            super(source, false);
        }
    }
}
