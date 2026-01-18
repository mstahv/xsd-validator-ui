package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.records.CheckBoxEventRecord;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.context.event.EventListener;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE_ITEMS;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;

/**
 * @author rubn
 */
public class SearchDialog extends Dialog {

    private final Div divCenterSpanNotSearch = new Div();
    private final MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
    private final Set<String> currentSelection = new ConcurrentSkipListSet<>();
    private final Span spanNotSearchFound = new Span("Item not found!");
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

        this.divCenterSpanNotSearch.add(spanNotSearchFound);
        this.divCenterSpanNotSearch.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Height.FULL,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

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
        listBox.getChildren().forEach(item -> item.getStyle().setCursor(CURSOR_POINTER));
        listBox.setRenderer(new ComponentRenderer<>(paramfileName -> {
            String fileName = paramfileName.contains(XML) ? "file-xml-icon.svg" : "xsd.svg";
            SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
            icon.setSize("40px");
            icon.getStyle().set("margin-right", "8px");
            return new Span(icon, new Span(paramfileName));
        }));

        listBox.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                this.currentSelection.addAll(event.getValue());
                onSelectCallback.accept(this.currentSelection);
            }
        });

        final HorizontalLayout badgeFilterRow = this.buildBadgeFilterRow();

        VerticalLayout layout = new VerticalLayout(searchField, badgeFilterRow, new Hr(), listBox);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        super.add(layout);
        searchField.focus();
    }

    private RadioButtonGroup<String> buildFilterBadgesRadioButtonGroup() {
        RadioButtonGroup<String> badgesRadioGroup = new RadioButtonGroup<>();
        badgesRadioGroup.setWidthFull();
        badgesRadioGroup.getStyle().setAlignItems(Style.AlignItems.END);
        badgesRadioGroup.setItems(List.of(".xml"));
//        badgesRadioGroup.setItemLabelGenerator(RadioButtomGridLoginSchedulerEnum::setItemLabelGenerator);
        applyRadioButtomTheme(badgesRadioGroup);
        return badgesRadioGroup;
    }

    private void applyRadioButtomTheme(RadioButtonGroup<String> radioButtonGroup) {
        radioButtonGroup.getChildren().forEach(component -> {
            if (component.getElement().getChild(0).toString().contains("xml")) {
                component.getElement().getThemeList().add("toggle badge pill");
                Tooltip.forComponent(component).setText("Refresh table");
            }
            component.addClassName(LumoUtility.Margin.Right.MEDIUM);
            component.getElement().getStyle().setCursor(CURSOR_POINTER);
        });
    }

    private HorizontalLayout buildBadgeFilterRow() {
        Span spanXmlBadge = new Span("xml");
        spanXmlBadge.getElement().getThemeList().add("badge pill contrast");
        spanXmlBadge.addClickListener(event -> this.filterList(XML));

        final HorizontalLayout headerRow = new HorizontalLayout(spanXmlBadge);
        headerRow.setPadding(false);
        headerRow.setWidthFull();
//        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        return headerRow;
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
        if(itemsToShow.isEmpty()) {
            listBox.add(this.divCenterSpanNotSearch);
        } else {
            listBox.remove(this.divCenterSpanNotSearch);
        }
        Set<String> selectionToRestore = currentSelection.stream()
                .filter(itemsToShow::contains)
                .collect(Collectors.toSet());
        listBox.setValue(selectionToRestore);
    }

    //esta clase debe ser componente de Spring para que esto se dispare correctamente
    @EventListener
    public void listener(CheckBoxEventRecord checkBoxEventRecord) {
        if(checkBoxEventRecord.fileName().equals("TExt")) {

        }
        if(checkBoxEventRecord.fileName().equals("TExt")) {

        }
    }

}