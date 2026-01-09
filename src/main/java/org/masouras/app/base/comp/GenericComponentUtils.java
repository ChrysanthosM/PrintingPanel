package org.masouras.app.base.comp;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.List;

@UtilityClass
public class GenericComponentUtils {

    public static <T> Sort toSpringSort(List<GridSortOrder<T>> orders) {
        return Sort.by(orders.stream()
                .map(order -> new Sort.Order(
                        order.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC,
                        order.getSorted().getKey()
                ))
                .toList()
        );
    }

    public static Object getEmbeddedFieldValueOr(Field embeddedField, Field subField, Object entity,
                                                 Object defaultValue) {
        try {
            Object embedded = embeddedField.get(entity);
            return embedded == null ? defaultValue : subField.get(embedded);
        } catch (IllegalAccessException e) {
            return defaultValue;
        }
    }

    public static Object getFieldValueOr(Field field, Object entity,
                                         Object defaultValue) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            return defaultValue;
        }
    }

    public static Field resolveField(Class<?> rootClass, String propertyPath) {
        try {
            String[] parts = propertyPath.split("\\.");
            Class<?> currentClass = rootClass;
            Field field = null;
            for (String part : parts) {
                field = currentClass.getDeclaredField(part);
                field.setAccessible(true);
                currentClass = field.getType();
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static Object getNestedPropertyValue(Object rootObject, String propertyPath) {
        if (rootObject == null || propertyPath == null) return null;
        try {
            Object current = rootObject;
            for (String part : propertyPath.split("\\.")) {
                if (current == null) return null;
                Field field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);
            }
            return current;
        } catch (Exception e) {
            return null;
        }
    }

}
