import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter';
import React, { ReactElement } from 'react';
import Editor from 'react-simple-code-editor';
import { highlight, languages } from 'prismjs/components/prism-core';
import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-markup';
import 'prismjs/themes/prism-dark.css';

function MyReactEditor({ content, onContentChange }: any) {
    // 1. Logica exacta del ejemplo para calcular lineas y padding
    const code = content || "";

    // Count newlines and pad to match actual line numbers (+2 logic from snippet)
    const lines = (code.match(/\n/g) || []).length + 2;

    // Determine padding needed
    const pad = String(lines).length;

    // Calculamos el ancho de la columna lateral en pixeles
    const sidebarWidth = 20 + pad * 8;

    // 2. Adaptamos el gradiente (bg) al tema oscuro (#1e1e1e)
    // El ejemplo usaba blanco/gris, aquí usamos colores oscuros para que cuadre con tu Prism
    const bg = `linear-gradient(90deg, #252526 ${sidebarWidth}px, #252526 ${sidebarWidth}px, #1e1e1e 100%)`;

    // 3. Generamos los números
    // NOTA: Usamos '\n' en lugar de '\\00000a' porque estamos en un DIV de React, no en CSS content
    const lineNos = [...Array(lines).keys()].slice(1).join('\n');

    return (
        // Reemplazo de <PseudoBox> por un <div> contenedor
        <div style={{
            position: 'relative',
            height: '100%',
            width: '100%',
            background: bg, // Aquí aplicamos el gradiente calculado
            overflow: 'auto',
            fontFamily: '"Fira code", "Fira Mono", monospace',
            fontSize: 14
        }}>
            {/* Simulacion del _before: Un div absoluto con los numeros */}
            <div style={{
                position: 'absolute',
                width: `${sidebarWidth}px`,
                paddingTop: '10px', // Mismo padding que el Editor
                paddingRight: '10px',
                textAlign: 'right',
                whiteSpace: 'pre',
                color: '#858585', // Color de los números
                userSelect: 'none',
                pointerEvents: 'none', // Para que los clics pasen al editor si es necesario
                top: 0,
                bottom: 0
            }}>
                {lineNos}
            </div>

            {/* El Editor con el margen calculado */}
            <Editor
                value={code}
                onValueChange={onContentChange}
                highlight={code => highlight(code, languages.markup || languages.js)}
                padding={10}
                style={{
                    fontFamily: 'inherit',
                    fontSize: 'inherit',
                    marginLeft: `${sidebarWidth}px`, // Desplazamos el editor a la derecha
                    backgroundColor: 'transparent', // Transparente para ver el gradiente de fondo
                    minHeight: '100%',
                    whiteSpace: 'pre', // Importante para mantener alineacion
                    color: '#f8f8f2'
                }}
            />
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