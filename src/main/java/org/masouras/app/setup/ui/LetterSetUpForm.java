package org.masouras.app.setup.ui;

import com.vaadin.flow.spring.annotation.UIScope;
import org.jspecify.annotations.Nullable;
import org.masouras.app.base.comp.GenericEntityForm;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class LetterSetUpForm extends GenericEntityForm<LetterSetUpEntity, LetterSetUpKey> {

    @Autowired
    public LetterSetUpForm(LetterSetUpService service) {
        super(LetterSetUpEntity.class, service);
    }

    @Override
    protected @Nullable String getOnSaveGetNotificationMessage() {
        return "Letter SetUp saved";
    }
}
