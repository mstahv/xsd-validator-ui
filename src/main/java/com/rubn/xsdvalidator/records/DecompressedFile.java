package com.rubn.xsdvalidator.records;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author rubn
 *
 * @param name
 * @param content
 * @param size
 */
public record DecompressedFile(String name, byte[] content, long size) {

    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }
}