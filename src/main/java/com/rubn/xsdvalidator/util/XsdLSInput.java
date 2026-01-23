package com.rubn.xsdvalidator.util;

import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;

/**
 * Esta clase implementa la interfaz LSInput, que se utiliza para representar
 * un objeto de entrada (Input) que contiene los datos necesarios para resolver
 * un esquema XML. Esta clase es parte de la configuración que maneja el
 * procesamiento de documentos XML, especialmente cuando se trabaja con esquemas
 * XSD.
 */
public class XsdLSInput implements LSInput {

    // Identificadores públicos y de sistema del XSD
    private String publicId;
    private String systemId;

    // InputStream donde se almacenan los bytes del XSD
    private InputStream inputStream;

    /**
     * Constructor que inicializa el objeto XsdLSInput con el publicId, systemId y
     * un InputStream.
     * 
     * @param publicId    Identificador público del recurso XSD.
     * @param systemId    Identificador de sistema (por ejemplo, la URL del XSD).
     * @param inputStream El InputStream del archivo XSD.
     */
    public XsdLSInput(String publicId, String systemId, InputStream inputStream) {
        this.publicId = publicId;
        this.systemId = systemId;
        this.inputStream = inputStream;
    }

    /**
     * Obtiene el InputStream asociado con el recurso XSD.
     * 
     * @return El InputStream que contiene los datos del XSD.
     */
    @Override
    public InputStream getByteStream() {
        return inputStream;
    }

    /**
     * Establece el InputStream para el recurso XSD.
     * 
     * @param byteStream El InputStream que se va a asignar al recurso XSD.
     */
    @Override
    public void setByteStream(InputStream byteStream) {
        this.inputStream = byteStream;
    }

    /**
     * Obtiene el publicId (identificador público) del recurso XSD.
     * 
     * @return El publicId del XSD.
     */
    @Override
    public String getPublicId() {
        return publicId;
    }

    /**
     * Establece el publicId para el recurso XSD.
     * 
     * @param publicId El publicId que se asignará al recurso XSD.
     */
    @Override
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    /**
     * Obtiene el systemId (identificador de sistema, normalmente una URL) del
     * recurso XSD.
     * 
     * @return El systemId del XSD.
     */
    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * Establece el systemId para el recurso XSD.
     * 
     * @param systemId El systemId que se asignará al recurso XSD.
     */
    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Obtiene la base URI para el recurso XSD. En este caso, no se utiliza y
     * se retorna null.
     * 
     * @return null (no se usa en este contexto).
     */
    @Override
    public String getBaseURI() {
        return null;
    }

    /**
     * Establece la base URI para el recurso XSD. Este método está vacío ya que
     * no se utiliza en este contexto.
     * 
     * @param baseURI La base URI que se desea establecer.
     */
    @Override
    public void setBaseURI(String baseURI) {
        // Método intencionalmente vacío. No se utiliza baseURI en este contexto.
    }

    /**
     * Obtiene el valor del atributo "certifiedText". En este caso, siempre retorna
     * falso, ya que no se usa la funcionalidad de texto certificado en este
     * contexto.
     * 
     * @return false (no se usa en este contexto).
     */
    @Override
    public boolean getCertifiedText() {
        return false;
    }

    /**
     * Establece el valor del atributo "certifiedText". Este método está vacío ya
     * que
     * no se utiliza en este contexto.
     * 
     * @param certifiedText El valor que se desea asignar a certifiedText.
     */
    @Override
    public void setCertifiedText(boolean certifiedText) {
        // Método intencionalmente vacío. No se utiliza certifiedText en este contexto.
    }

    /**
     * Obtiene el Reader de caracteres asociado al recurso XSD. Este método retorna
     * null ya que no se utiliza un Reader de caracteres en este contexto.
     * 
     * @return null (no se utiliza en este contexto).
     */
    @Override
    public Reader getCharacterStream() {
        return null;
    }

    /**
     * Establece el Reader de caracteres para el recurso XSD. Este método está vacío
     * ya que no se usa un Reader de caracteres en este contexto.
     * 
     * @param characterStream El Reader de caracteres que se desea asignar.
     */
    @Override
    public void setCharacterStream(Reader characterStream) {
        // Método intencionalmente vacío. No se utiliza CharacterStream en este
        // contexto.
    }

    /**
     * Obtiene el valor de la codificación asociada al recurso XSD. Este método
     * retorna null ya que no se usa codificación en este contexto.
     * 
     * @return null (no se utiliza en este contexto).
     */
    @Override
    public String getEncoding() {
        return null;
    }

    /**
     * Establece la codificación para el recurso XSD. Este método está vacío ya que
     * no se utiliza codificación en este contexto.
     * 
     * @param encoding La codificación que se desea asignar.
     */
    @Override
    public void setEncoding(String encoding) {
        // Método intencionalmente vacío. No se utiliza encoding en este contexto.
    }

    /**
     * Obtiene los datos en formato de texto del recurso XSD. Este método retorna
     * null ya que no se usan datos en formato texto en este contexto.
     * 
     * @return null (no se utiliza en este contexto).
     */
    @Override
    public String getStringData() {
        return null;
    }

    /**
     * Establece los datos en formato de texto para el recurso XSD. Este método
     * está vacío ya que no se utiliza texto en este contexto.
     * 
     * @param stringData Los datos en formato texto que se desean asignar.
     */
    @Override
    public void setStringData(String stringData) {
        // Método intencionalmente vacío. No se utiliza stringData en este contexto.
    }
}
