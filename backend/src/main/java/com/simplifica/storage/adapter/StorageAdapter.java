package com.simplifica.storage.adapter;

import java.io.InputStream;

/**
 * Storage adapter interface (Flysystem-style).
 *
 * Provides a unified API for different storage backends
 * (filesystem, Dropbox, Google Drive, etc).
 *
 * Similar to PHP's Flysystem - change adapter by configuration,
 * not by changing code.
 */
public interface StorageAdapter {

    /**
     * Stores a file in the storage backend.
     *
     * @param path the file path (relative to storage root)
     * @param inputStream the file content
     * @param contentType the MIME type
     * @return the storage-specific identifier or path
     * @throws StorageException if storage operation fails
     */
    String store(String path, InputStream inputStream, String contentType) throws StorageException;

    /**
     * Retrieves a file from the storage backend.
     *
     * @param path the file path
     * @return the file content as InputStream
     * @throws StorageException if file not found or retrieval fails
     */
    InputStream retrieve(String path) throws StorageException;

    /**
     * Deletes a file from the storage backend.
     *
     * @param path the file path
     * @return true if deleted successfully, false if file didn't exist
     * @throws StorageException if deletion fails
     */
    boolean delete(String path) throws StorageException;

    /**
     * Checks if a file exists in the storage backend.
     *
     * @param path the file path
     * @return true if file exists
     */
    boolean exists(String path);

    /**
     * Gets the public URL for a file (if supported).
     *
     * @param path the file path
     * @return the public URL or null if not supported
     */
    String getPublicUrl(String path);

    /**
     * Gets the adapter type name.
     *
     * @return adapter type (e.g., "filesystem", "dropbox", "googledrive")
     */
    String getAdapterType();
}
