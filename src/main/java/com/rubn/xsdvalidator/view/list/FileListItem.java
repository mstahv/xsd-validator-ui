package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.view.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxVariant;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FileListItem extends ListItem {

    private final Checkbox checkbox;
    private final String fileName;

    public FileListItem(String prefixFileName, long contentLength, BiConsumer<FileListItem, Boolean> onSelectionListener) {
        this.checkbox = new Checkbox();
        this.fileName = prefixFileName;

        this.checkbox.addValueChangeListener(event -> {
            if (event.getValue()) {
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
}

