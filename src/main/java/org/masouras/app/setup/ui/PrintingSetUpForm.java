package org.masouras.app.setup.ui;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.masouras.app.base.comp.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;


public class PrintingSetUpForm extends GenericEntityForm<PrintingSetUpEntity> {
    private final PrintingSetUpService service;
    private final Runnable onChange;

    public PrintingSetUpForm(PrintingSetUpService service, Runnable onChange) {
        super(PrintingSetUpEntity.class);
        this.service = service;
        this.onChange = onChange;
    }

    @Override
    protected void onSave(PrintingSetUpEntity entity) {
        service.save(entity);
        onChange.run();
        Notification.show("Printing SetUp saved", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
