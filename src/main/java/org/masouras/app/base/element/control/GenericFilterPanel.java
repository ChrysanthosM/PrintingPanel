package org.masouras.app.base.element.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GenericFilterPanel extends Details {
    private final Map<String, Component> filters = new LinkedHashMap<>();
    private boolean initialized = false;

    public GenericFilterPanel(String title) {
        setSummaryText(title);
        setOpened(false);
    }
    public void initializeFilters(Map<String, Supplier<Component>> factories) {
        if (initialized) return;
        initialized = true;
        setFilters(factories);
    }
    public void setFilters(Map<String, Supplier<Component>> factories) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        factories.forEach((key, factory) -> {
            Component component = factory.get();
            filters.put(key, component);
            layout.add(component);
        });
        add(layout);
    }

    public Map<String, Object> getFilterValues() {
        return filters.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), extractValue(entry.getValue())))
                .filter(entry -> entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private Object extractValue(Component c) {
        return c instanceof HasValue<?, ?> hv ? hv.getValue() : null;
    }
}
