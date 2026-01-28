package org.masouras.app.setup.ui.crud;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.element.control.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Route("printingSetUp")
@PageTitle("Printing SetUp")
@Menu(order = 2, icon = "vaadin:cube", title = "Printing SetUp")
@UIScope
@Component
public class PrintingSetUpCRUD extends GenericCrudView<PrintingSetUpEntity, PrintingSetUpKey> {
    @Autowired
    public PrintingSetUpCRUD() {
        super("Printing SetUp", 10, PrintingSetUpEntity.class);
    }

}

