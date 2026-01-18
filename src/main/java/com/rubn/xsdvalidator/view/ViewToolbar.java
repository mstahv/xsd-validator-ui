package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
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

        var toggleAndTitle = new HorizontalLayout(drawerToggle);
        toggleAndTitle.setId("toggle-and-title");
        toggleAndTitle.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
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
