package com.simplifica.storage.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Filesystem storage adapter (default/local).
 *
 * Stores files in the local filesystem.
 */
public class FilesystemStorageAdapter implements StorageAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemStorageAdapter.class);

    private final String rootPath;
    private final String baseUrl;

    public FilesystemStorageAdapter(String rootPath, String baseUrl) {
        this.rootPath = rootPath;
        this.baseUrl = baseUrl;

        // Create root directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(rootPath));
            LOGGER.info("Filesystem adapter initialized: {}", rootPath);
        } catch (IOException e) {
            throw new StorageException("Failed to create root directory: " + rootPath, e);
        }
    }

    @Override
    public String store(String path, InputStream inputStream, String contentType) throws StorageException {
        Path filePath = Paths.get(rootPath, path);

        try {
            // Create parent directories
            Files.createDirectories(filePath.getParent());

            // Copy file
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.debug("File stored: {}", path);
            return path;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + path, e);
        }
    }

    @Override
    public InputStream retrieve(String path) throws StorageException {
        Path filePath = Paths.get(rootPath, path);

        if (!Files.exists(filePath)) {
            throw new StorageException("File not found: " + path);
        }

        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to retrieve file: " + path, e);
        }
    }

    @Override
    public boolean delete(String path) throws StorageException {
        Path filePath = Paths.get(rootPath, path);

        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = Paths.get(rootPath, path);
        return Files.exists(filePath);
    }

    @Override
    public String getPublicUrl(String path) {
        // For filesystem, we don't have direct public URLs
        // Files are served through the application
        return null;
    }

    @Override
    public String getAdapterType() {
        return "filesystem";
    }
}
