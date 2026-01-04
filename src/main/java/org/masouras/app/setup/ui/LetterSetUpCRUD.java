package org.masouras.app.setup.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.masouras.model.mssql.schema.qb.structure.DbField;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("letterSetUp")
@PageTitle("Letter SetUp")
@Menu(order = 1, icon = "vaadin:envelopes", title = "Letter SetUp")
@RequiredArgsConstructor
public class LetterSetUpCRUD extends VerticalLayout {
    private final LetterSetUpService letterSetUpService;
    private final Grid<LetterSetUpEntity> entityGrid = new Grid<>();

    private LetterSetUpForm entityForm;

    @PostConstruct
    private void init() {
        setStyle();
        configureGrid();
        configureForm();

        add(new Button(new Icon(VaadinIcon.FILE_ADD), e -> addEntity()), getFormLayout());
        updateList();
    }
    private void setStyle() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }
    private @NonNull VerticalLayout getFormLayout() {
        VerticalLayout layout = new VerticalLayout(entityGrid, entityForm);
        layout.setFlexGrow(2, entityGrid);
        layout.setFlexGrow(1, entityForm);
        layout.setSizeFull();
        return layout;
    }
    private void configureGrid() {
        entityGrid.asSingleSelect();
        entityGrid.setAriaLabel("Letter SetUp");
        entityGrid.setEmptyStateText("You have no Letter SetUp yet");
        entityGrid.setSizeFull();
        entityGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        entityGrid.setItems(query -> letterSetUpService.list(toSpringPageRequest(query)).stream());

        entityGrid.addColumn(letterSetUpEntity -> letterSetUpEntity.getId().getLetterType()).setHeader(DbField.LETTER_TYPE.asAlias());
        entityGrid.addColumn(letterSetUpEntity -> letterSetUpEntity.getId().getSeqNo()).setHeader(DbField.SEQ_NO.asAlias());
        entityGrid.addColumn(LetterSetUpEntity::getXslType).setHeader(DbField.XSL_TYPE.asAlias());
        entityGrid.addColumn(LetterSetUpEntity::getRendererType).setHeader(DbField.RENDERER_TYPE.asAlias());
        entityGrid.addColumn(LetterSetUpEntity::getValidFlag).setHeader(DbField.VALID_FLAG.asAlias());

        entityGrid.addColumn(new ComponentRenderer<>(entity -> new HorizontalLayout(
                new Button(new Icon(VaadinIcon.EDIT), e -> editEntity(entity)),
                new Button(new Icon(VaadinIcon.TRASH), e -> {
            letterSetUpService.deleteById(entity.getId());
            updateList();
        })))).setHeader("Actions").setAutoWidth(true);
    }
    private void configureForm() {
        entityForm = new LetterSetUpForm(this::updateList, letterSetUpService);
        entityForm.setVisible(false);
    }

    private void updateList() {
        entityGrid.setItems(letterSetUpService.findAll());
    }

    private void addEntity() {
        entityForm.setEntity(new LetterSetUpEntity());
    }
    private void editEntity(LetterSetUpEntity letterSetUpEntity) {
        entityForm.setEntity(letterSetUpEntity);
    }
}
