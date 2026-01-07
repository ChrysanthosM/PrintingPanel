package org.masouras.app.setup.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpKey;
import org.masouras.model.mssql.schema.qb.structure.DbField;
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
//
//    @Override
//    protected void addGridColumns(Grid<LetterSetUpEntity> grid) {
//        grid.addColumn(e -> e.getId().getLetterType()).setHeader(DbField.LETTER_TYPE.asAlias());
//        grid.addColumn(e -> e.getId().getSeqNo()).setHeader(DbField.SEQ_NO.asAlias());
//        grid.addColumn(LetterSetUpEntity::getXslType).setHeader(DbField.OPTION_TYPE.asAlias());
//        grid.addColumn(LetterSetUpEntity::getRendererType).setHeader(DbField.RENDERER_TYPE.asAlias());
//        grid.addColumn(LetterSetUpEntity::getValidFlag).setHeader(DbField.VALID_FLAG.asAlias());
//    }
}
