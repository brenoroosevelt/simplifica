package com.simplifica.application.service;

import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.storage.adapter.StorageAdapter;
import com.simplifica.storage.adapter.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;

/**
 * Service for storing and retrieving process mapping ZIP files using the storage adapter.
 *
 * Handles ZIP extraction and stores each file individually via StorageAdapter,
 * preserving the folder structure. All files are stored under:
 *   processes/{processId}/{relativePath}
 *
 * This enables the ProcessHtmlController to proxy files maintaining relative URL
 * structure, which is required for Bizagi exports (HTML references CSS/JS/images
 * via relative paths).
 */
@Service
@Slf4j
public class ProcessMappingStorageService {

    private static final long MAX_ZIP_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    private static final long MAX_EXTRACTED_SIZE_BYTES = 100 * 1024 * 1024; // 100MB

    private static final Set<String> ALLOWED_ZIP_EXTENSIONS = Set.of(
        "html", "htm", "css", "js", "json",
        "jpg", "jpeg", "png", "gif", "svg", "webp",
        "txt", "xml",
        "woff", "woff2", "ttf", "eot", "otf"
    );

    @Autowired
    private StorageAdapter storageAdapter;

    /**
     * Validates, extracts and stores a ZIP file via the storage adapter.
     * Each file in the ZIP is stored at: processes/{processId}/{relativePath}
     *
     * @param file the ZIP file
     * @param processId the process UUID (used as storage prefix)
     * @return relative path to the entry-point HTML file (e.g., "index.html" or "subdir/file.html")
     * @throws BadRequestException if validation fails
     */
    public String storeZip(MultipartFile file, UUID processId) {
        validateZipFile(file);

        Path tempZipPath = null;
        try {
            // Write to temp file (ZipFile requires random access)
            tempZipPath = Files.createTempFile("mapping-upload-", ".zip");
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tempZipPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String indexHtmlPath = extractAndStore(tempZipPath.toFile(), processId);
            log.info("ZIP stored successfully for process {}, entry point: {}", processId, indexHtmlPath);
            return indexHtmlPath;

        } catch (IOException e) {
            log.error("Failed to process ZIP for process {}", processId, e);
            throw new BadRequestException("Failed to process ZIP file: " + e.getMessage(), e);
        } finally {
            if (tempZipPath != null) {
                try { Files.deleteIfExists(tempZipPath); } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Deletes all stored files for a process mapping.
     * Removes the entire processes/{processId}/ directory from storage.
     *
     * @param processId the process UUID
     */
    public void deleteAll(UUID processId) {
        String prefix = "processes/" + processId;
        try {
            storageAdapter.deleteDirectory(prefix);
            log.info("Deleted all mapping files for process {}", processId);
        } catch (StorageException e) {
            log.warn("Failed to delete mapping files for process {}: {}", processId, e.getMessage());
        }
    }

    private String extractAndStore(File zipFile, UUID processId) throws IOException {
        String storagePrefix = "processes/" + processId + "/";
        String indexHtmlPath = null;
        long totalExtractedSize = 0;

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Security: prevent path traversal
                if (entryName.contains("..")) {
                    log.warn("Path traversal attempt in ZIP entry: {}", entryName);
                    throw new BadRequestException("Invalid file path in ZIP: " + entryName);
                }

                // Skip directories and hidden files
                if (entry.isDirectory() || entryName.startsWith(".") || entryName.contains("/.")) {
                    continue;
                }

                // Validate extension
                String extension = getExtension(entryName);
                if (extension == null || !ALLOWED_ZIP_EXTENSIONS.contains(extension)) {
                    log.debug("Skipping file with disallowed extension: {}", entryName);
                    continue;
                }

                // Check total size limit
                totalExtractedSize += entry.getSize();
                if (totalExtractedSize > MAX_EXTRACTED_SIZE_BYTES) {
                    throw new BadRequestException("Extracted content exceeds maximum size of 100 MB");
                }

                // Store file via adapter
                String storagePath = storagePrefix + entryName;
                String contentType = guessContentType(extension);

                try (InputStream entryStream = zip.getInputStream(entry)) {
                    storageAdapter.store(storagePath, entryStream, contentType);
                } catch (StorageException e) {
                    throw new IOException("Failed to store file " + entryName + ": " + e.getMessage(), e);
                }

                log.debug("Stored: {}", storagePath);

                // Track index.html / entry-point HTML
                if (entryName.equalsIgnoreCase("index.html") || entryName.equalsIgnoreCase("index.htm")) {
                    indexHtmlPath = entryName;
                } else if (indexHtmlPath == null && (extension.equals("html") || extension.equals("htm"))) {
                    indexHtmlPath = entryName;
                }
            }
        }

        if (indexHtmlPath == null) {
            throw new BadRequestException("No HTML file found in ZIP");
        }

        return indexHtmlPath;
    }

    private void validateZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("ZIP file is required");
        }
        if (file.getSize() > MAX_ZIP_FILE_SIZE_BYTES) {
            throw new BadRequestException("ZIP file size exceeds maximum allowed size of 50 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null
                || (!contentType.equals("application/zip")
                    && !contentType.equals("application/x-zip-compressed")
                    && !contentType.equals("application/octet-stream"))) {
            throw new BadRequestException("File must be a ZIP file");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new BadRequestException("File must have .zip extension");
        }
    }

    private String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (dot > slash && dot < path.length() - 1) {
            return path.substring(dot + 1).toLowerCase();
        }
        return null;
    }

    private String guessContentType(String extension) {
        return switch (extension) {
            case "html", "htm" -> "text/html";
            case "css"         -> "text/css";
            case "js"          -> "application/javascript";
            case "json"        -> "application/json";
            case "xml"         -> "application/xml";
            case "svg"         -> "image/svg+xml";
            case "png"         -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif"         -> "image/gif";
            case "webp"        -> "image/webp";
            case "woff"        -> "font/woff";
            case "woff2"       -> "font/woff2";
            case "ttf"         -> "font/ttf";
            case "eot"         -> "application/vnd.ms-fontobject";
            case "otf"         -> "font/otf";
            default            -> "application/octet-stream";
        };
    }
}
