package org.masouras.app.setup.ui;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.masouras.app.base.comp.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class PrintingSetUpForm extends GenericEntityForm<PrintingSetUpEntity> {
    private final PrintingSetUpService service;
    @Setter private Runnable onSaveCallback;

    public PrintingSetUpForm(PrintingSetUpService service) {
        super(PrintingSetUpEntity.class);
        this.service = service;
    }

    @Override
    protected void onSave(PrintingSetUpEntity entity) {
        service.save(entity);
        if (onSaveCallback != null) onSaveCallback.run();
        Notification.show("Printing SetUp saved", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
