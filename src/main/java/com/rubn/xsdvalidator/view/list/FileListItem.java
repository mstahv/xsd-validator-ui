package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorFileUtils;
import com.rubn.xsdvalidator.view.SearchPopover;
import com.rubn.xsdvalidator.view.SimpleCodeEditor;
import com.rubn.xsdvalidator.view.SimpleCodeEditorDialog;
import com.rubn.xsdvalidator.view.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.function.BiConsumer;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CONTEXT_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.DELETE_ITEM;

@Log4j2
public class FileListItem extends ListItem {

    public static final String SIZE = "Size ⋅ ";
    private final SimpleCodeEditorDialog simpleCodeEditorDialog;
    @Getter
    private final Button buttonClose = new Button(LumoIcon.CROSS.create());
    @Getter
    private final SimpleCodeEditor simpleCodeEditor;
    private final Checkbox checkbox;

    @Getter
    private final String fileName;

    public FileListItem(String prefixFileName, long contentLength,
                        BiConsumer<FileListItem, Boolean> onSelectionListener,
                        final Map<String, byte[]> mapPrefixFileNameAndContent,
                        SearchPopover searchPopover) {
        this.checkbox = new Checkbox();
        this.fileName = prefixFileName;

        this.checkbox.addValueChangeListener(event -> {
            //Important! do not use event.isFromClient() in this condition
            if (event.getValue() != null) {
                onSelectionListener.accept(this, event.getValue());
            }
        });

        addClassName("file-list-item");
        addClassNames(Background.CONTRAST_5, BorderRadius.LARGE, Padding.Vertical.SMALL, Padding.Horizontal.SMALL);

        super.setPrefix(this.createFileIcon(prefixFileName.substring(prefixFileName.lastIndexOf('.') + 1)));

        final Span spanPrefixName = new Span(prefixFileName, FontSize.XSMALL);
        Tooltip tooltip = Tooltip.forComponent(spanPrefixName);
        tooltip.setText(prefixFileName);
        tooltip.setPosition(Tooltip.TooltipPosition.TOP);

        buttonClose.addClassName("button-close");
        buttonClose.setHeight("18px");
        buttonClose.getStyle().setMarginRight("var(--lumo-space-xs)");
        buttonClose.getStyle().setCursor(CURSOR_POINTER);
        buttonClose.getIcon().getStyle().setWidth("18px");
        buttonClose.getIcon().getStyle().setHeight("18px");
        buttonClose.getStyle().setBorderRadius(BorderRadius.LARGE);
        buttonClose.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
        Tooltip.forComponent(buttonClose)
                .withText("Close")
                .withPosition(Tooltip.TooltipPosition.TOP);

        super.setPrimary(spanPrefixName, this.buttonClose);

        checkbox.addThemeVariants(CheckboxVariant.LUMO_HELPER_ABOVE_FIELD);

        String content = SIZE + XsdValidatorFileUtils.formatSize(contentLength);
        Span sizeSpan = new Span();
        sizeSpan.setText(content);
        sizeSpan.addClassName(FontSize.XXSMALL);
        sizeSpan.setWidthFull();

        super.setSecondary(sizeSpan, this.checkbox);
        this.column.removeClassName(Padding.Vertical.XSMALL);

        setGap(Layout.Gap.SMALL);

        this.simpleCodeEditorDialog = new SimpleCodeEditorDialog(fileName, mapPrefixFileNameAndContent, searchPopover,
                sizeSpan);
        this.simpleCodeEditor = simpleCodeEditorDialog.getSimpleCodeEditor();

        super.addDoubleClickListener(eventClick -> {
            if (eventClick.isFromClient()) {
                this.checkbox.setValue(!this.checkbox.getValue());
            }
        });

    }

    private Component createFileIcon(String paramfileName) {

        Layout corner = new Layout();
        corner.setSizeFull();

        String fileName = paramfileName.contains("xml")
                ? "file-xml-icon.svg"
                : "xsd.svg";

        SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
        icon.setSize("40px");
        corner.add(icon);

        Layout fileIcon = new Layout(corner);
        fileIcon.setPosition(Layout.Position.RELATIVE);
        fileIcon.setWidth(2.3f, Unit.REM);

        return fileIcon;
    }

    public void setSelected(boolean selected) {
        this.checkbox.setValue(selected);
    }

    public boolean isChecked() {
        return this.checkbox.getValue();
    }


    public MenuItem buildContextMenuItem(FileListItem fileListItem) {
        ContextMenu contextMenu = this.buildContextMenu(fileListItem);
        contextMenu.addItem(this.createRowItemWithIcon("Edit", VaadinIcon.PENCIL.create(), "15px"),
                event -> event.getSource().getUI().ifPresent(ui -> simpleCodeEditorDialog.showXmlCode())
        ).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);
        contextMenu.addSeparator();
        contextMenu.addItem(this.createRowItemWithIcon("Delete", VaadinIcon.TRASH.create(), "15px")
        ).addClassNames(CONTEXT_MENU_ITEM_NO_CHECKMARK, DELETE_ITEM);
        return contextMenu.getItems().get(1);
    }

    private ContextMenu buildContextMenu(Component target) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);
        return contextMenu;
    }

    private HorizontalLayout createRowItemWithIcon(final String titleForSpan, AbstractIcon<?> icon, String iconSizeInPx) {
        final HorizontalLayout row = new HorizontalLayout();
        row.setId("row-with-icon");
        row.setSpacing(false);
        row.addClassNames(LumoUtility.Gap.SMALL);
        final com.vaadin.flow.component.html.Span span = new com.vaadin.flow.component.html.Span(titleForSpan);
        icon.setSize(iconSizeInPx);
        row.add(icon, span);
        row.addClassName(LumoUtility.FontSize.SMALL);
        return row;
    }

    /**
     * View the error directly in the editor
     *
     * @param line with error
     * @param xmlFileName with error
     */
    public void searchForTextLineInTheEditor(String line, String xmlFileName) {
        this.simpleCodeEditorDialog.searchLineOnEditor(line, xmlFileName);
    }

}

