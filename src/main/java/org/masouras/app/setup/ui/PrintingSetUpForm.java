package org.masouras.app.setup.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.masouras.model.mssql.schema.jpa.boundary.PrintingSetUpService;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingSetUpKey;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.*;
import org.masouras.model.mssql.schema.qb.structure.DbField;


public class PrintingSetUpForm extends FormLayout {
    private final Runnable onChangeRunPointer;
    private final PrintingSetUpService printingSetUpService;

    private PrintingSetUpEntity printingSetUpEntity;
    private final ComboBox<ActivityType> activityTypeComboBox = new ComboBox<>();
    private final ComboBox<ContentType> contentTypeComboBox = new ComboBox<>();
    private final IntegerField seqNo = new IntegerField();
    private final ComboBox<LetterType> letterTypeComboBox = new ComboBox<>();

    public PrintingSetUpForm(Runnable onChangeRunPointer, PrintingSetUpService printingSetUpService) {
        this.onChangeRunPointer = onChangeRunPointer;
        this.printingSetUpService = printingSetUpService;
        init();
    }
    private void init() {
        loadComponents();
        add(activityTypeComboBox, contentTypeComboBox, seqNo, letterTypeComboBox,
                new HorizontalLayout(
                        new Button(new Icon(VaadinIcon.DISC), e -> saveEntity()),
                        new Button("Cancel", e -> setEntity(null))));
    }
    private void loadComponents() {
        activityTypeComboBox.setPlaceholder("Enter " + DbField.ACTIVITY_TYPE.asAlias());
        activityTypeComboBox.setAriaLabel(DbField.ACTIVITY_TYPE.asAlias());
        activityTypeComboBox.setMinWidth("20em");
        activityTypeComboBox.setItems(ActivityType.values());

        contentTypeComboBox.setPlaceholder("Enter " + DbField.CONTENT_TYPE.asAlias());
        contentTypeComboBox.setAriaLabel(DbField.CONTENT_TYPE.asAlias());
        contentTypeComboBox.setMinWidth("20em");
        contentTypeComboBox.setItems(ContentType.values());

        seqNo.setPlaceholder("Enter " + DbField.SEQ_NO.asAlias());
        seqNo.setAriaLabel(DbField.SEQ_NO.asAlias());
        seqNo.setMin(1);
        seqNo.setMax(999);

        letterTypeComboBox.setPlaceholder("Enter " + DbField.LETTER_TYPE.asAlias());
        letterTypeComboBox.setAriaLabel(DbField.LETTER_TYPE.asAlias());
        letterTypeComboBox.setMinWidth("20em");
        letterTypeComboBox.setItems(LetterType.values());
    }

    public void setEntity(PrintingSetUpEntity entity) {
        this.printingSetUpEntity = entity;
        if (entity == null) {
            setVisible(false);
            clearComponents();
            return;
        }
        setEntityMain();
        setVisible(true);
    }
    private void setEntityMain() {
        if (this.printingSetUpEntity.getId() == null) {
            clearComponents();
            return;
        }
        activityTypeComboBox.setValue(this.printingSetUpEntity.getId().getActivityType());
        contentTypeComboBox.setValue(this.printingSetUpEntity.getId().getContentType());
        seqNo.setValue(this.printingSetUpEntity.getId().getSeqNo());
        letterTypeComboBox.setValue(this.printingSetUpEntity.getLetterType());
    }

    private void saveEntity() {
        printingSetUpService.save(new PrintingSetUpEntity(
                new PrintingSetUpKey(activityTypeComboBox.getValue(), contentTypeComboBox.getValue(), seqNo.getValue()),
                letterTypeComboBox.getValue()));
        onChangeRunPointer.run();
        setEntity(null);
        Notification.show("Printing SetUp added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void clearComponents() {
        activityTypeComboBox.clear();
        contentTypeComboBox.clear();
        seqNo.clear();
        letterTypeComboBox.clear();
    }
}
