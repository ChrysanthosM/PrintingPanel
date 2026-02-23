package org.masouras.app.base.element.util;

import com.vaadin.copilot.shaded.commons.lang3.StringUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class VaadinButtonFactory {

    public static Button createButton(@Nullable String text, Icon icon, @Nullable String tooltip, ComponentEventListener<ClickEvent<Button>> listener, ButtonVariant... variants) {
        Button button = StringUtils.isNotBlank(text) ? new Button(text, icon, listener) : new Button(icon, listener);
        Optional.ofNullable(variants).filter(v -> v.length > 0).ifPresent(button::addThemeVariants);
        if (StringUtils.isNotBlank(tooltip)) button.getElement().setProperty("title", tooltip);
        return button;
    }


}
