package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.util.SvgFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE_ITEMS;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;

/**
 * @author rubn
 */
public class SearchDialog extends Dialog {

    private final MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
    private final Set<String> currentSelection = new ConcurrentSkipListSet<>();

    private final List<String> allXsdXmlFiles;
    private final String initialXsdSelection;
    private final String initialXmlSelection;

    public SearchDialog(List<String> rawFileList, String initialXsdSelection,
                        String initialXmlSelection,
                        Consumer<Set<String>> onSelectCallback) {
        this.initialXsdSelection = initialXsdSelection;
        this.initialXmlSelection = initialXmlSelection;
        addClassName("search-dialog-content");
        setWidth("500px");

        final TextField searchField = new TextField();
        searchField.setPlaceholder("Filter [xsd, xml]");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> this.filterList(e.getValue()));
        searchField.setClearButtonVisible(true);

        if (initialXsdSelection != null && rawFileList.contains(initialXsdSelection)) {
            currentSelection.add(initialXsdSelection);
        }
        if (initialXmlSelection != null && rawFileList.contains(initialXmlSelection)) {
            currentSelection.add(initialXmlSelection);
        }

        Comparator<String> priorityComparator = Comparator
                .comparingInt((String fileName) -> currentSelection.contains(fileName) ? 0 : 1)
                .thenComparing(item -> item.toLowerCase());

        this.allXsdXmlFiles = rawFileList.stream()
                .sorted(priorityComparator)
                .toList();

        listBox.setItems(this.allXsdXmlFiles);
        listBox.setValue(currentSelection);
        listBox.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE_ITEMS);
        listBox.setHeight("300px");
        listBox.setWidthFull();

        listBox.setRenderer(new ComponentRenderer<>(paramfileName -> {
            String fileName = paramfileName.contains(XML) ? "file-xml-icon.svg" : "xsd.svg";
            SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
            icon.setSize("40px");
            icon.getStyle().set("margin-right", "8px");
            return new Span(icon, new Span(paramfileName));
        }));

        listBox.addValueChangeListener(event -> {
            if (event.isFromClient()) { // Solo si es clic del usuario
                this.currentSelection.clear();
                if (event.getValue() != null) {
                    this.currentSelection.addAll(event.getValue());
                }
                onSelectCallback.accept(this.currentSelection);
            }
        });

        VerticalLayout layout = new VerticalLayout(searchField, new Hr(), listBox);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        super.add(layout);
        searchField.focus();
    }

    private void filterList(String filterText) {
        List<String> itemsToShow;
        if (filterText == null || filterText.isEmpty()) {
            Comparator<String> priorityComparator = Comparator
                    .comparingInt((String fileName) -> currentSelection.contains(fileName) ? 0 : 1)
                    .thenComparing(item -> item.toLowerCase());
            itemsToShow = allXsdXmlFiles.stream()
                    .sorted(priorityComparator)
                    .toList();
        } else {
            itemsToShow = allXsdXmlFiles.stream()
                    .filter(name -> name.toLowerCase().contains(filterText.toLowerCase()))
                    .toList();
        }
        listBox.setItems(itemsToShow);
        Set<String> selectionToRestore = currentSelection.stream()
                .filter(itemsToShow::contains)
                .collect(Collectors.toSet());
        listBox.setValue(selectionToRestore);
    }
}