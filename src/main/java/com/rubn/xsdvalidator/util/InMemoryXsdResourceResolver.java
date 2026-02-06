package com.rubn.xsdvalidator.util;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

/**
 *
 * Resolve the necessary schemes; in this case, everything will be in memory.
 *
 * @author rubn
 */
public class InMemoryXsdResourceResolver implements LSResourceResolver {

    private final Map<String, byte[]> mapPrefixFileNameAndContent;

    public InMemoryXsdResourceResolver(Map<String, byte[]> mapPrefixFileNameAndContent) {
        this.mapPrefixFileNameAndContent = mapPrefixFileNameAndContent;
    }

    /**
     * @return A {@link org.w3c.dom.ls.LSInput}
     */
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            final byte[] fileBytes = mapPrefixFileNameAndContent.get(this.cleanFileName(systemId));
            // Check if the XSD file exists in the classpath
            if (fileBytes == null) {
                throw new IOException("The imported schema was not found: " + systemId);
            }
            return new XsdLSInput(publicId, systemId, new ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error resolving imported schema: " + systemId, e);
        }
    }

    private String cleanFileName(String path) {
        if (path == null) return null;
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
    }

}
