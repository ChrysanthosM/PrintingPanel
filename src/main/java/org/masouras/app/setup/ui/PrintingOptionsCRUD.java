package org.masouras.app.setup.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingOptionsService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingOptionsEntity;
import org.masouras.model.mssql.schema.qb.structure.DbField;
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
        super(PrintingOptionsEntity.class, form, service);
    }
//
//    @Override
//    protected void addGridColumns(Grid<PrintingOptionsEntity> grid) {
//        grid.addColumn(PrintingOptionsEntity::getPrintingOptionType).setHeader(DbField.OPTION_TYPE.asAlias());
//        grid.addColumn(PrintingOptionsEntity::getPrintingOptionName).setHeader(DbField.OPTION_NAME.asAlias());
//        grid.addColumn(PrintingOptionsEntity::getPrintingOptionValue).setHeader(DbField.OPTION_VALUE.asAlias());
//        grid.addColumn(PrintingOptionsEntity::getPrintingOptionDetails).setHeader(DbField.OPTION_DETAILS.asAlias());
//    }
}

