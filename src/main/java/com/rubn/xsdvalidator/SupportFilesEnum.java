package com.rubn.xsdvalidator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SupportFilesEnum {

    ZIP("zip"),
    FILE_7Z("7z"),
    RAR("rar"),
    UNKNOWN("unknown file");

    final String supportFile;

    public static SupportFilesEnum fromExtension(String extension) {
        return Arrays.stream(SupportFilesEnum.values())
                .filter(index -> index.getSupportFile().equals(extension))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
