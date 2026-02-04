package org.masouras.app.setup.ui.business.service;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.masouras.app.base.element.util.VaadinGridUtils;
import org.masouras.app.setup.ui.business.gui.SelectedItemsActionsPanel;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.ListToPrintDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Slf4j
public class PrintLettersService {
    public SelectedItemsActionsPanel<ListToPrintDTO> createPanel(Supplier<Set<ListToPrintDTO>> selectedItemsSupplier) {
        SelectedItemsActionsPanel<ListToPrintDTO> selectedItemsActionsPanel = new SelectedItemsActionsPanel<>(
                selectedItemsSupplier,
                List.of(
                        VaadinGridUtils.createButton("Print Selected", new Icon(VaadinIcon.PRINT), "Print Selected",
                                _ -> printLetters(selectedItemsSupplier.get()), ButtonVariant.LUMO_TERTIARY),
                        VaadinGridUtils.createButton("Archive Selected", new Icon(VaadinIcon.DOWNLOAD), "Archive Selected",
                                _ -> archiveLetters(selectedItemsSupplier.get()), ButtonVariant.LUMO_TERTIARY)
                )
        );

        selectedItemsActionsPanel.init();
        return selectedItemsActionsPanel;
    }

    public void printLetters(Set<ListToPrintDTO> selected) {
        if (log.isDebugEnabled()) log.info("Starting to print {} letters", selected.size());
        if (CollectionUtils.isEmpty(selected)) return;
    }

    public void archiveLetters(Set<ListToPrintDTO> selected) {
        if (log.isDebugEnabled()) log.info("Starting to archive {} letters", selected.size());
        if (CollectionUtils.isEmpty(selected)) return;
    }
}
