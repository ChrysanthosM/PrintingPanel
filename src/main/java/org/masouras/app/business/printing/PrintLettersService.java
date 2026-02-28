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
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

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
        List<LetterToPrintDTO> letterToPrintDTOS = selectedItemsProgressState.getSelectedItemsCached();
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
