package org.masouras.app.base.element.control;

import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudService;
import org.masouras.model.mssql.schema.jpa.boundary.GenericCrudServiceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GenericCrudServiceFactory {
    private final Map<Class<?>, GenericCrudServiceStrategy<?, ?>> genericCrudServiceStrategyMap = new HashMap<>();

    @Autowired
    public GenericCrudServiceFactory(List<GenericCrudServiceStrategy<?, ?>> genericCrudServiceStrategies) {
        genericCrudServiceStrategies.forEach(serviceStrategy -> genericCrudServiceStrategyMap.put(serviceStrategy.getEntityClass(), serviceStrategy));
    }

    @SuppressWarnings("unchecked")
    public <T, ID> GenericCrudService<T, ID> getGenericCrudService(Class<T> entityClass) {
        return ((GenericCrudServiceStrategy<T, ID>) Optional.ofNullable(genericCrudServiceStrategyMap.get(entityClass))
                .orElseThrow(() -> new IllegalStateException("No GenericCrudService found for entity: " + entityClass.getName()))
        ).getGenericCrudService();
    }
}
