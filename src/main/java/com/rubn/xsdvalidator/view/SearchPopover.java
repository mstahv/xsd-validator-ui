package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.util.XsdValidatorFileUtils;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.BADGE_PILL_SMALL;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE_ITEMS;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML_ICON;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XSD;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XSD_ICON;

/**
 * @author rubn
 */
@Log4j2
public class SearchPopover extends Popover {

    public static final String BADGE_PILL = "badge pill";
    public static final String THEME_INACTIVE = "contrast";
    public static final String HEIGHT = "300px";

    private final Div divCenterSpanNotSearch = new Div();
    private final MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
    private final Set<String> currentSelection = new ConcurrentSkipListSet<>();
    private final TextField searchField;

    private final Span totalSpan = new Span();
    private final Span xsdSpan = new Span();
    private final Span xmlSpan = new Span();

    private List<String> allXsdXmlFiles;
    private List<String> currentVisibleItems;

    public SearchPopover(TextField searchField, List<String> rawFileList, String initialXsdSelection,
                         String initialXmlSelection,
                         Consumer<Set<String>> onSelectCallback,
                         final Map<String, byte[]> mapPrefixFileNameAndContent) {

        this.searchField = searchField;

        super.addClassName("search-popover-content");
        super.setWidth("500px");
        super.setModal(false);
        super.setBackdropVisible(true);
        this.setCloseOnOutsideClick(true);
        super.setPosition(PopoverPosition.BOTTOM_END);
        super.setTarget(searchField);
//        this.searchField.getElement().executeJs(
//                "this.addEventListener('click', function(e) { e.stopPropagation(); });"
//        );
        // Center div with not found item
        Span spanNotSearchFound = new Span("Item not found!");
        spanNotSearchFound.addClassName(LumoUtility.TextColor.SECONDARY);
        this.divCenterSpanNotSearch.add(spanNotSearchFound);
        this.divCenterSpanNotSearch.setHeight(HEIGHT);
        this.divCenterSpanNotSearch.addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.Width.FULL,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

        this.searchField.addValueChangeListener(e -> {
//            if (!this.isOpened()) {
//                this.setOpened(true);
//            }
            this.filterList(e.getValue());
        });

        if (initialXsdSelection != null && rawFileList.contains(initialXsdSelection)) {
            currentSelection.add(initialXsdSelection);
        }
        if (initialXmlSelection != null && rawFileList.contains(initialXmlSelection)) {
            currentSelection.add(initialXmlSelection);
        }

        this.allXsdXmlFiles = new ArrayList<>(rawFileList);
        this.currentVisibleItems = new ArrayList<>(rawFileList);

        // Ordenar inicialmente
        sortAndSetItems(this.allXsdXmlFiles);

        listBox.setVisible(false);
        listBox.setValue(currentSelection);
        listBox.setSelectionPreservationMode(SelectionPreservationMode.PRESERVE_ALL);
        listBox.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE_ITEMS);
        listBox.setHeight(HEIGHT);
        listBox.setWidthFull();

        // --- 5. Renderer Optimizado ---
        listBox.setRenderer(new ComponentRenderer<>(paramfileName -> {
            String fileName = paramfileName.contains(XML) ? XML_ICON : XSD_ICON;
            SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
            icon.setSize("40px");

            // sizeInBytes to length String
            byte[] bytes = mapPrefixFileNameAndContent.get(paramfileName);
            long sizeInBytes = bytes != null ? bytes.length : 0;
            String formattedSize = XsdValidatorFileUtils.formatSize(sizeInBytes);

            Span spanParamFileName = new Span(paramfileName);
            spanParamFileName.addClassName(LumoUtility.TextColor.SECONDARY);
            spanParamFileName.getStyle().setCursor(CURSOR_POINTER);

            Span spanSize = new Span(formattedSize);
            spanSize.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XXSMALL);
            spanSize.getElement().getThemeList().add(BADGE_PILL_SMALL);

            final VerticalLayout verticalLayout = new VerticalLayout(spanParamFileName, spanSize);
            verticalLayout.setPadding(false);
            verticalLayout.setSpacing(false);

            HorizontalLayout row = new HorizontalLayout(icon, verticalLayout);
            row.setDefaultVerticalComponentAlignment(HorizontalLayout.Alignment.CENTER);
            return row;
        }));

        listBox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                Set<String> visibleSelectedInComponent = event.getValue();
                String newlySelected = visibleSelectedInComponent.stream()
                        .filter(item -> !this.currentSelection.contains(item))
                        .findFirst()
                        .orElse(null);
                if (newlySelected != null) { //Caso A
                    boolean isNewItemXml = newlySelected.toLowerCase().endsWith(XML);
                    this.currentSelection.removeIf(existingItem -> {
                        boolean existingIsXml = existingItem.toLowerCase().endsWith(XML);
                        return (existingIsXml == isNewItemXml) && !existingItem.equals(newlySelected);
                    });
                    this.currentSelection.add(newlySelected);
                } else {
                    // CASO B:  desmarcar
                    List<String> uncheckedItems = this.currentVisibleItems.stream()
                            .filter(item -> !visibleSelectedInComponent.contains(item))
                            .toList();
                    uncheckedItems.forEach(this.currentSelection::remove);
                }
                Set<String> visualUpdate = this.currentSelection.stream()
                        .filter(this.currentVisibleItems::contains)
                        .collect(Collectors.toSet());
                listBox.setValue(visualUpdate);
                onSelectCallback.accept(this.currentSelection);
            }
        });

        HorizontalLayout filtersBadges = new HorizontalLayout();
        filtersBadges.getStyle().setPadding("var(--lumo-space-xs)");
        filtersBadges.setSpacing("var(--lumo-space-s)");
        com.vaadin.flow.component.html.Span btnXml = new com.vaadin.flow.component.html.Span(XML);
        com.vaadin.flow.component.html.Span btnXsd = new com.vaadin.flow.component.html.Span(XSD);
        configureBadgeButton(btnXml);
        configureBadgeButton(btnXsd);

        ComponentEventListener<ClickEvent<com.vaadin.flow.component.html.Span>> listener = event -> {
            com.vaadin.flow.component.html.Span clicked = event.getSource();
            boolean wasActive = !clicked.getElement().getThemeList().contains(THEME_INACTIVE);
            makeInactive(btnXml);
            makeInactive(btnXsd);
            if (wasActive) {
                filterList(this.searchField.getValue());
            } else {
                clicked.getElement().getThemeList().remove(THEME_INACTIVE);
                String value = !this.searchField.getValue().isEmpty() ? this.searchField.getValue() : clicked.getText();
                filterList(value);
            }
        };

        btnXml.addClickListener(listener);
        btnXsd.addClickListener(listener);
        filtersBadges.add(btnXml, btnXsd);

        final Hr hrLine = buildHrSeparator();

        final HorizontalLayout rowFooter = new HorizontalLayout();
        rowFooter.getStyle().setPadding("var(--lumo-space-xs)");
        rowFooter.setSpacing("var(--lumo-space-s)");

        this.updateCounters();
        rowFooter.add(totalSpan, xsdSpan, xmlSpan);

        final Hr hrLineFooter = buildHrSeparator();

        VerticalLayout layout = new VerticalLayout(filtersBadges, hrLine, listBox, divCenterSpanNotSearch, hrLineFooter, rowFooter);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        super.add(layout);
    }

    public void updateSelectionFromOutside(String xsdSelection, String xmlSelection) {
        this.currentSelection.clear();
        if (xsdSelection != null && !xsdSelection.isEmpty()) {
            this.currentSelection.add(xsdSelection);
        }
        if (xmlSelection != null && !xmlSelection.isEmpty()) {
            this.currentSelection.add(xmlSelection);
        }
        if (this.currentVisibleItems != null) {
            Set<String> visualUpdate = this.currentSelection.stream()
                    .filter(this.currentVisibleItems::contains)
                    .collect(Collectors.toSet());
            this.listBox.setValue(visualUpdate);
        } else {
            this.listBox.setValue(this.currentSelection);
        }
    }

    /**
     * Sirve para actualizar los span del footer, cuando se borran o editan los {@link com.rubn.xsdvalidator.view.list.FileListItem}
     *
     * @param newItems
     */
    public void updateItems(List<String> newItems) {
        this.allXsdXmlFiles = List.copyOf(newItems);
        this.filterList(searchField.getValue());
        this.updateCounters();
    }

    @Override
    public void open() {
        super.open();
    }

    private void sortAndSetItems(List<String> items) {
        Comparator<String> priorityComparator = Comparator
                .comparingInt((String fileName) -> currentSelection.contains(fileName) ? 0 : 1)
                .thenComparing(item -> item.toLowerCase());

        List<String> sorted = items.stream()
                .sorted(priorityComparator)
                .toList();

        this.currentVisibleItems = sorted;
        listBox.setItems(sorted);

        Set<String> toSelect = currentSelection.stream()
                .filter(sorted::contains)
                .collect(Collectors.toSet());
        listBox.setValue(toSelect);
    }

    private void filterList(String filterText) {
        List<String> itemsToShow;
        if (filterText == null || filterText.isEmpty()) {
            itemsToShow = new ArrayList<>(allXsdXmlFiles);
        } else {
            itemsToShow = allXsdXmlFiles.stream()
                    .filter(name -> name.toLowerCase().contains(filterText.toLowerCase()))
                    .toList();
        }

        if (itemsToShow.isEmpty()) {
            listBox.setVisible(false);
            divCenterSpanNotSearch.setVisible(true);
            this.currentVisibleItems = new ArrayList<>();
        } else {
            listBox.setVisible(true);
            divCenterSpanNotSearch.setVisible(false);
            sortAndSetItems(itemsToShow);
        }
    }

    private void updateCounters() {
        long countXsd = allXsdXmlFiles.stream().filter(name -> name.toLowerCase().endsWith(XSD)).count();
        long countXml = allXsdXmlFiles.stream().filter(name -> name.toLowerCase().endsWith(XML)).count();

        configureSpan(totalSpan, "Total: " + allXsdXmlFiles.size());
        configureSpan(xsdSpan, "xsd: " + countXsd);
        configureSpan(xmlSpan, "xml: " + countXml);
    }

    private void configureSpan(Span span, String text) {
        span.setText(text);
        span.getElement().getThemeList().clear(); // Limpiar para evitar duplicados
        span.getElement().getThemeList().add(BADGE_PILL + " small");
        span.addClassNames(LumoUtility.TextColor.SECONDARY);
        span.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
    }

    private @NonNull Hr buildHrSeparator() {
        final Hr hrLine = new Hr();
        hrLine.addClassName("hr-line");
        return hrLine;
    }

    private void configureBadgeButton(com.vaadin.flow.component.html.Span span) {
        span.getElement().getThemeList().add(BADGE_PILL + " " + THEME_INACTIVE);
        span.getStyle().setCursor(CURSOR_POINTER);
        span.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
    }

    private void makeInactive(com.vaadin.flow.component.html.Span span) {
        span.getElement().getThemeList().add(THEME_INACTIVE);
    }

}