package org.masouras.app.base.element.control;

import org.masouras.app.base.element.component.GenericGridContainer;

public interface HasSelectedItemsActionsPanel<T> {
    default SelectedItemsActionsPanel<T> createActionsPanel(GenericGridContainer<T> genericGridContainer) { return null; }
}
