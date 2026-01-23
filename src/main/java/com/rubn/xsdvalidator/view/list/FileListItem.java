package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.rubn.xsdvalidator.view.CodeEditor;
import com.rubn.xsdvalidator.view.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WINDOW_COPY_TO_CLIPBOARD;

@Log4j2
public class FileListItem extends ListItem {

    private Dialog dialog = new Dialog();
    private final Checkbox checkbox;
    private final String fileName;
    private final Map<String, byte[]> mapPrefixFileNameAndContent;

    public FileListItem(String prefixFileName, long contentLength,
                        BiConsumer<FileListItem, Boolean> onSelectionListener, final Map<String, byte[]> mapPrefixFileNameAndContent) {
        this.checkbox = new Checkbox();
        this.fileName = prefixFileName;
        this.mapPrefixFileNameAndContent = mapPrefixFileNameAndContent;

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
        setPrimary(spanPrefixName);

        checkbox.addThemeVariants(CheckboxVariant.LUMO_HELPER_ABOVE_FIELD);

        String content = "Size ⋅ " + contentLength + "KB";
        setSecondary(new Span(content, FontSize.XXSMALL), checkbox);
        this.column.removeClassName(Padding.Vertical.XSMALL);

        setGap(Layout.Gap.SMALL);

        this.dialog = this.buildDialog();
        super.addDoubleClickListener(event -> dialog.open());

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

    public String getFileName() {
        return this.fileName;
    }

    public void showXmlCode() {
        this.dialog.open();
    }

    public void closeDialog() {
        this.dialog.close();
    }

    public Dialog buildDialog() {
        dialog.setSizeFull();
        dialog.setCloseOnEsc(true);
        dialog.addClassName("xml-visualizer-dialog");
        final Button closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.setTooltipText("Close");
        closeButton.getStyle().setCursor(CURSOR_POINTER);
        closeButton.addClassName(LumoUtility.Margin.Left.AUTO);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(e -> this.closeDialog());
        final Icon iconClose = VaadinIcon.ARROW_LEFT.create();
        Tooltip.forComponent(iconClose).setText("Back");
        iconClose.getStyle().setCursor(CURSOR_POINTER);
        iconClose.addClassName(LumoUtility.TextColor.TERTIARY);
        iconClose.addClickListener(event -> this.closeDialog());
        dialog.getHeader().add(iconClose);
        //Header with title
        final Span spanFileNameTitle = new Span(fileName);
        spanFileNameTitle.addClassNames(FontSize.LARGE, LumoUtility.FontWeight.BOLD);
        final SvgIcon copyButtonIcon = SvgFactory.createCopyButtonFromSvg();
        final Button copyButton = new Button(copyButtonIcon);
        copyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        copyButton.addClickListener(event -> {
            UI.getCurrent().getElement().executeJs(WINDOW_COPY_TO_CLIPBOARD, fileName);
            Notification.show("Copied " + fileName, 2500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            copyButton.setIcon(VaadinIcon.CHECK.create());
            Mono.just(copyButton)
                    .delayElement(Duration.ofMillis(1500))
                    .subscribe(btn -> {
                        btn.getUI().ifPresent(ui -> ui.access(() -> {
                            btn.setIcon(copyButtonIcon);
                        }));
                    });
        });
        dialog.addClosedListener(event -> copyButton.setIcon(copyButtonIcon));
        copyButton.setTooltipText("Copy filename!");
        dialog.getHeader().add(spanFileNameTitle, copyButton, closeButton);

        final CodeEditor codeEditor = new CodeEditor();
        codeEditor.setShowLineNumbers(true);
        codeEditor.setContent(new String(this.mapPrefixFileNameAndContent.get(fileName)));
        codeEditor.addValueChangeListener(event -> {
            //log.info("Event -> {}", event);
        });
        dialog.add(codeEditor);
//        dialog.add(this.buildSyntaxHighlighter());
        //Footer with update code ?

        final Button button = new Button("Update", (event) -> {
            Notification.show("Not yet implemented!!!").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            //log.info("Content: {}", codeEditor.getContent());
        });
        button.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);

        dialog.getFooter().add(button);
        return dialog;
    }

}

