package org.masouras.app.base.element.util;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@UtilityClass
public class VaadinGridUtils {

    public static <T> void createGridColumn(Grid<T> grid, Field embeddedField, Field field, BiConsumer<Grid.Column<T>, String> filterConsumer) {
        field.setAccessible(true);
        FormField formField = field.getAnnotation(FormField.class);
        String propertyPath = (embeddedField == null) ? field.getName() : embeddedField.getName() + "." + field.getName();

        Grid.Column<T> col = grid.addColumn(entity -> getFieldValue(embeddedField, field, entity))
                .setHeader((formField != null && StringUtils.isNotBlank(formField.label())) ? formField.label() : field.getName())
                .setSortable(true)
                .setComparator((_, _) -> 0)
                .setKey(propertyPath);

        filterConsumer.accept(col, propertyPath);
    }
    private static Object getFieldValue(Field embeddedField, Field field, Object entity) {
        try {
            Object target = entity;
            if (embeddedField != null) {
                target = embeddedField.get(entity);
                if (target == null) {
                    return StringUtils.EMPTY;
                }
            }

            Object value = field.get(target);
            return value != null ? value : StringUtils.EMPTY;
        } catch (IllegalAccessException e) {
            return StringUtils.EMPTY;
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

    public static Component createFilterComponent(Field field) {
        Component filterComponent;
        if (field.getType().isEnum()) {
            ComboBox<Object> combo = new ComboBox<>();
            combo.setItems(field.getType().getEnumConstants());
            combo.setPlaceholder("Filter");
            combo.setClearButtonVisible(true);
            combo.setWidthFull();
            filterComponent = combo;
        } else {
            TextField filter = new TextField();
            filter.setPlaceholder("Filter");
            filter.setClearButtonVisible(true);
            filter.setWidthFull();
            filterComponent = filter;
        }
        return filterComponent;
    }

    public static Button createButton(@Nullable String text, Icon icon, @Nullable String tooltip, ComponentEventListener<ClickEvent<Button>> listener, ButtonVariant... variants) {
        Button button = StringUtils.isNotBlank(text) ? new Button(text, icon, listener) : new Button(icon, listener);
        Optional.ofNullable(variants).filter(v -> v.length > 0).ifPresent(button::addThemeVariants);
        if (StringUtils.isNotBlank(tooltip)) button.getElement().setProperty("title", tooltip);
        return button;
    }

    public static <T> void reorderColumnsSetFirst(Grid<T> grid, Grid.Column<T> setFirstColumn) {
        List<Grid.Column<T>> newOrder = Stream.concat(
                Stream.of(setFirstColumn),
                grid.getColumns().stream().filter(column -> column != setFirstColumn)
        ).toList();
        grid.setColumnOrder(newOrder);
    }
}
