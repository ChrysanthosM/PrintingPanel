package org.masouras.app.setup.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.masouras.app.base.comp.GenericCrudView;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Route("letterSetUp")
@PageTitle("Letter SetUp")
@Menu(order = 1, icon = "vaadin:envelopes", title = "Letter SetUp")
@UIScope
@Component
public class LetterSetUpCRUD extends GenericCrudView<LetterSetUpEntity> {
    private final LetterSetUpService service;

    @Autowired
    public LetterSetUpCRUD(LetterSetUpService service, LetterSetUpForm form) {
        super(form);
        form.setOnSaveCallback(this::updateList);
        this.service = service;
    }

    @Override
    protected void addGridColumns(Grid<LetterSetUpEntity> grid) {
        grid.addColumn(e -> e.getId().getLetterType()).setHeader("Letter Type");
        grid.addColumn(e -> e.getId().getSeqNo()).setHeader("Seq No");
        grid.addColumn(LetterSetUpEntity::getXslType).setHeader("XSL Type");
        grid.addColumn(LetterSetUpEntity::getRendererType).setHeader("Renderer");
        grid.addColumn(LetterSetUpEntity::getValidFlag).setHeader("Valid");
    }

    @Override
    protected List<LetterSetUpEntity> fetchItems() {
        return service.findAll();
    }

    @Override
    protected void deleteItem(LetterSetUpEntity entity) {
        service.deleteById(entity.getId());
    }

    @Override
    protected LetterSetUpEntity createNewEntity() {
        return new LetterSetUpEntity();
    }
}
