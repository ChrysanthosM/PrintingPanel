package org.masouras.app.setup.ui;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class LetterSetUpForm extends GenericEntityForm<LetterSetUpEntity> {
    private final LetterSetUpService service;

    @Autowired
    public LetterSetUpForm(LetterSetUpService service) {
        super(LetterSetUpEntity.class);
        this.service = service;
    }

    @Override
    protected void onSave(LetterSetUpEntity entity) {
        service.save(entity);
        super.onSaveMain("Letter SetUp saved");
    }
}
