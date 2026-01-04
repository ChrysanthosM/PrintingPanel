package org.masouras.app.setup.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Printing Maintenance")
@Menu(order = 0, icon = "vaadin:home", title = "Printing Maintenance")
public class MainMenuView extends VerticalLayout {
    public MainMenuView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSpacing(true);

        add(new H1("Printing Maintenance"));
    }
}

