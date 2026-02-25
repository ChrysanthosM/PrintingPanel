package org.masouras.app.menu;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.element.component.GenericGridContainer;
import org.masouras.app.base.element.control.GenericDtoView;
import org.masouras.app.business.printing.PrintLettersPanelFactory;
import org.masouras.app.base.element.control.SelectedItemsActionsPanel;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.domain.LetterToPrintDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Route("printLetters")
@PageTitle("Print Letters")
@Menu(order = 4, icon = "vaadin:print", title = "Print Letters")
@UIScope
@Component
public class PrintLettersGUI extends GenericDtoView<LetterToPrintDTO> {
    private final PrintingDataService printingDataService;
    private final PrintLettersPanelFactory printLettersPanelFactory;

    @Autowired
    public PrintLettersGUI(PrintingDataService printingDataService, PrintLettersPanelFactory printLettersPanelFactory) {
        super("Print Letters", LetterToPrintDTO.class);
        this.printingDataService = printingDataService;
        this.printLettersPanelFactory = printLettersPanelFactory;
    }

    @Override
    public SelectedItemsActionsPanel<LetterToPrintDTO> createActionsPanel(GenericGridContainer<LetterToPrintDTO> genericGridContainer) {
        return printLettersPanelFactory.createPanel(genericGridContainer);
    }

    @Override
    protected List<LetterToPrintDTO> loadAllItems() {
        return printingDataService.getListToPrintDTOs();
    }
}
