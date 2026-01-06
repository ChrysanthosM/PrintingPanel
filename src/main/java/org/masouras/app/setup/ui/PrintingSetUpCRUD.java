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
import org.masouras.model.mssql.schema.jpa.boundary.PrintingSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;
import org.masouras.model.mssql.schema.qb.structure.DbField;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("printingSetUp")
@PageTitle("Printing SetUp")
@Menu(order = 2, icon = "vaadin:cube", title = "Printing SetUp")
@RequiredArgsConstructor
public class PrintingSetUpCRUD extends VerticalLayout {
    private final PrintingSetUpService printingSetUpService;
    private final Grid<PrintingSetUpEntity> entityGrid = new Grid<>();

    private PrintingSetUpForm entityForm;

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
        entityGrid.setAriaLabel("Printing SetUp");
        entityGrid.setEmptyStateText("You have no Printing SetUp yet");
        entityGrid.setSizeFull();
        entityGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        entityGrid.setItems(query -> printingSetUpService.list(toSpringPageRequest(query)).stream());

        entityGrid.addColumn(printingSetUpEntity -> printingSetUpEntity.getId().getActivityType()).setHeader(DbField.ACTIVITY_TYPE.asAlias());
        entityGrid.addColumn(printingSetUpEntity -> printingSetUpEntity.getId().getContentType()).setHeader(DbField.CONTENT_TYPE.asAlias());
        entityGrid.addColumn(printingSetUpEntity -> printingSetUpEntity.getId().getSeqNo()).setHeader(DbField.SEQ_NO.asAlias());
        entityGrid.addColumn(PrintingSetUpEntity::getLetterType).setHeader(DbField.LETTER_TYPE.asAlias());

        entityGrid.addColumn(new ComponentRenderer<>(entity -> new HorizontalLayout(
                new Button(new Icon(VaadinIcon.EDIT), e -> editEntity(entity)),
                new Button(new Icon(VaadinIcon.TRASH), e -> {
            printingSetUpService.deleteById(entity.getId());
            updateList();
        })))).setHeader("Actions").setAutoWidth(true);
    }
    private void configureForm() {
        entityForm = new PrintingSetUpForm(printingSetUpService, this::updateList);
        entityForm.setVisible(false);
    }

    private void updateList() {
        entityGrid.setItems(printingSetUpService.findAll());
    }

    private void addEntity() {
        entityForm.setEntity(new PrintingSetUpEntity());
    }
    private void editEntity(PrintingSetUpEntity entity) {
        entityForm.setEntity(entity);
    }
}
