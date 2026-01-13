package org.masouras.app.base.element.util;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import lombok.experimental.UtilityClass;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@UtilityClass
public class VaadinGridUtils {

    public static <T> void createGridColumn(Grid<T> grid, Field field, String propertyPath,
                                            BiFunction<T, Field, Object> valueExtractor, BiConsumer<Grid.Column<T>, String> filterConsumer) {
        field.setAccessible(true);
        FormField formField = field.getAnnotation(FormField.class);
        Grid.Column<T> col = grid.addColumn(entity -> {
                    Object value = valueExtractor.apply(entity, field);
                    return value != null ? value : StringUtils.EMPTY;
                })
                .setHeader((formField != null && StringUtils.isNotBlank(formField.label())) ? formField.label() : field.getName())
                .setSortable(true)
                .setKey(propertyPath);
        filterConsumer.accept(col, propertyPath);
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

    public static Component createFilterComponent(Field field, HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<?, ?>> listener) {
        Component filterComponent;
        if (field.getType().isEnum()) {
            ComboBox<Object> combo = new ComboBox<>();
            combo.setItems(field.getType().getEnumConstants());
            combo.setPlaceholder("Filter");
            combo.setClearButtonVisible(true);
            combo.setWidthFull();
            combo.addValueChangeListener(listener);
            filterComponent = combo;
        } else {
            TextField filter = new TextField();
            filter.setPlaceholder("Filter");
            filter.setClearButtonVisible(true);
            filter.setWidthFull();
            filter.addValueChangeListener(listener);
            filterComponent = filter;
        }
        return filterComponent;
    }

    public static Component cloneFilterComponent(Component component) {
        if (component instanceof ComboBox<?> original) {
            ComboBox<Object> copy = new ComboBox<>(original.getLabel());

            copy.setPlaceholder(original.getPlaceholder());
            copy.setClearButtonVisible(original.isClearButtonVisible());
            copy.setRequiredIndicatorVisible(original.isRequiredIndicatorVisible());
            copy.setAllowCustomValue(original.isAllowCustomValue());
            copy.setWidthFull();

            // Copy item label generator
            if (original.getItemLabelGenerator() != null) {
                copy.setItemLabelGenerator((ItemLabelGenerator<Object>) original.getItemLabelGenerator());
            }

            // Copy items (this ALWAYS works)
            if (original.getDataProvider() instanceof ListDataProvider<?> listProvider) {
                copy.setItems(listProvider.getItems().toArray());
            } else {
                // Fallback: extract items manually
                List<?> items = original.getDataProvider()
                        .fetch(new Query<>())
                        .toList();
                copy.setItems(items);
            }

            return copy;
        }

        if (component instanceof TextField tf) {
            TextField copy = new TextField(tf.getLabel());
            copy.setPlaceholder(tf.getPlaceholder());
            copy.setClearButtonVisible(tf.isClearButtonVisible());
            return copy;
        }

        if (component instanceof Checkbox cb) {
            return new Checkbox(cb.getLabel());
        }
        if (component instanceof DatePicker dp) {
            return new DatePicker(dp.getLabel());
        }
        throw new IllegalArgumentException("Unsupported filter type: " + component.getClass());
    }
}
