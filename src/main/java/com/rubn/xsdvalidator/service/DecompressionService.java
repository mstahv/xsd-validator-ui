package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.enums.SupportFilesEnum;
import com.rubn.xsdvalidator.records.DecompressedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
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
            case ZIP -> this.decompressZip(inputStream);
            case RAR -> this.decompressRar(inputStream);
            case FILE_7Z -> this.decompress7z(inputStream);
            default -> throw new IllegalArgumentException("Formato no soportado: " + extension);
        };
    }

    /**
     * Descomprime archivos ZIP
     */
    private List<DecompressedFile> decompressZip(InputStream inputStream) throws IOException {
        List<DecompressedFile> files = new CopyOnWriteArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream);
        final FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream()) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    zis.transferTo(fbaos);
                    byte[] content = fbaos.toByteArray();
                    files.add(new DecompressedFile(entry.getName(), content, entry.getSize()));
                    fbaos.reset();
                    log.info("Descomprimido: {} ({} bytes)", entry.getName(), entry.getSize());
                }
                zis.closeEntry();
            }
        }

        return files;
    }

    /**
     * Descomprime archivos RAR
     * <a href="https://stackoverflow.com/a/74223706/7267818">...</a>
     */
    private List<DecompressedFile> decompressRar(InputStream inputStream) throws IOException {
        List<DecompressedFile> files = new CopyOnWriteArrayList<>();

        Path tempFile = this.toTempFile(inputStream, "tempRarArchive-", SupportFilesEnum.RAR.getSupportFile());
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tempFile.toFile(), "r")) {// open for reading
                try (IInArchive inArchive = SevenZip.openInArchive(null, // autodetect archive type
                        new RandomAccessFileInStream(raf))) {
                    // Getting simple interface of the archive inArchive
                    ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

                    for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                        if (!item.isFolder()) {
                            ExtractOperationResult result;
                            final InputStream[] IS = new InputStream[1];

                            final Integer[] sizeArray = new Integer[1];
                            result = item.extractSlow(new ISequentialOutStream() {
                                /**
                                 * @param bytes of extracted data
                                 * @return size of extracted data
                                 */
                                @Override
                                public int write(byte[] bytes) {
                                    InputStream is = new ByteArrayInputStream(bytes);
                                    sizeArray[0] = bytes.length;
                                    IS[0] = new BufferedInputStream(is); // Data to write to file
                                    return sizeArray[0];
                                }
                            });

                            if (result == ExtractOperationResult.OK) {
                                files.add(new DecompressedFile(Path.of(item.getPath()).getFileName().toString(), this.readAllBytes(IS[0]), sizeArray[0]));
                            } else {
                                log.error("Error extracting item: " + result);
                            }
                        }
                    }
                }
            }

        } finally {
            Files.deleteIfExists(tempFile);
        }

        return files;
    }

    /**
     * Descomprime archivos 7z
     */
    private List<DecompressedFile> decompress7z(InputStream inputStream) throws IOException {
        List<DecompressedFile> files = new CopyOnWriteArrayList<>();

        Path tempFile = toTempFile(inputStream, "temp_7z_", SupportFilesEnum.FILE_7Z.getSupportFile());

        try {
            try (SevenZFile sevenZFile = SevenZFile.builder().setPath(tempFile).get()) {

                SevenZArchiveEntry entry;

                while ((entry = sevenZFile.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        byte[] content = new byte[(int) entry.getSize()];
                        sevenZFile.read(content);

                        files.add(new DecompressedFile(entry.getName(), content, entry.getSize()));
                        log.info("Descomprimido: {} ({} bytes)", entry.getName(), entry.getSize());
                    }
                }
            } catch (IOException e) {
                throw e;
            }
        } finally {
            Files.deleteIfExists(tempFile);
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
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        FastByteArrayOutputStream buffer = new FastByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toByteArray();
    }

    public boolean isCompressedFile(String fileName) {
        String extension = this.getFileExtension(fileName);
        SupportFilesEnum supportFilesEnum = SupportFilesEnum.fromExtension(extension);
        return switch (supportFilesEnum) {
            case FILE_7Z, RAR, ZIP -> true;
            case UNKNOWN -> false;
        };
    }

    private Path toTempFile(InputStream in, String prefix, String subfix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, subfix);
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
            in.transferTo(out);
        }
        return tempFile;
    }

}

