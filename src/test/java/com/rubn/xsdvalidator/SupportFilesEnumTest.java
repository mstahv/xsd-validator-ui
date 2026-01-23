package com.rubn.xsdvalidator;

import com.rubn.xsdvalidator.enums.SupportFilesEnum;
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

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

}