package org.masouras.app.setup.ui.business.gui;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.element.component.GenericGridContainer;
import org.masouras.app.base.element.control.GenericDtoView;
import org.masouras.app.setup.ui.business.service.PrintLettersService;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.ListToPrintDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Route("printLetters")
@PageTitle("Print Letters")
@Menu(order = 4, icon = "vaadin:print", title = "Print Letters")
@UIScope
@Component
public class PrintLettersGUI extends GenericDtoView<ListToPrintDTO> {
    private final PrintingDataService printingDataService;
    private final PrintLettersService printLettersService;

    @Autowired
    public PrintLettersGUI(PrintingDataService printingDataService, PrintLettersService printLettersService) {
        super("Print Letters", ListToPrintDTO.class);
        this.printingDataService = printingDataService;
        this.printLettersService = printLettersService;
    }

    @Override
    public SelectedItemsActionsPanel<ListToPrintDTO> createActionsPanel(GenericGridContainer<ListToPrintDTO> genericGridContainer) {
        return printLettersService.createPanel(() -> genericGridContainer.getGridState().getGrid().getSelectedItems());
    }

    @Override
    protected List<ListToPrintDTO> loadAllItems() {
        return printingDataService.getListToPrintDTOs();
    }
}
