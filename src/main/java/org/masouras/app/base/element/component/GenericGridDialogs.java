package org.masouras.app.base.element.component;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.masouras.app.base.element.util.VaadinGridUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class GenericGridDialogs {

    public static <T> void showDeleteDialog(T entity, Consumer<List<T>> deleteAction) {
        Dialog dialog = new Dialog();
        dialog.add("Are you sure you want to delete this record?");
        dialog.add(new HorizontalLayout(
                VaadinGridUtils.createButton("Delete", new Icon(VaadinIcon.TRASH), "Delete",
                        _ -> {
                            deleteAction.accept(List.of(entity));
                            dialog.close();
                        }, ButtonVariant.LUMO_WARNING),
                VaadinGridUtils.createButton("Cancel", new Icon(VaadinIcon.LEVEL_RIGHT), "Cancel", _ -> dialog.close())
        ));
        dialog.open();
    }

    public static <T> void showBulkDeleteDialog(Collection<T> selected, Consumer<List<T>> deleteAction) {
        if (selected == null || selected.isEmpty()) return;
        Dialog dialog = new Dialog();
        dialog.add("Delete " + selected.size() + " selected item" + (selected.size() == 1 ? StringUtils.EMPTY : "s") + "?");
        dialog.add(new HorizontalLayout(
                VaadinGridUtils.createButton("Delete", new Icon(VaadinIcon.TRASH), "Delete",
                        _ -> {
                            deleteAction.accept(new ArrayList<>(selected));
                            dialog.close();
                        }, ButtonVariant.LUMO_WARNING),
                VaadinGridUtils.createButton("Cancel", new Icon(VaadinIcon.LEVEL_RIGHT), "Cancel", _ -> dialog.close())
        ));
        dialog.open();
    }
}
