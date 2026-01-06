package org.masouras.app.setup.ui;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.masouras.app.base.comp.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;


public class LetterSetUpForm extends GenericEntityForm<LetterSetUpEntity> {
    private final LetterSetUpService service;
    private final Runnable onChange;

    public LetterSetUpForm(LetterSetUpService service, Runnable onChange) {
        super(LetterSetUpEntity.class);
        this.service = service;
        this.onChange = onChange;
    }

    @Override
    protected void onSave(LetterSetUpEntity entity) {
        service.save(entity);
        onChange.run();
        Notification.show("Letter SetUp saved", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }
}
