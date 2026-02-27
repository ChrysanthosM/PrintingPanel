package org.masouras.app.base.element.util;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class VaadinNotificationFactory {

    public static void showErrorNotification(@NonNull String message, Throwable err) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(0);

        HorizontalLayout layout = new HorizontalLayout(
                new Text(message + StringUtils.SPACE + err.getMessage()),
                VaadinButtonFactory.createButton("Close", new Icon(VaadinIcon.CLOSE), "Close",
                        _ -> notification.close(), ButtonVariant.LUMO_ERROR));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        notification.add(layout);

        notification.open();
    }

}
