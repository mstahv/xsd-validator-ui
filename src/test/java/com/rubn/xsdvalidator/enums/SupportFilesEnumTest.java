package com.rubn.xsdvalidator.enums;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupportFilesEnumTest {

    @Test
    @DisplayName("zip, 7zip, .rar")
    void a() {

        String file = getFileExtension("schema-xsd.zip");

        SupportFilesEnum supportFilesEnum = SupportFilesEnum.fromExtension(file);

        assertThat(supportFilesEnum).isEqualTo(SupportFilesEnum.ZIP);

    }

    @Test
    @DisplayName("extensions types")
    void extensionTypes() {

        assertThat(SupportFilesEnum.fromExtension(".rar")).isEqualTo(SupportFilesEnum.RAR);
        assertThat(SupportFilesEnum.fromExtension(".zip")).isEqualTo(SupportFilesEnum.ZIP);
        assertThat(SupportFilesEnum.fromExtension(".7z")).isEqualTo(SupportFilesEnum.FILE_7Z);
        assertThat(SupportFilesEnum.fromExtension(".txt")).isEqualTo(SupportFilesEnum.UNKNOWN);
        assertThat(SupportFilesEnum.fromExtension(null)).isEqualTo(SupportFilesEnum.UNKNOWN);
        assertThat(SupportFilesEnum.fromExtension(StringUtils.EMPTY)).isEqualTo(SupportFilesEnum.UNKNOWN);

    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}