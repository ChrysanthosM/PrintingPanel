package org.masouras.app.setup.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.masouras.model.mssql.schema.jpa.boundary.LetterSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.LetterSetUpKey;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.LetterType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.RendererType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ValidFlag;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.XslType;
import org.masouras.model.mssql.schema.qb.structure.DbField;


public class LetterSetUpForm extends FormLayout {
    private final Runnable onChangeRunPointer;
    private final LetterSetUpService letterSetUpService;

    private LetterSetUpEntity letterSetUpEntity;
    private final ComboBox<LetterType> letterTypeComboBox = new ComboBox<>();
    private final IntegerField seqNo = new IntegerField();
    private final ComboBox<XslType> xslTypeComboBox = new ComboBox<>();
    private final ComboBox<RendererType> rendererTypeComboBox = new ComboBox<>();
    private final ComboBox<ValidFlag> validFlagComboBox = new ComboBox<>();

    public LetterSetUpForm(Runnable onChangeRunPointer, LetterSetUpService letterSetUpService) {
        this.onChangeRunPointer = onChangeRunPointer;
        this.letterSetUpService = letterSetUpService;
        init();
    }
    private void init() {
        loadComponents();
        add(letterTypeComboBox, seqNo, xslTypeComboBox, rendererTypeComboBox, validFlagComboBox,
                new HorizontalLayout(
                        new Button(new Icon(VaadinIcon.DISC), e -> saveEntity()),
                        new Button("Cancel", e -> setEntity(null))));
    }
    private void loadComponents() {
        letterTypeComboBox.setPlaceholder("Enter " + DbField.LETTER_TYPE.asAlias());
        letterTypeComboBox.setAriaLabel(DbField.LETTER_TYPE.asAlias());
        letterTypeComboBox.setMinWidth("20em");
        letterTypeComboBox.setItems(LetterType.values());

        seqNo.setPlaceholder("Enter " + DbField.SEQ_NO.asAlias());
        seqNo.setAriaLabel(DbField.SEQ_NO.asAlias());
        seqNo.setMin(1);
        seqNo.setMax(999);

        xslTypeComboBox.setPlaceholder("Enter " + DbField.XSL_TYPE.asAlias());
        xslTypeComboBox.setAriaLabel(DbField.XSL_TYPE.asAlias());
        xslTypeComboBox.setMinWidth("20em");
        xslTypeComboBox.setItems(XslType.values());

        rendererTypeComboBox.setPlaceholder("Enter " + DbField.RENDERER_TYPE.asAlias());
        rendererTypeComboBox.setAriaLabel(DbField.RENDERER_TYPE.asAlias());
        rendererTypeComboBox.setMinWidth("20em");
        rendererTypeComboBox.setItems(RendererType.values());

        validFlagComboBox.setPlaceholder("Enter " + DbField.VALID_FLAG.asAlias());
        validFlagComboBox.setAriaLabel(DbField.VALID_FLAG.asAlias());
        validFlagComboBox.setMinWidth("20em");
        validFlagComboBox.setItems(ValidFlag.values());
    }

    public void setEntity(LetterSetUpEntity letterSetUpEntity) {
        this.letterSetUpEntity = letterSetUpEntity;
        if (letterSetUpEntity == null) {
            setVisible(false);
            return;
        }
        setEntityMain();
        setVisible(true);
    }
    private void setEntityMain() {
        if (this.letterSetUpEntity.getId() == null) {
            letterTypeComboBox.clear();
            seqNo.clear();
            xslTypeComboBox.clear();
            rendererTypeComboBox.clear();
            validFlagComboBox.clear();
            return;
        }
        letterTypeComboBox.setValue(this.letterSetUpEntity.getId().getLetterType());
        seqNo.setValue(this.letterSetUpEntity.getId().getSeqNo());
        xslTypeComboBox.setValue(this.letterSetUpEntity.getXslType());
        rendererTypeComboBox.setValue(this.letterSetUpEntity.getRendererType());
        validFlagComboBox.setValue(this.letterSetUpEntity.getValidFlag());
    }

    private void saveEntity() {
        letterSetUpService.save(new LetterSetUpEntity(
                new LetterSetUpKey(letterTypeComboBox.getValue(), seqNo.getValue()),
                xslTypeComboBox.getValue(),
                rendererTypeComboBox.getValue(),
                validFlagComboBox.getValue()));
        onChangeRunPointer.run();
        setEntity(null);
    }
}
