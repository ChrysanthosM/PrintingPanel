package org.masouras.app.base.element.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public final class GenericEntityGridState<T> {
    private final Grid<T> grid = new Grid<>();;
    private final HeaderRow filterRow = grid.appendHeaderRow();
    private final Map<Grid.Column<T>, String> columnProperties = new HashMap<>();
    private final Map<Grid.Column<T>, Component> columnFilters = new HashMap<>();
    private List<GridSortOrder<T>> currentSortOrders = new ArrayList<>();
    private List<T> allItems;
}
