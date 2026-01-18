package org.masouras.app.base.element.control;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@UIScope
public class GenericEntityFormFactory {
    private final Map<Class<?>, GenericEntityFormStrategy<?, ?>> genericCrudFormStrategyMap = new HashMap<>();

    @Autowired
    public GenericEntityFormFactory(List<GenericEntityFormStrategy<?, ?>> genericCrudFormStrategies) {
        genericCrudFormStrategies.forEach(formStrategy -> genericCrudFormStrategyMap.put(formStrategy.getEntityClass(), formStrategy));
    }

    @SuppressWarnings("unchecked")
    public <T, ID> GenericEntityForm<T, ID> getGenericEntityForm(Class<T> entityClass) {
        return ((GenericEntityFormStrategy<T, ID>) Optional.ofNullable(genericCrudFormStrategyMap.get(entityClass))
                .orElseThrow(() -> new IllegalStateException("No form found"))
        ).getGenericEntityForm();
    }
}
