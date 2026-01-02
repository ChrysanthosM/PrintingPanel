package org.masouras.app.setup.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
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

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Letter SetUp")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Letter SetUp")
@RequiredArgsConstructor
class LetterSetUpView extends VerticalLayout {
    private final LetterSetUpService letterSetUpService;

    private final Grid<LetterSetUpEntity> letterSetUpGrid = new Grid<>();
    private final Button createBtn = new Button("Create", event -> createLetterSetUp());

    private final ComboBox<LetterType> letterTypeComboBox = new ComboBox<>();
    private final NumberField seqNo = new NumberField();
    private final ComboBox<XslType> xslTypeComboBox = new ComboBox<>();
    private final ComboBox<RendererType> rendererTypeComboBox = new ComboBox<>();
    private final ComboBox<ValidFlag> validFlagComboBox = new ComboBox<>();

    @PostConstruct
    private void init() {
        loadComponents();
        styleComponents();
        addComponents();
    }
    private void loadComponents() {
        letterTypeComboBox.setPlaceholder("Enter Letter Type");
        letterTypeComboBox.setAriaLabel("Letter Type");
        letterTypeComboBox.setMinWidth("20em");
        letterTypeComboBox.setItems(LetterType.values());

        seqNo.setPlaceholder("Enter Sequence Number");
        seqNo.setAriaLabel("Sequence Number");
        seqNo.setMin(1);
        seqNo.setMax(999);

        xslTypeComboBox.setPlaceholder("Enter XSL Type");
        xslTypeComboBox.setAriaLabel("XSL Type");
        xslTypeComboBox.setItems(XslType.values());
        xslTypeComboBox.setMinWidth("20em");

        rendererTypeComboBox.setPlaceholder("Enter Renderer Type");
        rendererTypeComboBox.setAriaLabel("Renderer Type");
        rendererTypeComboBox.setItems(RendererType.values());
        rendererTypeComboBox.setMinWidth("20em");

        validFlagComboBox.setPlaceholder("Enter Action Type");
        validFlagComboBox.setAriaLabel("Action Type");
        validFlagComboBox.setItems(ValidFlag.values());
        validFlagComboBox.setMinWidth("20em");


        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.setEnabled(false);
        letterTypeComboBox.addValueChangeListener(e -> validateForm());
        seqNo.addValueChangeListener(e -> validateForm());
        xslTypeComboBox.addValueChangeListener(e -> validateForm());
        rendererTypeComboBox.addValueChangeListener(e -> validateForm());
        validFlagComboBox.addValueChangeListener(e -> validateForm());

        letterSetUpGrid.setItems(query -> letterSetUpService.list(toSpringPageRequest(query)).stream());
        letterSetUpGrid.addColumn(item -> item.getId().getLetterType()).setHeader("Letter Type");
        letterSetUpGrid.addColumn(item -> item.getId().getSeqNo()).setHeader("Seq No");
        letterSetUpGrid.addColumn(LetterSetUpEntity::getXslType).setHeader("XSL");
        letterSetUpGrid.addColumn(LetterSetUpEntity::getRendererType).setHeader("Renderer");
        letterSetUpGrid.addColumn(LetterSetUpEntity::getValidFlag).setHeader("Action");

        letterSetUpGrid.setEmptyStateText("You have no Letter SetUp yet");
        letterSetUpGrid.setSizeFull();
        letterSetUpGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
    }
    private void styleComponents() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);
    }
    private void addComponents() {
        add(new ViewToolbar("Letter SetUp", ViewToolbar.group(letterTypeComboBox, seqNo, xslTypeComboBox, rendererTypeComboBox, validFlagComboBox, createBtn)));
        add(letterSetUpGrid);
    }
    private void validateForm() {
        createBtn.setEnabled(
                letterTypeComboBox.getValue() != null
                        && seqNo.getValue() != null
                        && xslTypeComboBox.getValue() != null
                        && rendererTypeComboBox.getValue() != null
                        && validFlagComboBox.getValue() != null
        );
    }


    private void createLetterSetUp() {
        letterSetUpService.save(new LetterSetUpEntity(
                new LetterSetUpKey(letterTypeComboBox.getValue(), seqNo.getValue().intValue()),
                xslTypeComboBox.getValue(),
                rendererTypeComboBox.getValue(),
                validFlagComboBox.getValue()));
        letterSetUpGrid.getDataProvider().refreshAll();
        letterTypeComboBox.clear();
        seqNo.clear();
        xslTypeComboBox.clear();
        rendererTypeComboBox.clear();
        validFlagComboBox.clear();
        Notification.show("Letter SetUp added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

}
