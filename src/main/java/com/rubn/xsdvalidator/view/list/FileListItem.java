package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.util.ConfirmDialogBuilder;
import com.rubn.xsdvalidator.util.FileUtils;
import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.rubn.xsdvalidator.view.SimpleCodeEditor;
import com.rubn.xsdvalidator.view.Span;
import com.rubn.xsdvalidator.view.Uploader;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CONTEXT_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.DELETE_ITEM;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.LIGHT;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.VS_DARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WINDOW_COPY_TO_CLIPBOARD;

@Log4j2
public class FileListItem extends ListItem {

    private Dialog dialog = new Dialog();
    private final ProgressBar progressBar = new ProgressBar();
    private final SimpleCodeEditor simpleCodeEditor = new SimpleCodeEditor();
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

        String content = "Size ⋅ " + FileUtils.formatSize(contentLength);
        setSecondary(new Span(content, FontSize.XXSMALL), checkbox);
        this.column.removeClassName(Padding.Vertical.XSMALL);

        setGap(Layout.Gap.SMALL);

        this.dialog = this.buildDialog();
        super.addDoubleClickListener(event -> this.showXmlCode());

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
        this.simpleCodeEditor.setContent(new String(this.mapPrefixFileNameAndContent.get(fileName)));
        this.progressBar.setVisible(false);
    }

    public void closeDialog() {
        this.dialog.close();
    }

    public Dialog buildDialog() {
        dialog.setSizeFull();
        dialog.setCloseOnEsc(true);
        dialog.addClassName("xml-visualizer-dialog");
        final Button closeButton = this.buildCloseButton();
        final Icon iconBackLeft = this.buildBackIconLeft();
        dialog.getHeader().add(iconBackLeft);
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
        this.progressBar.setWidth("10%");
        this.progressBar.setVisible(true);
        this.progressBar.setIndeterminate(true);

        Icon iconTheme = this.buildIconTheme();
        SvgIcon iconWordWrap = this.buildIconWordWrap();
        dialog.getHeader().add(spanFileNameTitle, copyButton, iconTheme, iconWordWrap, this.progressBar, closeButton);

        simpleCodeEditor.addValueChangeListener(event -> {
            //this.progressBar.setVisible(false);
        });
        dialog.add(simpleCodeEditor);
        //Footer with update code ?
        final Button button = new Button("Save", (event) -> {
            Notification.show("Not yet implemented!!!").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            //log.info("Content: {}", codeEditor.getContent());
        });
        button.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);

        dialog.getFooter().add(button);
        return dialog;
    }

    private @NonNull Icon buildBackIconLeft() {
        final Icon iconClose = VaadinIcon.ARROW_LEFT.create();
        Tooltip.forComponent(iconClose).setText("Back");
        iconClose.getStyle().setCursor(CURSOR_POINTER);
        iconClose.addClassName(LumoUtility.TextColor.TERTIARY);
        iconClose.addClickListener(event -> this.closeDialog());
        return iconClose;
    }

    private @NonNull Button buildCloseButton() {
        final Button closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.setTooltipText("Close");
        closeButton.getStyle().setCursor(CURSOR_POINTER);
        closeButton.addClassName(LumoUtility.Margin.Left.AUTO);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(e -> this.closeDialog());
        return closeButton;
    }

    private Icon buildIconTheme() {
        final Icon iconTheme = VaadinIcon.ADJUST.create();
        Tooltip.forComponent(iconTheme)
                .withPosition(Tooltip.TooltipPosition.BOTTOM_END)
                .withText("dark - ligth");
        iconTheme.getStyle().setCursor(CURSOR_POINTER);
        iconTheme.addClassName(FontSize.SMALL);
        iconTheme.addClickListener(event -> {
            String theme = simpleCodeEditor.getTheme().equals(VS_DARK) ? LIGHT : VS_DARK;
            simpleCodeEditor.setTheme(theme);
        });
        return iconTheme;
    }

    private SvgIcon buildIconWordWrap() {
        final SvgIcon iconTheme = SvgFactory.createIconFromSvg("word-wrap.svg", "25px", null);
        Tooltip.forComponent(iconTheme)
                .withPosition(Tooltip.TooltipPosition.BOTTOM_END)
                .withText("word wrap");
        iconTheme.getStyle().setCursor(CURSOR_POINTER);
        iconTheme.addClickListener(event -> simpleCodeEditor.setWordWrap(!simpleCodeEditor.getWordWrap()));
        return iconTheme;
    }

    public MenuItem buildContextMenuItem(FileListItem fileListItem) {
        ContextMenu contextMenu = this.buildContextMenu(fileListItem);
        contextMenu.addItem(this.createRowItemWithIcon("Edit", VaadinIcon.PENCIL.create(), "15px"),
                event -> event.getSource().getUI().ifPresent(ui -> fileListItem.showXmlCode())
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

}

