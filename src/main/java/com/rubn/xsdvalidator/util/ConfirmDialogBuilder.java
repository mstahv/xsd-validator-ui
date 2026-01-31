package com.rubn.xsdvalidator.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.INFORMATION;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.OK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WARNING;

/**
 * ConfirmDialogBuilder
 *
 * @author rubn
 */
@Log4j2
@Value
public class ConfirmDialogBuilder {

    public static void showWarning(String text) {
        ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .build();
    }

    public static void showWarningUI(String text, final UI ui) {
        final var confirmDialog = ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .build();
        ui.addToModalComponent(confirmDialog);
    }

    public static void showInformationUI(String text, final UI ui) {
        final var confirmDialog = ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.INFO_CIRCLE_O, INFORMATION)
                .withText(text)
                .build();
        ui.addToModalComponent(confirmDialog);
    }

    public static void showInformation(final String text) {
        ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.INFO_CIRCLE_O, INFORMATION)
                .withText(text)
                .build();
    }

    public static ConfirmDialog showConfirmInformation(final String text, UI ui) {
        var confirmDialog = ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .build();
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        ui.addToModalComponent(confirmDialog);
        return confirmDialog;
    }

    private static WithIconAndHeaderText builder() {
        return new InnerBuilder();
    }

    /**
     * 1
     */
    public interface WithIconAndHeaderText {
        Text withHeaderIconAndHeaderText(final VaadinIcon icon, final String textHeader);
    }

    /**
     * 2
     */
    public interface Text {
        Build withText(final String text);
    }

    /**
     * 3
     */
    public interface Build extends IBuilder<ConfirmDialog> {

    }

    public interface IBuilder<T> {
        @NonNull T build();
    }

    /**
     * The magic builder
     */
    @Getter
    public static class InnerBuilder implements Text, WithIconAndHeaderText, Build {

        private VaadinIcon icon;
        private String text;
        private String headerText;
        private final ConfirmDialog confirmDialog = new ConfirmDialog();

        @Override
        public Build withText(String text) {
            Objects.requireNonNull(text, "Text must not be null");
            this.text = text;
            return this;
        }

        @Override
        public Text withHeaderIconAndHeaderText(VaadinIcon icon, String headerText) {
            Objects.requireNonNull(icon, "Icon must not be null");
            Objects.requireNonNull(headerText, "Header text must not be null");
            this.icon = icon;
            this.headerText = headerText;
            return this;
        }

        @Override
        @NonNull
        public ConfirmDialog build() {
            Icon createdIcon = icon.create();
            Span spanIcon = new Span(createdIcon);
            spanIcon.getElement().setAttribute("aria-hidden", true);

            Layout iconLayout = buildIconLayout(spanIcon);
            Layout textLayout = buildTextLayout();

            // Main layout
            Layout layout = new Layout(iconLayout, textLayout);
            layout.setAlignItems(Layout.AlignItems.START);
            layout.setFlexDirection(Layout.FlexDirection.ROW);
            layout.setGap(Layout.Gap.MEDIUM);

            confirmDialog.setText(layout);
            confirmDialog.setConfirmText(OK);
            confirmDialog.open();

            return confirmDialog;
        }

        private @NonNull Layout buildIconLayout(Span spanIcon) {
            Layout iconLayout = new Layout(spanIcon);
            if (icon == VaadinIcon.WARNING) {
                confirmDialog.setConfirmButtonTheme("error primary");
                spanIcon.addClassNames(LumoUtility.TextColor.ERROR);
                iconLayout.addClassName(LumoUtility.Background.ERROR_10);
            } else {
                spanIcon.addClassNames(LumoUtility.TextColor.PRIMARY);
                iconLayout.addClassName(LumoUtility.Background.PRIMARY_10);
            }

            iconLayout.setId("icon-layout");
            iconLayout.addClassNames(LumoUtility.BorderRadius.FULL, LumoUtility.Flex.SHRINK_NONE,
                    LumoUtility.Width.LARGE,  LumoUtility.Height.LARGE);
            iconLayout.setAlignItems(Layout.AlignItems.CENTER);
            iconLayout.setJustifyContent(Layout.JustifyContent.CENTER);
            return iconLayout;
        }

        private @NonNull Layout buildTextLayout() {
            Span spanText = new Span(text);
            spanText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
            final H3 h3HeaderText = new H3(headerText);
            h3HeaderText.addClassName(LumoUtility.FontSize.MEDIUM);

            Layout textLayout = new Layout(h3HeaderText, spanText);
            textLayout.setFlexDirection(Layout.FlexDirection.COLUMN);
            textLayout.setGap(Layout.Gap.SMALL);

            textLayout.removeClassNames(LumoUtility.TextAlignment.CENTER);
            return textLayout;
        }

    }
}

