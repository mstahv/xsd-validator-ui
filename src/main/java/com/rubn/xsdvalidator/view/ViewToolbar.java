package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.jspecify.annotations.Nullable;

public final class ViewToolbar extends Composite<HorizontalLayout> {

    public ViewToolbar(@Nullable String viewTitle, Component... components) {
        var layout = getContent();
        layout.setPadding(false);
        layout.setWidthFull();
        layout.addClassName(LumoUtility.Border.BOTTOM);

        var drawerToggle = new DrawerToggle();
        drawerToggle.addClassNames(LumoUtility.Margin.NONE);

        var title = new H1(viewTitle);
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE, LumoUtility.FontWeight.LIGHT);

        var toggleAndTitle = new HorizontalLayout(drawerToggle, title);
        toggleAndTitle.setId("toggle-and-title");
        toggleAndTitle.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toggleAndTitle.setWidth("20%");
        layout.add(toggleAndTitle);

        if (components.length > 0) {
            var actions = new HorizontalLayout(components);
            actions.setWidthFull();
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            actions.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.add(actions);
        }
    }

}
