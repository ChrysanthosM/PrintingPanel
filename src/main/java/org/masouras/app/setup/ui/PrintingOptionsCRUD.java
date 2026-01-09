package org.masouras.app.setup.ui;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingOptionsService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingOptionsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Route("printingOptions")
@PageTitle("Printing Options")
@Menu(order = 3, icon = "vaadin:cogs", title = "Printing Options")
@UIScope
@Component
public class PrintingOptionsCRUD extends GenericCrudView<PrintingOptionsEntity, Long> {

    @Autowired
    public PrintingOptionsCRUD(PrintingOptionsService service, PrintingOptionsForm form) {
        super(10, PrintingOptionsEntity.class, form, service);
    }
}

