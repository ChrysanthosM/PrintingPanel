package org.masouras.app.setup.ui.crud;

import com.vaadin.flow.spring.annotation.UIScope;
import org.jspecify.annotations.Nullable;
import org.masouras.app.base.element.control.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingOptionsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class PrintingOptionsForm extends GenericEntityForm<PrintingOptionsEntity, Long> {

    @Autowired
    public PrintingOptionsForm() {
        super(PrintingOptionsEntity.class);
    }

    @Override
    protected @Nullable String getOnSaveGetNotificationMessage() {
        return "Printing Option saved";
    }
}
