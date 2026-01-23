package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.function.SerializableConsumer;

@NpmPackage(value = "react-simple-code-editor", version = "0.13.1")
@NpmPackage(value = "prismjs", version = "1.29.0")
@JsModule("./simple-code-editor.tsx")
@Tag("simple-code-editor")
public class CodeEditor extends ReactAdapterComponent implements HasSize {

    public CodeEditor() {
        super();
        this.setWidthFull();
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        setState("showLineNumbers", showLineNumbers);
    }

    /**
     * Envía texto al editor (Desde Java -> Navegador)
     */
    public void setContent(String value) {
        setState("content", value);
    }

    public String getContent() {
        return getState("content", String.class);
    }

    /**
     * Escucha cambios en el editor (Desde Navegador -> Java)
     * Se dispara cada vez que el usuario escribe.
     */
    public void addValueChangeListener(SerializableConsumer<String> listener) {
        super.addStateChangeListener("content", String.class, listener);
    }

}