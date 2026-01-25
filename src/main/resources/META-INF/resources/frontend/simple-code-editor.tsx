import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter';
import React, { ReactElement } from 'react';
import Editor from '@monaco-editor/react';

function MyReactEditor({ content, onContentChange, themeName, wrapEnabled }: any) {

    const handleEditorChange = (value: string | undefined) => {
        if (onContentChange && value !== undefined) {
            onContentChange(value);
        }
    };

    return (
        <div style={{ height: '100%', width: '100%', overflow: 'hidden' }}>
            <Editor
                height="100%"
                defaultLanguage="xml"
                theme={themeName || "vs-dark"}
                value={content || ""}
                onChange={handleEditorChange}

                options={{
                    wordWrap: wrapEnabled ? 'on' : 'off',
                    minimap: { enabled: false },
                    automaticLayout: true,
                    scrollBeyondLastLine: false,
                    fontSize: 14,
                    lineNumbers: 'on',
                    renderLineHighlight: 'all',
                }}
            />
        </div>
    );
}

class SimpleCodeEditorElement extends ReactAdapterElement {

    protected render(hooks: RenderHooks): ReactElement | null {
        const [content, setContent] = hooks.useState<string>("content");
        const [theme] = hooks.useState<string>("theme");
        const [wordWrap] = hooks.useState<boolean>("wordWrap");

        return (
            <MyReactEditor
                content={content}
                themeName={theme}
                wrapEnabled={wordWrap}
                onContentChange={(newVal: string) => {
                    setContent(newVal);
                    this.dispatchEvent(new CustomEvent("editor-change", {
                        detail: { value: newVal },
                        bubbles: true,
                        composed: true
                    }));
                }}
            />
        );
    }
}

customElements.define('simple-code-editor', SimpleCodeEditorElement);