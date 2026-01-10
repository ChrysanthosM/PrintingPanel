package org.masouras.app.base.element.component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;

public class PaginationBar extends HorizontalLayout {
    public static class PageChangeEvent extends ComponentEvent<PaginationBar> {
        public PageChangeEvent(PaginationBar source) {
            super(source, false);
        }
    }

    @Getter private final int pageSize;
    @Getter private int currentPage;
    private int totalPages;

    private final Span paginationInfo = new Span();
    private final Button firstBtn = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT), _ -> goToPage(0));
    private final Button prevBtn = new Button(new Icon(VaadinIcon.ANGLE_LEFT), _ -> goToPage(currentPage - 1));
    private final Button nextBtn = new Button(new Icon(VaadinIcon.ANGLE_RIGHT), _ -> goToPage(currentPage + 1));
    private final Button lastBtn = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT), _ -> goToPage(totalPages - 1));

    public PaginationBar(int pageSize) {
        this.pageSize = pageSize;

        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(firstBtn, prevBtn, paginationInfo, nextBtn, lastBtn);
    }

    public void addPageChangeListener(ComponentEventListener<PageChangeEvent> listener) {
        addListener(PageChangeEvent.class, listener);
    }
    private void goToPage(int page) {
        if (page < 0 || page >= totalPages) return;
        currentPage = page;
        fireEvent(new PageChangeEvent(this));
    }

    public void updatePaginationBar(int totalItems, int totalPages) {
        this.totalPages = totalPages;

        int start = currentPage * pageSize + 1;
        int end = Math.min((currentPage + 1) * pageSize, totalItems);
        paginationInfo.setText("Rows: " + start + "–" + end + " of " + totalItems);

        firstBtn.setEnabled(currentPage > 0);
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(currentPage < this.totalPages - 1);
        lastBtn.setEnabled(currentPage < this.totalPages - 1);
    }
}
