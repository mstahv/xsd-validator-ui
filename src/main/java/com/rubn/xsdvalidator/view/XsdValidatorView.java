package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.service.DecompressionService;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
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

    private TextField buildSearch(Input input) {
        final TextField searchField = new TextField();
        searchField.setWidth("500px");
        searchField.setClearButtonVisible(true);
        final SearchPopover searchPopover = input.buildPopover(searchField);
        searchPopover.setTarget(searchField);

        searchField.getStyle().setCursor(CURSOR_POINTER);
        //buttonSearchXsd.getStyle().setBorder("1px var(--lumo-utility-border-style,solid) var(--lumo-utility-border-color,var(--lumo-contrast-10pct))");
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        final Span spanShortCut = new Span("Ctrl K");
        spanShortCut.getElement().getThemeList().add("badge small pill constrast");
        spanShortCut.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        Shortcuts.addShortcutListener(searchField, listener -> searchPopover.open(),
                Key.KEY_K, KeyModifier.CONTROL);

        final Span animatedText = new Span();
        animatedText.addClassName("search-animation");

        final Icon icon = VaadinIcon.SEARCH.create();
        icon.addClassNames(LumoUtility.FontSize.SMALL);

        var row = new HorizontalLayout(icon, animatedText);
        row.setSpacing("var(--lumo-space-xs)");
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        searchField.setPrefixComponent(row);
        searchField.setSuffixComponent(spanShortCut);
        searchField.addClassName(LumoUtility.FontSize.SMALL);
        searchField.setTooltipText("Search xsd or xml");

        return searchField;
    }

}
