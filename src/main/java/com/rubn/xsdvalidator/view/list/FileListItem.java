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

import java.util.function.Consumer;

public class FileListItem extends ListItem {

    private final Checkbox checkbox;
    private final String fileName;

    public FileListItem(String prefixFileName, long contentLength, Consumer<FileListItem> onSelectionListener) {
        this.checkbox = new Checkbox();
        this.fileName = prefixFileName;

        // 2. Listener: Si este radio se marca, avisamos al padre (onSelectionListener)
        this.checkbox.addValueChangeListener(event -> {
            if (event.getValue()) {
                // Le pasamos 'this' (este item) al padre para que sepa cuál se seleccionó
                onSelectionListener.accept(this);
            } else {
                // Opcional: Evitar que el usuario desmarque haciendo clic en el mismo
                // (Comportamiento nativo de radio button: una vez marcado, no se desmarca solo)
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

    // Método público para que el padre pueda desmarcar este item
    public void setSelected(boolean selected) {
        // Usamos setValue(selected) pero evitamos disparar el listener de nuevo para no crear bucles
        this.checkbox.setValue(selected);
    }

    public String getFileName() {
        return this.fileName;
    }
}

