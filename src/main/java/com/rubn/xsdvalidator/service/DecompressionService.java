package com.rubn.xsdvalidator.service;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.rubn.xsdvalidator.SupportFilesEnum;
import com.rubn.xsdvalidator.records.DecompressedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author rubn
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DecompressionService {

    public List<DecompressedFile> decompressFile(String fileName, InputStream inputStream) throws IOException {
        String extension = getFileExtension(fileName).toLowerCase();
        SupportFilesEnum supportFilesEnum = SupportFilesEnum.fromExtension(extension);
        return switch (supportFilesEnum) {
            case ZIP -> decompressZip(inputStream);
            case RAR -> decompressRar(inputStream);
            case FILE_7Z -> decompress7z(inputStream);
            default -> throw new IllegalArgumentException("Formato no soportado: " + extension);
        };
    }

    /**
     * Descomprime archivos ZIP
     */
    public List<DecompressedFile> decompressZip(InputStream inputStream) throws IOException {
        List<DecompressedFile> files = new CopyOnWriteArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] content = this.readAllBytes(zis);
                    files.add(new DecompressedFile(entry.getName(), content, entry.getSize()));
                    log.info("Descomprimido: {} ({} bytes)", entry.getName(), entry.getSize());
                }
                zis.closeEntry();
            }
        }

        return files;
    }

    /**
     * Descomprime archivos RAR
     */
    public List<DecompressedFile> decompressRar(InputStream inputStream) throws IOException {
        List<DecompressedFile> files = new ArrayList<>();

        // RAR necesita un archivo temporal porque la biblioteca no soporta streams directamente
        Path tempFile = Files.createTempFile("temp_rar_", ".rar");

        try {
            // Guardar el stream en archivo temporal
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Procesar el RAR
            try (Archive archive = new Archive(tempFile.toFile())) {
                FileHeader fileHeader;

                while ((fileHeader = archive.nextFileHeader()) != null) {
                    if (!fileHeader.isDirectory()) {
                        FastByteArrayOutputStream baos = new FastByteArrayOutputStream();
                        archive.extractFile(fileHeader, baos);

                        byte[] content = baos.toByteArray();
                        files.add(new DecompressedFile(
                                fileHeader.getFileName(),
                                content,
                                fileHeader.getUnpSize()
                        ));
                        log.info("Descomprimido: {} ({} bytes)",
                                fileHeader.getFileName(), fileHeader.getUnpSize());
                    }
                }
            } catch (RarException e) {
                throw new RuntimeException(e);
            }
        } finally {
            // Limpiar archivo temporal
            Files.deleteIfExists(tempFile);
        }

        return files;
    }

    /**
     * Descomprime archivos 7Z
     */
    public List<DecompressedFile> decompress7z(InputStream inputStream) throws IOException {
        List<DecompressedFile> files = new ArrayList<>();

        // Leer todo el contenido en memoria
        byte[] data = inputStream.readAllBytes();

        try (SevenZFile sevenZFile = new SevenZFile(new SeekableInMemoryByteChannel(data))) {
            SevenZArchiveEntry entry;

            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content);

                    files.add(new DecompressedFile(entry.getName(), content, entry.getSize()));
                    log.info("Descomprimido: {} ({} bytes)", entry.getName(), entry.getSize());
                }
            }
        }

        return files;
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * Lee todos los bytes de un InputStream sin cerrarlo
     */
    private byte[] readAllBytes(InputStream is) throws IOException {
        FastByteArrayOutputStream buffer = new FastByteArrayOutputStream();
        is.transferTo(buffer);
        return buffer.toByteArray();
    }

    public boolean isCompressedFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return extension.equals(SupportFilesEnum.ZIP.getSupportFile())
                || extension.equals(SupportFilesEnum.RAR.getSupportFile())
                || extension.equals(SupportFilesEnum.FILE_7Z.getSupportFile());
    }

}

