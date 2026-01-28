package org.masouras.app.setup.ui.business.domain;

import com.vaadin.flow.component.textfield.TextField;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.masouras.model.mssql.schema.jpa.control.vaadin.FormField;

@Data
@AllArgsConstructor
public class ListToPrintDTO {
    @FormField(component = TextField.class, order = 1, label = "Record ID")
    private Long recId;

    @FormField(component = TextField.class, order = 2, label = "Final Content ID")
    private Long finalContentId;

    @FormField(component = TextField.class, order = 3, label = "Activity Type")
    private String activityType;
}
