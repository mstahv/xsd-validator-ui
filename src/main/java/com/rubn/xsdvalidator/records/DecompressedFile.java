package com.rubn.xsdvalidator.records;

/**
 * @param fileName
 * @param content
 * @param size
 * @author rubn
 */
public record DecompressedFile(String fileName, byte[] content, long size) {
}