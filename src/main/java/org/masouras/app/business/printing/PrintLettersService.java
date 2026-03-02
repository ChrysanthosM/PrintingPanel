package org.masouras.app.business.printing;

import com.google.common.base.Preconditions;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.masouras.app.base.element.control.SelectedItemsProgressState;
import org.masouras.data.control.service.PrintFileService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingFilesService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.LetterToPrintDTO;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintLettersService {
    private final PrintingDataService printingDataService;
    private final PrintingFilesService printingFilesService;
    private final PrintFileService printFileService;

    public boolean printLetters(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, String selectedPrinter, @Nullable String selectedOutputPath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(selectedItemsProgressState.getPrintingJobID()));
        Preconditions.checkArgument(StringUtils.isNotBlank(selectedPrinter), "Selected printer cannot be empty");
        return printLettersMain(selectedItemsProgressState, selectedPrinter, selectedOutputPath);
    }
    private boolean printLettersMain(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, String selectedPrinter, String selectedOutputPath) {
        Set<LetterToPrintDTO> letterToPrintDTOS = selectedItemsProgressState.getSelectedItemsCached();
        if (log.isInfoEnabled()) log.info("Starting to print {} letters", letterToPrintDTOS.size());

        letterToPrintDTOS.stream()
                .map(letterToPrintDTO -> new AbstractMap.SimpleEntry<>(letterToPrintDTO, letterToPrintDTO.getFinalContentId()))
                .filter(entry -> entry.getValue() != null)
                .takeWhile(_ -> selectedItemsProgressState.progressIsNotCancelled())
                .forEach(entry -> printingFilesService.findById(entry.getValue()).ifPresent(printingFilesEntity -> {
                    if (log.isInfoEnabled()) log.info("Printing letter with Content ID: {}", entry.getValue());
                    printFileService.printPdf(printingFilesEntity, selectedPrinter, StringUtils.trimToNull(selectedOutputPath));
                    archiveLetters(Set.of(entry.getKey()));
                    selectedItemsProgressState.progressIncrement();
                }));
        return selectedItemsProgressState.progressIsNotCancelled();
    }

    public boolean prepareLettersForPrinting(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState) {
        Set<LetterToPrintDTO> letterToPrintDTOS = selectedItemsProgressState.getSelectedItemsCached();
        if (log.isInfoEnabled()) log.info("Preparing {} letters for printing", letterToPrintDTOS.size());
        int updatedCount = updateSetPrintingStatus(letterToPrintDTOS, PrintingStatus.FOR_PRINTING);
        if (log.isInfoEnabled()) log.info("{} letters marked as FOR_PRINTING", updatedCount);
        return updatedCount > 0;
    }

    public boolean reEnabledDataGrid(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState) {
        selectedItemsProgressState.progressContinue();
        return true;
    }

    public void archiveLetters(Set<LetterToPrintDTO> letterToPrintDTOS) {
        if (log.isInfoEnabled()) log.info("Starting to archive {} letters", letterToPrintDTOS.size());
        int updatedCount = updateSetPrintingStatus(letterToPrintDTOS, PrintingStatus.PRINTED);
        if (log.isInfoEnabled()) log.info("{} letters archived", updatedCount);
    }
    private int updateSetPrintingStatus(Set<LetterToPrintDTO> letterToPrintDTOS, PrintingStatus printingStatus) {
        if (CollectionUtils.isEmpty(letterToPrintDTOS)) return 0;
        Set<Long> listRecIDs = letterToPrintDTOS.stream().map(LetterToPrintDTO::getRecId).filter(Objects::nonNull).collect(Collectors.toSet());
        return printingDataService.updateSetPrintingStatus(listRecIDs, printingStatus);
    }
}
