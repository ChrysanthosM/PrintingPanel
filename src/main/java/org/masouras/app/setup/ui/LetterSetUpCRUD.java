package org.masouras.app.setup.ui;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Route("letterSetUp")
@PageTitle("Letter SetUp")
@Menu(order = 1, icon = "vaadin:envelopes", title = "Letter SetUp")
@UIScope
@Component
public class LetterSetUpCRUD extends GenericCrudView<LetterSetUpEntity, LetterSetUpKey> {

    @Autowired
    public LetterSetUpCRUD(LetterSetUpService service, LetterSetUpForm form) {
        super(LetterSetUpEntity.class, form, service);
    }
}
