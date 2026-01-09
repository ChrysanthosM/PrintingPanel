package org.masouras.app.base.comp;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

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


}
