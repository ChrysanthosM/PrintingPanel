package org.masouras.app.base.element.control;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.masouras.app.base.element.util.VaadinUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FolderBrowserDialog extends Dialog {
    private Path currentPath;
    private final VerticalLayout folderList = new VerticalLayout();

    public FolderBrowserDialog(TextField targetField, String initialPath) {
        this.currentPath = Path.of(initialPath);

        setHeaderTitle("Select Output Folder");
        setWidth("500px");
        setHeight("400px");

        // Current path display
        TextField pathDisplay = new TextField();
        pathDisplay.setReadOnly(true);
        pathDisplay.setWidthFull();

        folderList.setPadding(false);
        folderList.setSpacing(false);
        Scroller scroller = new Scroller(folderList);
        scroller.setWidthFull();
        scroller.setHeight("280px");

        add(pathDisplay, scroller);

        getFooter().add(
                VaadinUtils.createButton("Cancel", new Icon(VaadinIcon.CLOSE), "Cancel",
                        _ -> close(), ButtonVariant.LUMO_TERTIARY),
                VaadinUtils.createButton("Select This Folder", new Icon(VaadinIcon.CHECK), "Select This Folder",
                         _ -> {
                            targetField.setValue(currentPath.toString());
                             targetField.setTooltipText(targetField.getValue());
                            close();
                        }, ButtonVariant.LUMO_PRIMARY)
        );

        addOpenedChangeListener(_ -> { if (isOpened()) refresh(pathDisplay); });
        refresh(pathDisplay);
    }

    private void refresh(TextField pathDisplay) {
        pathDisplay.setValue(currentPath.toString());
        folderList.removeAll();

        // "Go up" entry
        Path parent = currentPath.getParent();
        if (parent != null) {
            Button upBtn = VaadinUtils.createButton("[..] Up one level", new Icon(VaadinIcon.ARROW_UP), "Go up to parent directory",
                    _ -> {
                        currentPath = parent;
                        refresh(pathDisplay);
                    }, ButtonVariant.LUMO_TERTIARY);
            upBtn.setWidthFull();
            folderList.add(upBtn);
        }

        // List subdirectories
        try (Stream<Path> paths = Files.list(currentPath)) {
            paths.filter(Files::isDirectory)
                    .sorted()
                    .forEach(dir -> {
                        Button dirBtn = VaadinUtils.createButton(dir.getFileName().toString(), new Icon(VaadinIcon.FOLDER), "Open " + dir.getFileName(),
                                _ -> {
                                    currentPath = dir;
                                    refresh(pathDisplay);
                                }, ButtonVariant.LUMO_TERTIARY);
                        dirBtn.setWidthFull();
                        folderList.add(dirBtn);
                    });
        } catch (IOException e) {
            folderList.add(new Span("Cannot read directory: " + e.getMessage()));
        }
    }
}
