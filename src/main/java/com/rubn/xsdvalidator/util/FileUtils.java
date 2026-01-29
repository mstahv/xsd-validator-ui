package com.rubn.xsdvalidator.util;

import lombok.experimental.UtilityClass;

/**
 * @author rubn formatSize
 */
@UtilityClass
public class FileUtils {

    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "kMG".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }

    public String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

}