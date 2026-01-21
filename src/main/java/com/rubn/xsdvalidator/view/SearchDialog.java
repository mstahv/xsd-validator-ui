package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE_ITEMS;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XSD;

/**
 * @author rubn
 */
public class SearchDialog extends Dialog {

    public static final String BADGE_PILL = "badge pill";
    public static final String THEME_INACTIVE = "contrast";
    private final TextField searchField = new TextField();
    private final Div divCenterSpanNotSearch = new Div();
    private final MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
    private final Set<String> currentSelection = new ConcurrentSkipListSet<>();
    private final List<String> allXsdXmlFiles;

    public SearchDialog(List<String> rawFileList, String initialXsdSelection,
                        String initialXmlSelection,
                        Consumer<Set<String>> onSelectCallback) {
        addClassName("search-dialog-content");
        setWidth("500px");

        Span spanNotSearchFound = new Span("Item not found!");
        this.divCenterSpanNotSearch.add(spanNotSearchFound);
        this.divCenterSpanNotSearch.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Height.FULL,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

        searchField.setPlaceholder("Filter [xsd, xml]");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.addClassName(LumoUtility.TextColor.SECONDARY);
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
            Span spanParamFileName = new Span(paramfileName);
            spanParamFileName.addClassName(LumoUtility.TextColor.SECONDARY);
            return new Span(icon, spanParamFileName);
        }));

        listBox.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                this.currentSelection.addAll(event.getValue());
                onSelectCallback.accept(this.currentSelection);
            }
        });

        HorizontalLayout filtersBadges = new HorizontalLayout();
        filtersBadges.getStyle().setPadding("var(--lumo-space-xs)");
        filtersBadges.setSpacing("var(--lumo-space-s)");
        com.vaadin.flow.component.html.Span btnXml = new com.vaadin.flow.component.html.Span(".xml");
        com.vaadin.flow.component.html.Span btnXsd = new com.vaadin.flow.component.html.Span(".xsd");
        configureBadgeButton(btnXml);
        configureBadgeButton(btnXsd);
        ComponentEventListener<ClickEvent<com.vaadin.flow.component.html.Span>> listener = event -> {
            com.vaadin.flow.component.html.Span clicked = event.getSource();
            boolean wasActive = !clicked.getElement().getThemeList().contains(THEME_INACTIVE);
            makeInactive(btnXml);
            makeInactive(btnXsd);
            if (wasActive) {
                filterList("");
            } else {
                clicked.getElement().getThemeList().remove(THEME_INACTIVE);
                String value = !this.searchField.getValue().isEmpty() ? this.searchField.getValue() : clicked.getText();
                filterList(value);
            }
        };

        btnXml.addClickListener(listener);
        btnXsd.addClickListener(listener);
        filtersBadges.add(btnXml, btnXsd);

        final Hr hrLine = new Hr();
        hrLine.addClassName("hr-line");

        VerticalLayout layout = new VerticalLayout(searchField, filtersBadges, hrLine, listBox);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        super.add(layout);
        searchField.focus();

        this.buildSpanCounters().forEach(span -> super.getFooter().add(span));

    }

    private void configureBadgeButton(com.vaadin.flow.component.html.Span span) {
        span.getElement().getThemeList().add(BADGE_PILL + " " + THEME_INACTIVE);
        span.getStyle().setCursor(CURSOR_POINTER);
        span.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
    }

    private void makeInactive(com.vaadin.flow.component.html.Span span) {
        span.getElement().getThemeList().add(THEME_INACTIVE);
    }

    @SuppressWarnings("unused")
//    private RadioButtonGroup<String> buildFilterBadgesRadioButtonGroup() {
//        RadioButtonGroup<String> badgesRadioGroup = new RadioButtonGroup<>();
//        badgesRadioGroup.getStyle().setAlignItems(Style.AlignItems.END);
//        badgesRadioGroup.setItems(List.of(XML, XSD));
//        badgesRadioGroup.addThemeName("badge-pills");
//        badgesRadioGroup.getElement().getChildren().forEach(item -> item.getStyle().setCursor(CURSOR_POINTER));
//        badgesRadioGroup.addValueChangeListener(event -> {
//            String value = this.fieldNotEmptyOrUseItems(event);
//            if (value.isEmpty()) {
//                this.filterList(StringUtils.EMPTY);
//                System.out.println("Filtro removido");
//            } else {
//                this.filterList(value);
//            }
//        });
//        return badgesRadioGroup;
//    }

    private String fieldNotEmptyOrUseItems(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String> event) {
        return !this.searchField.getValue().isEmpty() ? this.searchField.getValue() : event.getValue();
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
        if (itemsToShow.isEmpty()) {
            listBox.add(this.divCenterSpanNotSearch);
        } else {
            listBox.remove(this.divCenterSpanNotSearch);
        }
        Set<String> selectionToRestore = currentSelection.stream()
                .filter(itemsToShow::contains)
                .collect(Collectors.toSet());
        listBox.setValue(selectionToRestore);
    }

    private List<Span> buildSpanCounters() {
        long countXsd = allXsdXmlFiles
                .stream()
                .filter(name -> name.toLowerCase().endsWith(XSD))
                .count();

        long countXml = allXsdXmlFiles
                .stream()
                .filter(name -> name.toLowerCase().endsWith(XML))
                .count();

        final Span totalXmlAndXsds = this.buildSpan("Total: ", allXsdXmlFiles.size());
        final Span xsdSpan = this.buildSpan("xsd: ", countXsd);
        final Span xmlSpan = this.buildSpan("xml: ", countXml);

        return Arrays.asList(totalXmlAndXsds, xsdSpan, xmlSpan);
    }

    private Span buildSpan(String name, long countXsd) {
        Span span = new Span(name + countXsd);
        span.getElement().getThemeList().add(BADGE_PILL + " small");
        span.addClassNames(LumoUtility.TextColor.SECONDARY);
        span.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        return span;
    }

}