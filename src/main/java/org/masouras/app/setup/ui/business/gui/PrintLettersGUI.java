package org.masouras.app.setup.ui.business.gui;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.element.control.GenericDtoView;
import org.masouras.app.setup.ui.business.domain.ListToPrintDTO;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Route("printLetters")
@PageTitle("Print Letters")
@Menu(order = 4, icon = "vaadin:print", title = "Print Letters")
@UIScope
@Component
public class PrintLettersGUI extends GenericDtoView<ListToPrintDTO> {
    private final PrintingDataService listToPrintService;

    @Autowired
    public PrintLettersGUI(PrintingDataService listToPrintService) {
        super("Print Letters", ListToPrintDTO.class);
        this.listToPrintService = listToPrintService;
    }

    @Override
    protected List<ListToPrintDTO> loadAllItems() {
        return listToPrintService.getListToPrintProjections()
                .stream()
                .map(item -> new ListToPrintDTO(
                        item.getRecId(),
                        item.getFinalContentId(),
                        item.getActivityType()
                ))
                .toList();
    }
}
