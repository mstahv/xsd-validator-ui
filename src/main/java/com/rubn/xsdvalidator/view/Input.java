package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.records.DecompressedFile;
import com.rubn.xsdvalidator.service.DecompressionService;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.util.ConfirmDialogBuilder;
import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.view.list.CustomList;
import com.rubn.xsdvalidator.view.list.FileListItem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.FastByteArrayOutputStream;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CONTEXT_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.DELETE_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.JS_COMMAND;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WINDOW_COPY_TO_CLIPBOARD;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XSD;

/**
 * @author rubn
 */
@Slf4j
public class Input extends Layout implements BeforeEnterObserver {

    private final Map<String, byte[]> mapPrefixFileNameAndContent = new ConcurrentHashMap<>();
    private final AtomicInteger counterSpanId = new AtomicInteger(0);
    private final List<String> allErrorsList = new CopyOnWriteArrayList<>();
    private final VerticalLayout verticalLayoutArea;
    private final Button attachment;
    private final Button validateButton;
    private final CustomList customList;
    private final Uploader uploader;
    private final Anchor anchorDownloadErrors;
    /**
     * Service
     */
    private final ValidationXsdSchemaService validationXsdSchemaService;
    private final DecompressionService decompressionService;
    /**
     * Mutable fields
     */
    private Span spanWordError;
    private String selectedXmlFile;
    private String selectedMainXsd;

    public Input(final ValidationXsdSchemaService validationXsdSchemaService, final DecompressionService decompressionService) {
        this.validationXsdSchemaService = validationXsdSchemaService;
        this.decompressionService = decompressionService;
        addClassName("input-layout");
        // Text area
        verticalLayoutArea = new VerticalLayout();
        verticalLayoutArea.addClassNames("vertical-area");
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        final ContextMenu contextMenu = this.buildContextMenu(verticalLayoutArea);
        contextMenu.addItem(this.createRowItemWithIcon("Clear errors!", VaadinIcon.TRASH.create(), "15px"), event -> {
            verticalLayoutArea.removeAll();
            this.counterSpanId.set(0);
            verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        }).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);

        // attachment upload button
        attachment = new Button(VaadinIcon.UPLOAD.create());
        attachment.addClassName("attachment");
        attachment.addThemeVariants(ButtonVariant.LUMO_SMALL);
        attachment.setAriaLabel("Attachment");
        attachment.setTooltipText("Attachment");

        this.customList = new CustomList();
        this.anchorDownloadErrors = new Anchor();
        this.anchorDownloadErrors.setEnabled(false);
        MenuBar menuBar = this.buildMenuBarOptions();
        final Div divHeader = new Div(customList, menuBar);
        divHeader.addClassName("div-files-wrapper");

        this.uploader = new Uploader(attachment);
        this.uploader.setUploadHandler(this.buildUploadHandler());

        validateButton = new Button("Validate", VaadinIcon.CHECK.create());
        validateButton.addClassName("validate-button");
        validateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        validateButton.setAriaLabel("Validate");
        validateButton.setTooltipText("validate");
        validateButton.setDisableOnClick(true);
        validateButton.addClickListener(event -> {
            if (Objects.isNull(this.selectedXmlFile) || this.selectedXmlFile.isBlank()) {
                showMessageFailedToStartValidation();
                return;
            }
            if (Objects.isNull(this.selectedMainXsd) || this.selectedMainXsd.isBlank()) {
                showMessageFailedToStartValidation();
                return;
            }
            this.validateXmlInputWithXsdSchema();
        });

        Layout actions = new Layout(this.uploader, validateButton);
        actions.addClassName("actions");

        add(divHeader, verticalLayoutArea, actions);
    }

    private void showMessageFailedToStartValidation() {
        ConfirmDialogBuilder.showWarning("Failed to start validation, check uploaded files...");
        validateButton.setEnabled(true);
    }

    private MenuBar buildMenuBarOptions() {
        final Button buttonClearAll = new Button(VaadinIcon.ELLIPSIS_V.create());
        buttonClearAll.getStyle().setCursor(CURSOR_POINTER);
        buttonClearAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        final MenuBar menuBarGridOptions = new MenuBar();
        menuBarGridOptions.addClassName("fixed-menu");
        var tooltip = Tooltip.forComponent(buttonClearAll);
        tooltip.setPosition(Tooltip.TooltipPosition.START_TOP);
        tooltip.setText("Options");
        menuBarGridOptions.setThemeName("tertiary-inline contrast");
        menuBarGridOptions.addThemeVariants(MenuBarVariant.LUMO_SMALL);

        final MenuItem itemEllipsis = menuBarGridOptions.addItem("");
        itemEllipsis.add(buttonClearAll);

        this.anchorDownloadErrors.setClassName("anchor-downloader");
        this.anchorDownloadErrors.setHref(DownloadHandler.fromInputStream((event) -> {
            try {
                String errors = this.textProcessing();
//                log.info("Errors menu item: {}", errors);
                byte[] byteArray = errors.getBytes(StandardCharsets.UTF_8);
                String fileNameError = "validation-errors-" + System.currentTimeMillis() + ".txt";
                event.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + fileNameError + "\"");
                return new DownloadResponse(
                        new ByteArrayInputStream(byteArray),
                        fileNameError,
                        MediaType.TEXT_PLAIN_VALUE,
                        byteArray.length
                );
            } catch (Exception e) {
                log.error("Error downloading errors: ", e);
                ConfirmDialogBuilder.showWarning("Error downloading logs");
                return DownloadResponse.error(500);
            }
        }));

        final HorizontalLayout rowDownload = this.createRowItemWithIcon("Download errors", VaadinIcon.DOWNLOAD.create(), "18px");
        this.anchorDownloadErrors.addComponentAsFirst(rowDownload);
        itemEllipsis.getSubMenu().addItem(this.anchorDownloadErrors).addClassNames(MENU_ITEM_NO_CHECKMARK);

        SvgIcon svgIcon = SvgFactory.createCopyButtonFromSvg();
        svgIcon.getStyle().setMarginLeft("-5px");
        svgIcon.getStyle().setMarginBottom("-6px");
        var row = this.createRowItemWithIcon("Copy all text", svgIcon, "25px");
        row.getStyle().setGap("0.2em");
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        MenuItem itemDelete = itemEllipsis.getSubMenu().addItem(row, event -> {
            if (!this.allErrorsList.isEmpty()) {
                String errors = this.textProcessing();
                UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, errors);
                Notification.show("Copied!", 2000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            }
        });
        itemDelete.addClassNames(MENU_ITEM_NO_CHECKMARK);
        itemDelete.getStyle().setPaddingBottom("10px");

        itemEllipsis.getSubMenu().addSeparator();

        itemEllipsis.getSubMenu().addItem(this.createRowItemWithIcon("Clear files and text",
                VaadinIcon.TRASH.create(), "18px"), event -> {
            verticalLayoutArea.removeAll();
            verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
            customList.removeAll();
            mapPrefixFileNameAndContent.clear();
            allErrorsList.clear();
            this.counterSpanId.set(0);
            this.anchorDownloadErrors.setEnabled(false);
            this.selectedMainXsd = StringUtils.EMPTY;
            this.selectedXmlFile = StringUtils.EMPTY;
        }).addClassNames(MENU_ITEM_NO_CHECKMARK, DELETE_MENU_ITEM_NO_CHECKMARK);

        itemEllipsis.getSubMenu()
                .getItems()
                .forEach(item -> item.getStyle().setCursor(CURSOR_POINTER));

        return menuBarGridOptions;
    }

    private InMemoryUploadHandler buildUploadHandler() {
        return UploadHandler.inMemory((metadata, data) -> {
                    //without buffered, prevent a SAXException
                    try (final InputStream inputStream = new ByteArrayInputStream(data);
                         final FastByteArrayOutputStream fastOutputStream = new FastByteArrayOutputStream()) {
                        String fileName = metadata.fileName();
                        if (this.decompressionService.isCompressedFile(fileName)) {
                            List<DecompressedFile> files = this.decompressionService.decompressFile(fileName, inputStream);
                            files.forEach(decompressedFile -> this.processFile(metadata, decompressedFile.content(), true, decompressedFile));
                        } else {
                            inputStream.transferTo(fastOutputStream);
                            byte[] bytes = fastOutputStream.toByteArray();
                            this.processFile(metadata, bytes, false, new DecompressedFile(null, null, 0L));
                        }
                    } catch (Exception error) {
                        log.error(error.getMessage());
                        ConfirmDialogBuilder.showWarning("Upload failed: " + error.getMessage());
                    }
                })
                .whenStart(() -> {
                    log.info("Upload started");
                    this.uploader.clearFileList();
                })
                .whenComplete((transferContext, aBoolean) -> log.info("Upload complete"));
    }

    private void validateXmlInputWithXsdSchema() {
        byte[] inputXml = this.mapPrefixFileNameAndContent.get(this.selectedXmlFile);
        byte[] inputXsdSchema = this.mapPrefixFileNameAndContent.get(this.selectedMainXsd);

        this.validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsdSchema)
                .switchIfEmpty(Mono.defer(() -> {
                    this.executeUI(() -> ConfirmDialogBuilder.showInformation("Validation successful!!!"));
                    return Mono.empty();
                }))
                .doOnError(onError -> {
                    this.executeUI(() -> {
//                        log.error("Error validating: {}", xmlFileName, onError);
                        ConfirmDialogBuilder.showWarning("Validation error " + onError.getLocalizedMessage());
                        this.buildErrorSpanAndUpdate(onError.getLocalizedMessage());
                    });
                })
                .delayElements(Duration.ofMillis(50), Schedulers.boundedElastic())
                .doOnTerminate(() -> {
                    log.info("Terminated!");
                    this.executeUI(() -> validateButton.setEnabled(true));
                })
                .subscribe(word -> {
                    super.getUI().ifPresent(ui -> {
                        ui.access(() -> {
                            if (!word.isEmpty()) {
//                                log.info(word);
                                this.buildErrorSpanAndUpdate(word);
                                this.anchorDownloadErrors.setEnabled(!allErrorsList.isEmpty());
                            }
                        });
                    });
                });
    }

    private void buildErrorSpanAndUpdate(String word) {
        //Obliga a crear otro span mas abajo en el VerticalLayout
        if (word.equals("ERROR:") || word.equals("WARNING:") || word.equals("FATAL:")) {
            this.spanWordError = this.buildErrorSpan();
            verticalLayoutArea.add(this.spanWordError);
            this.allErrorsList.add(StringUtils.LF);
        }
        this.allErrorsList.add(word);
        this.spanWordError.getElement().executeJs(JS_COMMAND, word);
    }

    private Span buildErrorSpan() {
        final Span span = new Span();
        span.setId(String.valueOf(this.counterSpanId.incrementAndGet()));
        Tooltip.forComponent(span).setText("Copy text");
        span.addClassName("parent-span");
        span.addClickListener(event -> {
            span.getId().ifPresent(id -> {
                span.getElement().executeJs("""
                        return Array.from(this.querySelectorAll('.error-word'))
                            .map(span => span.textContent)
                            .join('')
                        """
                ).then(String.class, textToCopy -> {
                    UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, textToCopy);
                    Notification.show("Error #" + id + " copied!", 2000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                });
            });
        });
        return span;
    }

    private void processFile(final UploadMetadata uploadMetadata, byte[] readedBytesFromFile, boolean isCompressed,
                             DecompressedFile decompressedFile) {

        final String fileName = isCompressed ? decompressedFile.fileName() : uploadMetadata.fileName();
        long contentLength = isCompressed ? decompressedFile.content().length : uploadMetadata.contentLength();
        mapPrefixFileNameAndContent.put(fileName, readedBytesFromFile);

        // Definimos el comportamiento: Qué pasa cuando ALGUIEN selecciona este item
        final FileListItem fileListItem = this.buildFileListItem(fileName, contentLength);
        customList.add(fileListItem);

        ContextMenu contextMenu = this.buildContextMenu(fileListItem);
        contextMenu.addItem(this.createRowItemWithIcon("Delete", VaadinIcon.TRASH.create(), "15px"), event -> {
            event.getSource().getUI().ifPresent(ui -> {
                ConfirmDialogBuilder.showConfirmInformation("Do you want to delete: " + fileName, ui)
                        .addConfirmListener(confirm -> {
                            customList.remove(fileListItem);
                            mapPrefixFileNameAndContent.remove(fileName, readedBytesFromFile);
                            this.uploader.clearFileList();
                            // Si borramos el que estaba seleccionado, limpiar la variable
                            if (fileName.equals(this.selectedMainXsd)) {
                                this.selectedMainXsd = StringUtils.EMPTY;
                            }
                            if (fileName.equals(this.selectedXmlFile)) {
                                this.selectedXmlFile = StringUtils.EMPTY;
                            }
                        });
            });
        }).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);
    }

    private FileListItem buildFileListItem(String fileName, long contentLength) {
        BiConsumer<FileListItem, Boolean> onItemSelected = null;
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(XML)) {
            onItemSelected = (selectedItem, isChecked) -> {
                if (isChecked) {
                    this.selectedXmlFile = selectedItem.getFileName();
                    this.clearOtherSelections(selectedItem, XML);
                } else {
                    if (selectedItem.getFileName().equals(this.selectedXmlFile)) {
                        this.selectedXmlFile = StringUtils.EMPTY;
                    }
                }
            };
        } else {
            onItemSelected = (item, isChecked) -> {
                if (isChecked) {
                    this.selectedMainXsd = item.getFileName();
                    this.clearOtherSelections(item, XSD);
                } else {
                    if (item.getFileName().equals(this.selectedMainXsd)) {
                        this.selectedMainXsd = StringUtils.EMPTY;
                    }
                }
            };
        }
        return new FileListItem(fileName, contentLength, onItemSelected);
    }

    /**
     * Recorre la lista y desmarca los items que no sean el actual
     * Y que coincidan con la extensión (para no desmarcar XMLs si selecciono XSDs)
     */
    private void clearOtherSelections(FileListItem currentItem, String extensionFilter) {
        customList.getChildren().forEach(component -> {
            if (component instanceof FileListItem item) {
                if (item != currentItem && item.isChecked() && item.getFileName().toLowerCase().endsWith(extensionFilter)) {
                    item.setSelected(false);
                }
            }
        });
    }

    public ContextMenu buildContextMenu(Component target) {
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

    private String textProcessing() {
        return String.join(" ", this.allErrorsList
                        .stream()
                        .map(item -> item.equals(StringUtils.LF) ? item.concat(StringUtils.LF) : item)
                        .toList())
                .trim();
    }

    private void executeUI(Command command) {
        super.getUI().ifPresent(ui -> {
            try {
                ui.access(command);
            } catch (UIDetachedException ex) {
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
    }

}
