import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter';
import React, { ReactElement, useState, useEffect, useRef } from 'react';
import Editor from '@monaco-editor/react';

function MyReactEditor({ content, onContentChange, themeName, wrapEnabled }: any) {
    const [altPressed, setAltPressed] = useState(false);
    const editorRef = useRef<any>(null);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => { if (e.altKey) setAltPressed(true); };
        const handleKeyUp = (e: KeyboardEvent) => { if (!e.altKey) setAltPressed(false); };
        const handleBlur = () => setAltPressed(false);

        window.addEventListener('keydown', handleKeyDown);
        window.addEventListener('keyup', handleKeyUp);
        window.addEventListener('blur', handleBlur);

        return () => {
            window.removeEventListener('keydown', handleKeyDown);
            window.removeEventListener('keyup', handleKeyUp);
            window.removeEventListener('blur', handleBlur);
        };
    }, []);

    function handleEditorDidMount(editor: any) {
        editorRef.current = editor;

        editor.onMouseDown((e: any) => {
            if (e.event.altKey) {
                const position = e.target.position;
                if (position) {
                    editor.setPosition(position);
                    editor.setSelection({
                        startLineNumber: position.lineNumber,
                        startColumn: position.column,
                        endLineNumber: position.lineNumber,
                        endColumn: position.column
                    });
                }
            }
        });
    }

    const handleEditorChange = (value: string | undefined) => {
        if (onContentChange && value !== undefined) onContentChange(value);
    };

    return (
        <div className={altPressed ? "alt-mode-active" : ""} style={{ height: '100%', width: '100%', overflow: 'hidden' }}>
            <style>{`
                .alt-mode-active .view-lines, .alt-mode-active .view-line,
                .alt-mode-active .monaco-editor, .alt-mode-active .overflow-guard {
                    cursor: crosshair !important;
                }
            `}</style>

            <Editor
                height="100%"
                defaultLanguage="xml"
                theme={themeName || "vs-dark"}
                value={content || ""}
                onChange={handleEditorChange}
                onMount={handleEditorDidMount} // <-- IMPORTANTE
                options={{
                    wordWrap: wrapEnabled ? 'on' : 'off',
                    minimap: { enabled: false },
                    automaticLayout: true,
                    columnSelection: altPressed,
                    multiCursorModifier: 'alt',
                    fontSize: 14,
                    lineNumbers: 'on',
                    scrollBeyondLastLine: false,
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