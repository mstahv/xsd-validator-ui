import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter';
import React, { ReactElement } from 'react';
import Editor from 'react-simple-code-editor';
import { highlight, languages } from 'prismjs/components/prism-core';
import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-markup';
import 'prismjs/themes/prism-dark.css';

function MyReactEditor({ content, onContentChange }: any) {
    const code = content || "";
    const lines = (code.match(/\n/g) || []).length + 2;
    const pad = String(lines).length;
    const sidebarWidth = 20 + pad * 8;
    const bg = `linear-gradient(90deg, #252526 ${sidebarWidth}px, #252526 ${sidebarWidth}px, #1e1e1e 100%)`;
    const lineNos = [...Array(lines)
        .keys()]
        .slice(1)
        .join('\n');

    return (
        <div
            className="editor-force-nowrap"
            style={{
                position: 'relative',
                height: '100%',
                width: '100%',
                background: bg,
                overflow: 'auto',
                fontFamily: '"Fira code", "Fira Mono", monospace',
                fontSize: 14,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start'
            }}
        >
            <style>{`
                .editor-force-nowrap pre,
                .editor-force-nowrap textarea {
                    white-space: pre !important; /* EL FIX DEFINITIVO */
                    overflow-wrap: normal !important;
                    overflow-x: auto !important;
                }
            `}</style>

            <div style={{
                position: 'sticky',
                left: 0,
                zIndex: 10,
                width: `${sidebarWidth}px`,
                minWidth: `${sidebarWidth}px`,
                paddingTop: '10px',
                paddingRight: '10px',
                textAlign: 'right',
                whiteSpace: 'pre',
                color: '#858585',
                userSelect: 'none',
                pointerEvents: 'none',
                height: '100%'
            }}>
                {lineNos}
            </div>

            <div style={{
                position: 'absolute',
                top: 0,
                left: 0,
                minWidth: '100%',
                minHeight: '100%'
            }}>
                <Editor
                    value={code}
                    onValueChange={onContentChange}
                    highlight={code => highlight(code, languages.markup || languages.js)}
                    padding={10}
                    style={{
                        fontFamily: 'inherit',
                        fontSize: 'inherit',
                        marginLeft: `${sidebarWidth}px`,
                        backgroundColor: 'transparent',
                        minHeight: '100%',
                        color: '#f8f8f2',
                        whiteSpace: 'pre',
                    }}
                />
            </div>
        </div>
    );
}

class SimpleCodeEditorElement extends ReactAdapterElement {
    protected render(hooks: RenderHooks): ReactElement | null {
        const [content, setContent] = hooks.useState<string>("content");

        return (
            <MyReactEditor
                content={content}
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