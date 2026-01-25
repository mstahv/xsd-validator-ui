package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.function.SerializableConsumer;

/**
 * @author rubn
 */
@NpmPackage(value = "@monaco-editor/react", version = "4.7.0")
@JsModule("./simple-code-editor.tsx")
@Tag("simple-code-editor")
public class CodeEditor extends ReactAdapterComponent implements HasSize {

    public CodeEditor() {
        super();
        // Configuración inicial por defecto
        setTheme("vs-dark");
        setWordWrap(true);
    }

    public void setContent(String value) {
        setState("content", value);
    }

    public String getContent() {
        return getState("content", String.class);
    }

    public void setTheme(String theme) {
        setState("theme", theme);
    }

    public String getTheme() {
        return getState("theme", String.class);
    }

    public void setWordWrap(boolean enabled) {
        setState("wordWrap", enabled);
    }

    public Boolean getWordWrap() {
        return getState("wordWrap", Boolean.class);
    }

    public void addValueChangeListener(SerializableConsumer<String> listener) {
        super.addStateChangeListener("content", String.class, listener);
    }

}