package org.masouras.app.business.printing;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.masouras.app.base.element.util.VaadinGridUtils;
import org.masouras.app.base.element.control.SelectedItemsActionsPanel;
import org.masouras.data.control.service.PrintFileService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingFilesService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.ListToPrintDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintLettersService {
    private final PrintingDataService printingDataService;
    private final PrintingFilesService printingFilesService;
    private final PrintFileService printFileService;

    public SelectedItemsActionsPanel<ListToPrintDTO> createPanel(Supplier<Set<ListToPrintDTO>> selectedItemsSupplier) {
        ComboBox<String> printerCombo = new ComboBox<>("Select Printer");
        printerCombo.setItems(printFileService.getAvailablePrinters());
        printerCombo.setPlaceholder("Choose printer...");

        SelectedItemsActionsPanel<ListToPrintDTO> selectedItemsActionsPanel = new SelectedItemsActionsPanel<>(
                selectedItemsSupplier,
                List.of(
                        VaadinGridUtils.createButton("Print Selected", new Icon(VaadinIcon.PRINT), "Print Selected",
                                _ -> printLetters(selectedItemsSupplier.get(), printerCombo.getValue()), ButtonVariant.LUMO_TERTIARY),
                        VaadinGridUtils.createButton("Archive Selected", new Icon(VaadinIcon.DOWNLOAD), "Archive Selected",
                                _ -> archiveLetters(selectedItemsSupplier.get()), ButtonVariant.LUMO_TERTIARY)
                )
        );

        selectedItemsActionsPanel.addComponentAsFirst(printerCombo);

        selectedItemsActionsPanel.init();
        return selectedItemsActionsPanel;
    }

    public void printLetters(Set<ListToPrintDTO> selectedRows, String selectedPrinter) {
        if (log.isInfoEnabled()) log.info("Starting to print {} letters", selectedRows.size());
        if (CollectionUtils.isEmpty(selectedRows)) return;
        List<Long> listContentIDs = selectedRows.stream().map(ListToPrintDTO::getFinalContentId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(listContentIDs)) return;

        listContentIDs.forEach(id -> printingFilesService.findById(id).ifPresent(printingFilesEntity -> {
            if (log.isInfoEnabled()) log.info("Printing letter with Content ID: {}", id);
            printFileService.printPdf(printingFilesEntity.getContentBinary(), selectedPrinter);
            printFileService.printToMicrosoftPdf(printingFilesEntity.getContentBinary(), "D:\\MyDocuments\\Temp\\PrintingTest\\Letter_" + id + ".pdf");
        }));
    }

    public void archiveLetters(Set<ListToPrintDTO> selected) {
        if (log.isInfoEnabled()) log.info("Starting to archive {} letters", selected.size());
        archiveLettersMain(selected);
    }

    private void archiveLettersMain(Set<ListToPrintDTO> selected) {
        if (CollectionUtils.isEmpty(selected)) return;
        List<Long> listRecIDs = selected.stream().map(ListToPrintDTO::getRecId).toList();
        int updatedCount = printingDataService.updateSetPrinted(listRecIDs);
        if (log.isInfoEnabled()) log.info("{} letters archived", updatedCount);
    }
}
