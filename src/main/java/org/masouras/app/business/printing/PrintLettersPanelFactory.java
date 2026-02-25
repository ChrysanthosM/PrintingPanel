package org.masouras.app.business.printing;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.masouras.app.base.element.component.GenericGridContainer;
import org.masouras.app.base.element.control.FolderBrowserDialog;
import org.masouras.app.base.element.control.SelectedItemsActionsPanel;
import org.masouras.app.base.element.util.AsyncUiExecutor;
import org.masouras.app.base.element.util.VaadinButtonFactory;
import org.masouras.data.control.service.PrintFileService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.LetterToPrintDTO;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class PrintLettersPanelFactory {
    private final PrintLettersService printLettersService;
    private final PrintFileService printFileService;

    public SelectedItemsActionsPanel<LetterToPrintDTO> createPanel(GenericGridContainer<LetterToPrintDTO> genericGridContainer) {
        ComboBox<String> printerCombo = new ComboBox<>("Select Printer");
        printerCombo.setItems(printFileService.getAvailablePrinters());
        printerCombo.setPlaceholder("Choose printer...");
        printerCombo.setWidth("auto");

        Path defaultPath = Path.of(System.getProperty("user.home"));
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp")) {
            defaultPath = Path.of("D:", "MyDocuments", "Programming", "Files", "PMP", "Print");
        }
        TextField folderField = new TextField("Output Path", defaultPath.toString(), "Print to PDF? -> Output Path");
        folderField.setReadOnly(true);
        folderField.setWidthFull();
        folderField.setTooltipText(folderField.getValue());
        Button browseButton = VaadinButtonFactory.createButton("Browse", new Icon(VaadinIcon.FOLDER_OPEN), "Browse for output folder",
                _ -> new FolderBrowserDialog(folderField, folderField.getValue()).open(), ButtonVariant.LUMO_TERTIARY);


        Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier = () -> genericGridContainer.getGridState().getGrid().getSelectedItems();
        Button printButton = VaadinButtonFactory.createButton("Print Selected", new Icon(VaadinIcon.PRINT), "Print Selected",
                _ -> printSelectedLetters(genericGridContainer, selectedItemsSupplier, printerCombo, folderField),
                ButtonVariant.LUMO_TERTIARY);
        Button archiveButton = VaadinButtonFactory.createButton("Archive Selected", new Icon(VaadinIcon.FOLDER), "Archive Selected",
                _ -> archiveSelectedLetters(genericGridContainer, selectedItemsSupplier),
                ButtonVariant.LUMO_TERTIARY);

        SelectedItemsActionsPanel<LetterToPrintDTO> panel = new SelectedItemsActionsPanel<>(
                selectedItemsSupplier,
                List.of(printButton, archiveButton),
                List.of(printerCombo, folderField, browseButton)
        );
        panel.init();

        return panel;
    }

    private void archiveSelectedLetters(GenericGridContainer<LetterToPrintDTO> genericGridContainer, Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier) {
        AsyncUiExecutor.runWithUiLock(
                genericGridContainer,
                () -> printLettersService.archiveLetters(selectedItemsSupplier.get()),
                throwable -> showErrorNotification("Archive failed:", throwable),
                genericGridContainer::refreshGrid
        );
    }

    private void printSelectedLetters(GenericGridContainer<LetterToPrintDTO> genericGridContainer, Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier, ComboBox<String> printerCombo, TextField folderField) {
        AsyncUiExecutor.runWithUiLock(
                genericGridContainer,
                () -> printLettersService.printLetters(selectedItemsSupplier.get(), printerCombo.getValue(), folderField.getValue()),
                throwable -> showErrorNotification("Printing failed:", throwable),
                genericGridContainer::refreshGrid
        );
    }

    private void showErrorNotification(@NonNull String message, Throwable err) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.MIDDLE);

        HorizontalLayout layout = new HorizontalLayout(
                new Text(message + StringUtils.SPACE + err.getMessage()),
                VaadinButtonFactory.createButton("Close", new Icon(VaadinIcon.CLOSE), "Close",
                        _ -> notification.close(), ButtonVariant.LUMO_ERROR));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        notification.add(layout);

        notification.setDuration(0);
        notification.open();
    }
}
