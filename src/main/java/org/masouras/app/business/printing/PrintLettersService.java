package org.masouras.app.business.printing;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.masouras.app.base.element.util.VaadinGridUtils;
import org.masouras.app.base.element.control.SelectedItemsActionsPanel;
import org.masouras.data.control.service.PrintFileService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingFilesService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.LetterToPrintDTO;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintLettersService {
    private final PrintingDataService printingDataService;
    private final PrintingFilesService printingFilesService;
    private final PrintFileService printFileService;

    public SelectedItemsActionsPanel<LetterToPrintDTO> createPanel(Supplier<Set<LetterToPrintDTO>> selectedItemsSupplier) {
        ComboBox<String> printerCombo = new ComboBox<>("Select Printer");
        printerCombo.setItems(printFileService.getAvailablePrinters());
        printerCombo.setPlaceholder("Choose printer...");

        SelectedItemsActionsPanel<LetterToPrintDTO> selectedItemsActionsPanel = new SelectedItemsActionsPanel<>(
                selectedItemsSupplier,
                List.of(
                        VaadinGridUtils.createButton("Print Selected", new Icon(VaadinIcon.PRINT), "Print Selected",
                                _ -> printLetters(selectedItemsSupplier.get(), printerCombo.getValue()), ButtonVariant.LUMO_TERTIARY),
                        VaadinGridUtils.createButton("Archive Selected", new Icon(VaadinIcon.FOLDER), "Archive Selected",
                                _ -> archiveLetters(selectedItemsSupplier.get()), ButtonVariant.LUMO_TERTIARY)
                )
        );

        selectedItemsActionsPanel.addComponentAsFirst(printerCombo);

        selectedItemsActionsPanel.init();
        return selectedItemsActionsPanel;
    }

    public void printLetters(Set<LetterToPrintDTO> letterToPrintDTOS, @Nullable String selectedPrinter) {
        if (StringUtils.isBlank(selectedPrinter)) {
            Notification.show("Please select a printer before printing.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (CollectionUtils.isEmpty(letterToPrintDTOS)) {
            Notification.show("Noting is selected for printing.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        printLettersMain(letterToPrintDTOS, selectedPrinter);
    }
    private void printLettersMain(Set<LetterToPrintDTO> letterToPrintDTOS, String selectedPrinter) {
        if (log.isInfoEnabled()) log.info("Starting to print {} letters", letterToPrintDTOS.size());

        letterToPrintDTOS.stream()
                .map(letterToPrintDTO -> new AbstractMap.SimpleEntry<>(letterToPrintDTO, letterToPrintDTO.getFinalContentId()))
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> printingFilesService.findById(entry.getValue()).ifPresent(printingFilesEntity -> {
                    if (log.isInfoEnabled()) log.info("Printing letter with Content ID: {}", entry.getValue());
                    printFileService.printPdf(printingFilesEntity.getContentBinary(), selectedPrinter);
                    archiveLetters(Set.of(entry.getKey()));
                }));
    }

    public void archiveLetters(Set<LetterToPrintDTO> letterToPrintDTOS) {
        if (log.isInfoEnabled()) log.info("Starting to archive {} letters", letterToPrintDTOS.size());
        int updatedCount = archiveLettersMain(letterToPrintDTOS);
        if (log.isInfoEnabled()) log.info("{} letters archived", updatedCount);
    }

    private int archiveLettersMain(Set<LetterToPrintDTO> letterToPrintDTOS) {
        if (CollectionUtils.isEmpty(letterToPrintDTOS)) return 0;
        List<Long> listRecIDs = letterToPrintDTOS.stream().map(LetterToPrintDTO::getRecId).toList();
        return printingDataService.updateSetPrinted(listRecIDs);
    }
}
