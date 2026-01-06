package org.masouras.app.setup.ui;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.masouras.app.base.comp.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class LetterSetUpForm extends GenericEntityForm<LetterSetUpEntity> {
    private final LetterSetUpService service;
    @Setter private Runnable onSaveCallback;

    public LetterSetUpForm(LetterSetUpService service) {
        super(LetterSetUpEntity.class);
        this.service = service;
    }

    @Override
    protected void onSave(LetterSetUpEntity entity) {
        service.save(entity);
        if (onSaveCallback != null) onSaveCallback.run();
        Notification.show("Letter SetUp saved", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }
}
