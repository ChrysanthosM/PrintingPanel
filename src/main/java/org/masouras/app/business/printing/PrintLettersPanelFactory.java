package org.masouras.app.business.printing;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.masouras.app.base.element.component.GenericGridContainer;
import org.masouras.app.base.element.control.FolderBrowserDialog;
import org.masouras.app.base.element.control.SelectedItemsActionsPanel;
import org.masouras.app.base.element.control.SelectedItemsProgressState;
import org.masouras.app.base.element.util.AsyncExecutorProvider;
import org.masouras.app.base.element.util.VaadinButtonFactory;
import org.masouras.app.base.element.util.VaadinNotificationFactory;
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
    private final SelectedItemsProgressService selectedItemsProgressService;

    public SelectedItemsActionsPanel<LetterToPrintDTO> createPanel(GenericGridContainer<LetterToPrintDTO> genericGridContainer) {
        ComboBox<String> printerCombo = new ComboBox<>("Select Printer");
        printerCombo.setItems(printFileService.getAvailablePrinters());
        printerCombo.setPlaceholder("Choose printer...");
        printerCombo.setWidth("auto");

        Path defaultPath = Path.of(System.getProperty("user.home"));
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(arg -> arg.contains("-agentlib:jdwp="))) {
            defaultPath = Path.of("D:", "MyDocuments", "Programming", "Files", "PMP", "Print");
        }
        TextField folderField = new TextField("Output Path", defaultPath.toString(), "Print to PDF? -> Output Path");
        folderField.setReadOnly(true);
        folderField.setWidthFull();
        folderField.setTooltipText(folderField.getValue());
        Button browseButton = VaadinButtonFactory.createButton("Browse", new Icon(VaadinIcon.FOLDER_OPEN), "Browse for output folder",
                _ -> new FolderBrowserDialog(folderField, folderField.getValue()).open(), ButtonVariant.LUMO_TERTIARY);


        SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState = new SelectedItemsProgressState<>(true, selectedItemsProgressService, genericGridContainer);
        Button printButton = VaadinButtonFactory.createButton("Print Selected", new Icon(VaadinIcon.PRINT), "Print Selected",
                _ -> printSelectedLetters(selectedItemsProgressState, printerCombo, folderField),
                ButtonVariant.LUMO_TERTIARY);
        Button archiveButton = VaadinButtonFactory.createButton("Archive Selected", new Icon(VaadinIcon.FOLDER), "Archive Selected",
                _ -> archiveSelectedLetters(genericGridContainer),
                ButtonVariant.LUMO_TERTIARY);

        SelectedItemsActionsPanel<LetterToPrintDTO> selectedItemsActionsPanel = new SelectedItemsActionsPanel<>(
                selectedItemsProgressState,
                List.of(printButton, archiveButton),
                List.of(printerCombo, folderField, browseButton)
        );
        selectedItemsActionsPanel.init();

        return selectedItemsActionsPanel;
    }

    private void archiveSelectedLetters(GenericGridContainer<LetterToPrintDTO> genericGridContainer) {
        Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier = () -> genericGridContainer.getGridState().getGrid().getSelectedItems();
        if (CollectionUtils.isEmpty(selectedItemsSupplier.get())) return;

        genericGridContainer.setEnabled(false);
        try {
            printLettersService.archiveLetters(selectedItemsSupplier.get());
        } catch (Exception e) {
            VaadinNotificationFactory.showErrorNotification("Archive failed:", e);
        } finally {
            genericGridContainer.refreshGrid();
            genericGridContainer.setEnabled(true);
        }
    }

    private void printSelectedLetters(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState,
                                      ComboBox<String> printerCombo, TextField folderField) {
        selectedItemsProgressState.progressStart();
        AsyncExecutorProvider.runAsyncSequential(
                List.of(
                        () -> printLettersService.prepareLettersForPrinting(selectedItemsProgressState),
                        () -> printLettersService.reEnabledDataGrid(selectedItemsProgressState),
                        () -> printLettersService.printLetters(selectedItemsProgressState, printerCombo.getValue(), folderField.getValue())
                ),
                _ -> selectedItemsProgressState.progressEndedOK(),
                selectedItemsProgressState::progressEndedError
        );
    }
}
