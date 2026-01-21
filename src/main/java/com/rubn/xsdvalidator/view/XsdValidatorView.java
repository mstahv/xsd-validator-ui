package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.service.DecompressionService;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.lang3.StringUtils;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.COPY_TO_CLIPBOARD;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;

@UIScope
@SpringComponent
@Route("")
@RouteAlias("xsd-validator")
@PageTitle("xsd-validator")
@Menu(order = 1, icon = "vaadin:clipboard-check", title = "XSD Validator")
@JsModule(COPY_TO_CLIPBOARD)
class XsdValidatorView extends Main {

    public XsdValidatorView(final ValidationXsdSchemaService validationXsdSchemaService,
                            final DecompressionService decompressionService) {
        setSizeFull();
        getStyle().setOverflow(Style.Overflow.VISIBLE);


        final Input input = new Input(validationXsdSchemaService, decompressionService);
        add(new ViewToolbar(StringUtils.EMPTY, this.buildSearch(input), this.createInfoIcon()));
        add(input);
    }

    private Span createInfoIcon() {
        final Span span = new Span();
        span.getStyle().setCursor(CURSOR_POINTER);
        Tooltip.forComponent(span).setText("Show info");
        span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.AlignSelf.CENTER, LumoUtility.Margin.Right.SMALL);
        span.add(VaadinIcon.INFO_CIRCLE.create());
        span.addClickListener(event -> {
            new AboutDialog().open();
        });
        return span;
    }

    private Button buildSearch(Input input) {
        final Button buttonSearchXsd = new Button();
        Span animatedText = new Span();
        animatedText.addClassName("search-animation");
        buttonSearchXsd.getStyle().setCursor(CURSOR_POINTER);
        buttonSearchXsd.getStyle().setBorder("1px var(--lumo-utility-border-style,solid) var(--lumo-utility-border-color,var(--lumo-contrast-10pct))");
        buttonSearchXsd.addClassName(LumoUtility.TextColor.SECONDARY);
        Span spanShortCut = new Span("Ctrl K");
        spanShortCut.getElement().getThemeList().add("badge small pill constrast");
        spanShortCut.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        Shortcuts.addShortcutListener(buttonSearchXsd, input::openXsdSearchDialog,
                Key.KEY_K, KeyModifier.CONTROL);
        var row = new HorizontalLayout(new Span("Search:"), animatedText, spanShortCut);
        row.setSpacing("var(--lumo-space-xs)");
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        buttonSearchXsd.setSuffixComponent(row);
        buttonSearchXsd.setPrefixComponent(VaadinIcon.SEARCH.create());
        buttonSearchXsd.addThemeVariants(ButtonVariant.LUMO_SMALL);
        buttonSearchXsd.setTooltipText("Search xsd or xml");
        buttonSearchXsd.addClickListener(e -> input.openXsdSearchDialog());
        return buttonSearchXsd;
    }

}
