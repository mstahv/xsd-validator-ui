package com.rubn.xsdvalidator.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.io.InputStream;

/**
 * Esta clase implementa la interfaz `LSResourceResolver` y es responsable de
 * resolver los recursos XSD importados durante el proceso de validación de un
 * esquema XML.
 * 
 * Proporciona la funcionalidad de buscar y cargar los esquemas XSD desde una
 * ubicación específica (en este caso, la carpeta `schemas/xsd/` dentro del
 * classpath).
 */
public class XsdResourceResolver implements LSResourceResolver {

    /**
     * Este método sobrescribe el método `resolveResource` de la interfaz
     * `LSResourceResolver` y se encarga de buscar el recurso XSD importado en el
     * classpath.
     * 
     * La búsqueda se realiza bajo la carpeta `schemas/xsd/` y, si se encuentra el
     * archivo, se carga como un `InputStream`. Luego, se crea un objeto
     * `XsdLSInput` con la información del XSD.
     * 
     * @param type         El tipo de recurso a resolver (generalmente
     *                     "http://www.w3.org/2001/XMLSchema").
     * @param namespaceURI El URI del espacio de nombres del XSD.
     * @param publicId     El identificador público del recurso XSD.
     * @param systemId     El identificador de sistema (usualmente la ruta del
     *                     archivo XSD).
     * @param baseURI      La URI base que se utiliza para resolver las rutas
     *                     relativas.
     * @return Un objeto `LSInput` que representa el recurso XSD importado.
     */
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            // Construir la ruta al archivo XSD importado dentro de la carpeta
            // "schemas/xsd/"
            String path = "schemas/xsd/" + systemId;

            // Crear un recurso Spring para acceder al archivo XSD
            Resource importedResource = new ClassPathResource(path);

            // Verificar si el archivo XSD existe en el classpath
            if (!importedResource.exists()) {
                // Si el archivo no existe, lanzar una excepción
                throw new IOException("No se encontró el esquema importado: " + path);
            }

            // Obtener el InputStream del archivo XSD
            InputStream inputStream = importedResource.getInputStream();

            // Crear y retornar un objeto XsdLSInput con los detalles del esquema importado
            return new XsdLSInput(publicId, systemId, inputStream);

        } catch (Exception e) {
            // En caso de error, lanzar una RuntimeException con un mensaje de error
            throw new RuntimeException("Error resolviendo el esquema importado: " + systemId, e);
        }
    }
}
