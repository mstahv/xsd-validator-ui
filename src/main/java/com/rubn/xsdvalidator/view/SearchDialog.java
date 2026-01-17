package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.util.SvgFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;
import java.util.function.Consumer;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE_ITEMS;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;

/**
 * @author rubn
 */
public class SearchDialog extends Dialog {

    private final ListBox<String> listBox = new ListBox<>();
    private final List<String> allXsdXmlFiles;
    private final Consumer<String> onSelectCallback;

    public SearchDialog(List<String> xsdFiles, Consumer<String> onSelectCallback) {
        this.allXsdXmlFiles = xsdFiles;
        this.onSelectCallback = onSelectCallback;
        addClassName("search-dialog-content");
        setWidth("500px");

        final TextField searchField = new TextField();
        searchField.setPlaceholder("Filter [xsd, xml]");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> filterList(e.getValue()));
        searchField.setClearButtonVisible(true);

        listBox.setItems(xsdFiles);
        listBox.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE_ITEMS);
        listBox.setHeight("300px");
        listBox.setWidthFull();

        // Add renderer for icon
        listBox.setRenderer(new ComponentRenderer<>(paramfileName -> {
            String fileName = paramfileName.contains(XML)
                    ? "file-xml-icon.svg"
                    : "xsd.svg";
            SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
            icon.setSize("40px");

            icon.getStyle().set("margin-right", "8px");
            //.set("color", "gray");
            return new Span(icon, new Span(paramfileName));
        }));

        listBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                onSelectCallback.accept(event.getValue());
                this.close();
            }
        });

        VerticalLayout layout = new VerticalLayout(searchField, new Hr(), listBox);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        super.add(layout);

        // Focus search on open
        searchField.focus();
    }

    private void filterList(String filterText) {
        List<String> filtered = allXsdXmlFiles.stream()
                .filter(name -> name.toLowerCase().contains(filterText.toLowerCase()))
                .toList();
        listBox.setItems(filtered);
    }
}