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

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Letter SetUp")
@Menu(order = 0, icon = "vaadin:envelopes", title = "Letter SetUp")
@RequiredArgsConstructor
public class LetterSetUpCRUD extends VerticalLayout {
    private final LetterSetUpService letterSetUpService;
    private final Grid<LetterSetUpEntity> entityGrid = new Grid<>();

    private LetterSetUpForm entityForm;

    @PostConstruct
    private void init() {
        setSizeFull();
        configureGrid();
        configureForm();

        add(new Button(new Icon(VaadinIcon.FILE_ADD), e -> addEntity()), getHorizontalLayout());
        updateList();
    }

    private @NonNull HorizontalLayout getHorizontalLayout() {
        HorizontalLayout layout = new HorizontalLayout(entityGrid, entityForm);
        layout.setFlexGrow(2, entityGrid);
        layout.setFlexGrow(1, entityForm);
        layout.setSizeFull();
        return layout;
    }

    private void configureGrid() {
        entityGrid.setItems(query -> letterSetUpService.list(toSpringPageRequest(query)).stream());

        entityGrid.addColumn(letterSetUpEntity -> letterSetUpEntity.getId().getLetterType()).setHeader("Letter Type");
        entityGrid.addColumn(letterSetUpEntity -> letterSetUpEntity.getId().getSeqNo()).setHeader("Seq No");
        entityGrid.addColumn(LetterSetUpEntity::getXslType).setHeader("XSL");
        entityGrid.addColumn(LetterSetUpEntity::getRendererType).setHeader("Renderer");
        entityGrid.addColumn(LetterSetUpEntity::getValidFlag).setHeader("Action");

        entityGrid.addColumn(new ComponentRenderer<>(letter -> new HorizontalLayout(
                new Button(new Icon(VaadinIcon.EDIT), e -> editEntity(letter)),
                new Button(new Icon(VaadinIcon.TRASH), e -> {
            letterSetUpService.deleteById(letter.getId());
            updateList();
        })))).setHeader("Actions").setAutoWidth(true);

        entityGrid.setEmptyStateText("You have no Letter SetUp yet");
        entityGrid.setSizeFull();
        entityGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
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
