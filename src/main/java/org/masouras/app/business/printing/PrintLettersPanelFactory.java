package org.masouras.app.business.printing;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.masouras.app.base.element.component.GenericGridContainer;
import org.masouras.app.base.element.control.FolderBrowserDialog;
import org.masouras.app.base.element.control.SelectedItemsActionsPanel;
import org.masouras.app.base.element.util.AsyncUiExecutor;
import org.masouras.app.base.element.util.ProgressPanel;
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
    private final PrintLettersProgressService printLettersProgressService;

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


        Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier = () -> genericGridContainer.getGridState().getGrid().getSelectedItems();
        ProgressPanel progressPanel = new ProgressPanel();
        Button printButton = VaadinButtonFactory.createButton("Print Selected", new Icon(VaadinIcon.PRINT), "Print Selected",
                _ -> printSelectedLetters(progressPanel, genericGridContainer, printerCombo, folderField),
                ButtonVariant.LUMO_TERTIARY);
        Button archiveButton = VaadinButtonFactory.createButton("Archive Selected", new Icon(VaadinIcon.FOLDER), "Archive Selected",
                _ -> archiveSelectedLetters(genericGridContainer),
                ButtonVariant.LUMO_TERTIARY);

        SelectedItemsActionsPanel<LetterToPrintDTO> selectedItemsActionsPanel = new SelectedItemsActionsPanel<>(
                selectedItemsSupplier,
                List.of(printButton, archiveButton),
                List.of(printerCombo, folderField, browseButton),
                progressPanel
        );
        selectedItemsActionsPanel.init();

        return selectedItemsActionsPanel;
    }

    private void archiveSelectedLetters(GenericGridContainer<LetterToPrintDTO> genericGridContainer) {
        Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier = () -> genericGridContainer.getGridState().getGrid().getSelectedItems();
        AsyncUiExecutor.runWithUiLock(
                genericGridContainer,
                () -> printLettersService.archiveLetters(selectedItemsSupplier.get()),
                throwable -> showErrorNotification("Archive failed:", throwable),
                genericGridContainer::refreshGrid
        );
    }

    private void printSelectedLetters(ProgressPanel progressPanel, GenericGridContainer<LetterToPrintDTO> genericGridContainer, ComboBox<String> printerCombo, TextField folderField) {
        Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier = () -> genericGridContainer.getGridState().getGrid().getSelectedItems();

        String printingJobID = printLettersProgressService.startJob();
        progressPanel.start(selectedItemsSupplier.get().size());

        UI ui = UI.getCurrent();
        ui.setPollInterval(500);
        Registration pollRegistration = ui.addPollListener(_ -> progressPanel.update(printLettersProgressService.getCurrent(printingJobID)));

        AsyncUiExecutor.runWithUiLock(
                genericGridContainer,
                () -> printLettersService.printLetters(printingJobID, selectedItemsSupplier.get(), printerCombo.getValue(), folderField.getValue()),
                throwable -> endOfPrintingLettersError(progressPanel, throwable, ui, pollRegistration, printingJobID),
                () -> endOfPrintingLettersNormal(ui, pollRegistration, printingJobID, progressPanel, genericGridContainer)
        );
    }
    private void endOfPrintingLettersError(ProgressPanel progressPanel, Throwable throwable, UI ui, Registration pollRegistration, String printingJobID) {
        stopProgressBar(ui, pollRegistration, printingJobID, progressPanel);
        showErrorNotification("Printing failed:", throwable);
    }
    private void endOfPrintingLettersNormal(UI ui, Registration pollRegistration, String printingJobID, ProgressPanel progressPanel, GenericGridContainer<LetterToPrintDTO> genericGridContainer) {
        stopProgressBar(ui, pollRegistration, printingJobID, progressPanel);
        genericGridContainer.refreshGrid();
    }
    private void stopProgressBar(UI ui, Registration pollRegistration, String printingJobID, ProgressPanel progressPanel) {
        ui.setPollInterval(-1);
        pollRegistration.remove();
        progressPanel.finish();
        printLettersProgressService.endJob(printingJobID);
    }

    private void showErrorNotification(@NonNull String message, Throwable err) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(0);

        HorizontalLayout layout = new HorizontalLayout(
                new Text(message + StringUtils.SPACE + err.getMessage()),
                VaadinButtonFactory.createButton("Close", new Icon(VaadinIcon.CLOSE), "Close",
                        _ -> notification.close(), ButtonVariant.LUMO_ERROR));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        notification.add(layout);

        notification.open();
    }
}
