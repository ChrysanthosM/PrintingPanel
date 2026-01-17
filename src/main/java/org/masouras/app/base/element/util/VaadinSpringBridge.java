package org.masouras.app.base.element.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
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

    public static <T> Specification<T> buildSpecification(Class<T> entityClass, Map<String, Object> filters, List<GridSortOrder<T>> orders) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = filters.entrySet().stream()
                    .map(entry -> {
                        Path<?> path = resolvePath(root, entry.getKey());
                        Object value = entry.getValue();
                        return (value instanceof String stringValue)
                                ? criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), "%" + stringValue.toLowerCase() + "%")
                                : criteriaBuilder.equal(path, value);
                    })
                    .toList();

            if (CollectionUtils.isNotEmpty(orders)) {
                List<Order> jpaOrders = getJpaOrders(entityClass, orders, root, criteriaBuilder);
                query.orderBy(jpaOrders);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    private static <T> @NonNull List<Order> getJpaOrders(Class<T> entityClass, List<GridSortOrder<T>> orders, Root<T> root, CriteriaBuilder criteriaBuilder) {
        return orders.stream()
                .map(gridOrder -> {
                    String property = gridOrder.getSorted().getKey();
                    Path<?> path = resolvePath(root, property);
                    Class<?> type = resolveFieldType(entityClass, property);
                    if (!type.isEnum()) return gridOrder.getDirection() == SortDirection.ASCENDING ? criteriaBuilder.asc(path) : criteriaBuilder.desc(path);

                    Expression<String> sortExpr = getCaseExpresion(criteriaBuilder, type, path).otherwise(StringUtils.EMPTY);
                    return gridOrder.getDirection() == SortDirection.ASCENDING ? criteriaBuilder.asc(sortExpr) : criteriaBuilder.desc(sortExpr);
                })
                .toList();
    }
    private static CriteriaBuilder.Case<String> getCaseExpresion(CriteriaBuilder criteriaBuilder, Class<?> type, Path<?> path) {
        CriteriaBuilder.Case<String> caseExpr = criteriaBuilder.selectCase();
        caseExpr = Arrays.stream(type.getEnumConstants())
                .map(constant -> (Enum<?>) constant)
                .reduce(caseExpr, (expr, enumConst) ->
                                expr.when(
                                        criteriaBuilder.equal(path, convertEnumToDbValue(enumConst)),
                                        enumConst.name()
                                ),
                        (expr1, _) -> expr1
                );
        return caseExpr;
    }
    @SuppressWarnings("unchecked")
    private static String convertEnumToDbValue(Enum<?> enumValue) {
        try {
            Class<?> converterClass = Class.forName(enumValue.getDeclaringClass().getName() + "Converter");
            AttributeConverter converter = (AttributeConverter) converterClass.getDeclaredConstructor().newInstance();
            return (String) converter.convertToDatabaseColumn(enumValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert enum to DB value", e);
        }
    }
    private static Class<?> resolveFieldType(Class<?> entityClass, String propertyPath) {
        Class<?> currentClass = entityClass;
        for (String part : propertyPath.split("\\.")) {
            try {
                Field field = currentClass.getDeclaredField(part);
                currentClass = field.getType();
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Invalid property path: " + propertyPath, e);
            }
        }
        return currentClass;
    }
    private static Path<?> resolvePath(From<?, ?> root, String propertyPath) {
        return Arrays.stream(propertyPath.split("\\."))
                .reduce((Path<?>) root, Path::get, (_, p2) -> p2);
    }
}
