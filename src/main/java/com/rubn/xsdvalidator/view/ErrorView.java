package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;

/**
 * View shown when trying to navigate to a view that does not exist using
 */
@AnonymousAllowed
public class ErrorView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private final Span explanation = new Span();

    public ErrorView() {
        super.setSizeFull();
        explanation.addClassName(LumoUtility.TextColor.SECONDARY);
        final HorizontalLayout rowHeader = this.buildHeaderText();
        super.add(rowHeader);

        final Image imageBrokenRelay = this.buildCenterImage();

        final Anchor anchor = this.buildAnchor();
        super.add(explanation, imageBrokenRelay, anchor);
        super.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    private @NonNull HorizontalLayout buildHeaderText() {
        final Icon iconForBack = this.buildBackIcon();
        final H1 headerNotFound = new H1("Not found!");
        headerNotFound.addClassName(LumoUtility.Margin.Right.AUTO);
        final HorizontalLayout rowHeader = new HorizontalLayout(iconForBack, headerNotFound);
        rowHeader.setSpacing(false);
        rowHeader.setWidthFull();
        return rowHeader;
    }

    private @NonNull Image buildCenterImage() {
        final Image imageBrokenRelay = new Image("404-image.png", "404");
        imageBrokenRelay.setWidth("400px");
        imageBrokenRelay.addClassName("error-image");
        Tooltip.forComponent(imageBrokenRelay).setText("Not found!");
        imageBrokenRelay.getStyle().setCursor(CURSOR_POINTER);
        imageBrokenRelay.addClickListener(event -> UI.getCurrent().navigate("/"));
        return imageBrokenRelay;
    }

    private Anchor buildAnchor() {
        final Anchor anchor = new Anchor("/", "back to home...");
        anchor.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.BOLD);
        return anchor;
    }

    private Icon buildBackIcon() {
        final Icon iconForBack = VaadinIcon.ARROW_LEFT.create();
        Tooltip.forComponent(iconForBack)
                .withPosition(Tooltip.TooltipPosition.BOTTOM_END)
                .withText("Back - ESC");
        iconForBack.getStyle().setCursor(CURSOR_POINTER);
        iconForBack.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Margin.Right.AUTO);
        iconForBack.addClickListener(event -> UI.getCurrent().navigate("/"));
        iconForBack.addClickShortcut(Key.ESCAPE);
        return iconForBack;
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        explanation.setText("Could not navigate to '" + event.getLocation().getPath() + "'.");
        return HttpServletResponse.SC_NOT_FOUND;
    }
}