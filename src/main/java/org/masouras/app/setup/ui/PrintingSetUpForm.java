package org.masouras.app.setup.ui;

import com.vaadin.flow.spring.annotation.UIScope;
import org.jspecify.annotations.Nullable;
import org.masouras.app.base.element.control.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class PrintingSetUpForm extends GenericEntityForm<PrintingSetUpEntity, PrintingSetUpKey> {

    @Autowired
    public PrintingSetUpForm(PrintingSetUpService service) {
        super(PrintingSetUpEntity.class, service);
    }

    @Override
    protected @Nullable String getOnSaveGetNotificationMessage() {
        return "Printing SetUp saved";
    }
}
