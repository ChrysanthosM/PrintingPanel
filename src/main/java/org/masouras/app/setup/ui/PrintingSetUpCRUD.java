package org.masouras.app.setup.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Route("printingSetUp")
@PageTitle("Printing SetUp")
@Menu(order = 2, icon = "vaadin:cube", title = "Printing SetUp")
@UIScope
@Component
public class PrintingSetUpCRUD extends GenericCrudView<PrintingSetUpEntity> {
    private final PrintingSetUpService service;

    @Autowired
    public PrintingSetUpCRUD(PrintingSetUpService service, PrintingSetUpForm form) {
        super(form);
        form.setOnSaveCallback(this::updateList);
        this.service = service;
    }

    @Override
    protected void addGridColumns(Grid<PrintingSetUpEntity> grid) {
        grid.addColumn(e -> e.getId().getActivityType()).setHeader("Activity Type");
        grid.addColumn(e -> e.getId().getContentType()).setHeader("Content Type");
        grid.addColumn(e -> e.getId().getSeqNo()).setHeader("Seq No");
        grid.addColumn(PrintingSetUpEntity::getLetterType).setHeader("Letter Type");
    }

    @Override
    protected List<PrintingSetUpEntity> fetchItems() {
        return service.findAll();
    }

    @Override
    protected void deleteItem(PrintingSetUpEntity entity) {
        service.deleteById(entity.getId());
    }

    @Override
    protected PrintingSetUpEntity createNewEntity() {
        return new PrintingSetUpEntity();
    }
}

