package org.masouras.app.base.element.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@UtilityClass
public class VaadinSpringBridge {

    @SuppressWarnings("unchecked")
    public static Component createField(Field field, FormField formField) {
        try {
            Component component = formField.component().getDeclaredConstructor().newInstance();

            if (component instanceof HasLabel label) {
                label.setLabel(formField.label());
            }
            if (component instanceof HasPlaceholder placeholder) {
                placeholder.setPlaceholder(formField.label());
            }

            if (component instanceof ComboBox<?> combo) {
                Class<?> enumType = field.getType();
                if (enumType.isEnum()) {
                    ComboBox<Object> cb = (ComboBox<Object>) combo;
                    cb.setItems(enumType.getEnumConstants());
                }
            }

            return component;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create field for " + field.getName(), e);
        }
    }

    public static <T> Sort toSpringSort(List<GridSortOrder<T>> orders) {
        return Sort.by(orders.stream()
                .map(order -> new Sort.Order(
                        order.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC,
                        order.getSorted().getKey()
                ))
                .toList()
        );
    }

    public static <T> Specification<T> buildSpecification(Class<T> entityClass, Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((property, value) -> {
                Path<?> path = resolvePath(root, property);
                if (value instanceof String stringValue) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), "%" + stringValue.toLowerCase() + "%"));
                } else {
                    predicates.add(criteriaBuilder.equal(path, value));
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    private static Path<?> resolvePath(From<?, ?> root, String propertyPath) {
        return Arrays.stream(propertyPath.split("\\."))
                .reduce((Path<?>) root, Path::get, (p1, p2) -> p2);
    }
}
