package org.masouras.app.business.printing;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.combobox.ComboBox;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.masouras.app.base.element.control.SelectedItemsProgressState;
import org.masouras.data.boundary.FilesFacade;
import org.masouras.data.control.service.PrintFileService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingFilesService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.LetterToPrintDTO;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintLettersService {
    private final PrintingDataService printingDataService;
    private final PrintingFilesService printingFilesService;
    private final PrintFileService printFileService;
    private final FilesFacade filesFacade;

    private static final int CHUNK_SIZE = 10;

    public boolean printLetters(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, String selectedPrinter, @Nullable String selectedOutputPath) {
        if (log.isInfoEnabled()) log.info("Starting to print {} letters", selectedItemsProgressState.getSelectedItemsCached().size());
        return printLettersProcedure(selectedItemsProgressState, selectedPrinter, selectedOutputPath);
    }

    private boolean printLettersProcedure(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, String selectedPrinter, String selectedOutputPath) {
        Map<Integer, Set<LetterToPrintDTO>> chunkedLettersToPrintDTOs = chunkLettersToPrintDTOs(selectedItemsProgressState.getSelectedItemsCached(), CHUNK_SIZE);
        chunkedLettersToPrintDTOs.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .takeWhile(_ -> selectedItemsProgressState.progressIsNotCancelled())
                .forEach(entry -> printLettersProcedureMain(selectedItemsProgressState, selectedPrinter, selectedOutputPath, entry.getKey(), entry.getValue()));
        return selectedItemsProgressState.progressIsNotCancelled();
    }
    private void printLettersProcedureMain(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, String selectedPrinter, String selectedOutputPath,
                                           Integer chunkIndex, Set<LetterToPrintDTO> chunkOfLetterToPrintDTOs) {
        if (log.isInfoEnabled()) log.info("Processing chunk {} with {} letters", chunkIndex, chunkOfLetterToPrintDTOs.size());
        List<byte[]> pdfByteArrayList = getPdfByteArrayList(chunkOfLetterToPrintDTOs);
        if (log.isInfoEnabled()) log.info("Found {} actual letters", pdfByteArrayList.size());
        printLettersProcedureMergeAndPrint(selectedItemsProgressState, selectedPrinter, selectedOutputPath, chunkIndex, chunkOfLetterToPrintDTOs, pdfByteArrayList);
    }
    private void printLettersProcedureMergeAndPrint(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, String selectedPrinter, String selectedOutputPath,
                                                    Integer chunkIndex, Set<LetterToPrintDTO> chunkOfLetterToPrintDTOs,
                                                    List<byte[]> pdfByteArrayList) {
        String uuid = UUID.randomUUID().toString();
        try {
            byte[] mergedPdfBytes = mergePDFsToPrint(selectedItemsProgressState, pdfByteArrayList);
            if (selectedItemsProgressState.progressIsCancelled()) return;
            if (log.isInfoEnabled()) log.info("Merged PDF size for chunk No.{} ({}): {}", chunkIndex, uuid, mergedPdfBytes.length);
            printFileService.printPdf(uuid, mergedPdfBytes, selectedPrinter, selectedOutputPath);

            archiveLetters(chunkOfLetterToPrintDTOs);
            selectedItemsProgressState.progressIncrement(CHUNK_SIZE);
        } catch (IOException e) {
            if (log.isErrorEnabled()) log.error("Error merging PDF chunk No.{} ({}): {}", chunkIndex, uuid, e.getMessage(), e);
            throw new UncheckedIOException(e);
        }
    }

    private byte[] mergePDFsToPrint(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, List<byte[]> pdfByteArray) throws IOException {
        try (PDDocument mergedPDDocument = new PDDocument()) {
            for (byte[] pdfBytes : pdfByteArray) {
                try (PDDocument pdDocument = Loader.loadPDF(pdfBytes)) {
                    addBlankPage(mergedPDDocument);
                    for (PDPage page : pdDocument.getPages()) {
                        mergedPDDocument.addPage(page);
                        if (selectedItemsProgressState.progressIsCancelled()) return ArrayUtils.EMPTY_BYTE_ARRAY;
                    }
                }
                if (selectedItemsProgressState.progressIsCancelled()) return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mergedPDDocument.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
    private void addBlankPage(PDDocument mergedPDDocument) {
        // Ensure next PDF starts on an even page
        int currentPages = mergedPDDocument.getNumberOfPages();
        if (currentPages % 2 != 0) {
            PDPage lastPage = mergedPDDocument.getPage(currentPages - 1);
            PDRectangle size = lastPage.getMediaBox();
            mergedPDDocument.addPage(new PDPage(size));
        }
    }

    private Map<Integer, Set<LetterToPrintDTO>> chunkLettersToPrintDTOs(Set<LetterToPrintDTO> letterToPrintDTOS, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);
        return letterToPrintDTOS.stream()
                .collect(Collectors.groupingBy(
                        _ -> index.getAndIncrement() / chunkSize,
                        LinkedHashMap::new,
                        Collectors.toUnmodifiableSet()
                ));
    }
    public @NonNull List<byte[]> getPdfByteArrayList(Set<LetterToPrintDTO> letterToPrintDTOS) {
        List<Long> finalContentIds = letterToPrintDTOS.stream().map(LetterToPrintDTO::getFinalContentId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(finalContentIds)) return Collections.emptyList();

        return printingFilesService.findByAllByIds(finalContentIds).parallelStream()
                .flatMap(printingFilesEntity -> {
                    List<byte[]> pdfBytesList = filesFacade.byteArrayToObject(printingFilesEntity.getContentBinary());
                    if (CollectionUtils.isEmpty(pdfBytesList)) {
                        if (log.isWarnEnabled()) log.warn("pdfBytesList is empty after converting byte array to object. Cannot print PDFs for printingFilesEntity ID: {}", printingFilesEntity.getId());
                        return Stream.empty();
                    }
                    return pdfBytesList.stream().filter(ArrayUtils::isNotEmpty);
                })
                .toList();
    }

    public boolean validateSelectedLetters(SelectedItemsProgressState<LetterToPrintDTO> selectedItemsProgressState, ComboBox<String> printerCombo) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(selectedItemsProgressState.getPrintingJobID()));
        Preconditions.checkArgument(StringUtils.isNotBlank(printerCombo.getValue()), "Selected printer cannot be empty");
        Preconditions.checkArgument(!selectedItemsProgressState.getGenericGridContainer().getGridState().getGrid().getSelectedItems().isEmpty(), "No letters selected for printing");
        return true;
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
