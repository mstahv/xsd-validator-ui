import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter';
import React from 'react';
import Editor from 'react-simple-code-editor';
import { highlight, languages } from 'prismjs/components/prism-core';
import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-markup'; // XML/HTML
import 'prismjs/themes/prism-dark.css';

function MyReactEditor({ content, onContentChange }) {
    // Renderizado del editor
    return (
        <div style={{
            height: '100%',
            width: '100%',
            backgroundColor: '#1e1e1e',
            overflow: 'auto'
        }}>
            <Editor
                value={content || ''}
                onValueChange={onContentChange}
                highlight={code => highlight(code, languages.markup || languages.js)}
                padding={10}
                style={{
                    fontFamily: '"Fira code", "Fira Mono", monospace',
                    fontSize: 14,
                    minHeight: '100%',
                    color: '#f8f8f2'
                }}
            />
        </div>
    );
}

class SimpleCodeEditorElement extends ReactAdapterElement {
    protected override render(hooks: RenderHooks) {
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