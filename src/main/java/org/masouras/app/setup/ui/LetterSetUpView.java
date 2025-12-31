package org.masouras.app.setup.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.masouras.app.base.ui.ViewToolbar;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpKey;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.LetterType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.RendererType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ValidFlag;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.XslType;

import java.util.Objects;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Letter SetUp")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Letter SetUp")
@RequiredArgsConstructor
class LetterSetUpView extends VerticalLayout {
    private final LetterSetUpService letterSetUpService;

    private final Grid<LetterSetUpEntity> letterSetUpGrid = new Grid<>();
    private final Button createBtn = new Button("Create", event -> createLetterSetUp());


    private final TextField letterType = new TextField();
    private final NumberField seqNo = new NumberField();
    private final TextField xslType = new TextField();
    private final TextField rendererType = new TextField();
    private final TextField actionType = new TextField();

    @PostConstruct
    private void init() {
        load();
        style();
        show();
    }
    private void load() {
        letterType.setPlaceholder("Enter Letter Type");
        letterType.setAriaLabel("Letter Type");
        letterType.setMaxLength(5);
        letterType.setMinWidth("20em");

        seqNo.setPlaceholder("Enter Sequence Number");
        seqNo.setAriaLabel("Sequence Number");
        seqNo.setMin(1);
        seqNo.setMax(999);

        xslType.setPlaceholder("Enter XSL Type");
        xslType.setAriaLabel("XSL Type");
        xslType.setMaxLength(50);
        xslType.setMinWidth("20em");

        rendererType.setPlaceholder("Enter Renderer Type");
        rendererType.setAriaLabel("Renderer Type");
        rendererType.setMaxLength(3);
        rendererType.setMinWidth("20em");

        actionType.setPlaceholder("Enter Action Type");
        actionType.setAriaLabel("Action Type");
        actionType.setMaxLength(1);
        actionType.setMinWidth("20em");


        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        letterSetUpGrid.setItems(query -> letterSetUpService.list(toSpringPageRequest(query)).stream());
        letterSetUpGrid.addColumn(LetterSetUpEntity::getXslType).setHeader("XSL");
        letterSetUpGrid.addColumn(LetterSetUpEntity::getRendererType).setHeader("Renderer");
        letterSetUpGrid.addColumn(LetterSetUpEntity::getValidFlag).setHeader("Action");

        letterSetUpGrid.setEmptyStateText("You have no Letter SetUp yet");
        letterSetUpGrid.setSizeFull();
        letterSetUpGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
    }
    private void style() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);
    }
    private void show() {
        add(new ViewToolbar("Letter SetUp", ViewToolbar.group(letterType, seqNo, xslType, rendererType, actionType, createBtn)));
        add(letterSetUpGrid);
    }

    private void createLetterSetUp() {
        letterSetUpService.save(new LetterSetUpEntity(
                new LetterSetUpKey(LetterType.getFromCode(letterType.getValue()), seqNo.getValue().intValue()),
                Objects.requireNonNull(XslType.getFromCode(xslType.getValue())),
                Objects.requireNonNull(RendererType.getFromCode(rendererType.getValue())),
                Objects.requireNonNull(ValidFlag.getFromCode(actionType.getValue()))));
        letterSetUpGrid.getDataProvider().refreshAll();
        letterType.clear();
        seqNo.clear();
        xslType.clear();
        rendererType.clear();
        actionType.clear();
        Notification.show("Letter SetUp added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

}
