package com.simplifica.storage.adapter;

import com.simplifica.storage.config.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating storage adapters (Flysystem-style).
 *
 * Creates the appropriate adapter based on configuration.
 */
@Component
public class StorageAdapterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageAdapterFactory.class);

    /**
     * Creates a storage adapter based on the provider type.
     *
     * @param properties storage properties
     * @param baseUrl base URL for serving files
     * @return the configured storage adapter
     * @throws IllegalArgumentException if provider is unknown or misconfigured
     */
    public StorageAdapter createAdapter(StorageProperties properties, String baseUrl) {
        String provider = properties.getProvider().toLowerCase();

        LOGGER.info("Creating storage adapter: {}", provider);

        switch (provider) {
            case "local", "filesystem":
                return createFilesystemAdapter(properties, baseUrl);

            case "dropbox":
                return createDropboxAdapter(properties);

            case "googledrive":
                return createGoogleDriveAdapter(properties);

            default:
                throw new IllegalArgumentException(
                        "Unknown storage provider: " + provider +
                        ". Supported: local, dropbox, googledrive");
        }
    }

    private StorageAdapter createFilesystemAdapter(StorageProperties properties, String baseUrl) {
        String rootPath = properties.getFilesystem().getRootPath();

        if (rootPath == null || rootPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Filesystem root path is required. Set STORAGE_FILESYSTEM_ROOT");
        }

        LOGGER.info("Initializing filesystem adapter: {}", rootPath);
        return new FilesystemStorageAdapter(rootPath, baseUrl);
    }

    private StorageAdapter createDropboxAdapter(StorageProperties properties) {
        String accessToken = properties.getDropbox().getAccessToken();
        String basePath = properties.getDropbox().getBasePath();

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Dropbox access token is required. Set STORAGE_DROPBOX_ACCESS_TOKEN");
        }

        if (basePath == null || basePath.isBlank()) {
            basePath = "/simplifica";
        }

        LOGGER.info("Initializing Dropbox adapter: {}", basePath);
        return new DropboxStorageAdapter(accessToken, basePath);
    }

    private StorageAdapter createGoogleDriveAdapter(StorageProperties properties) {
        String credentialsPath = properties.getGoogleDrive().getCredentialsPath();
        String baseFolderName = properties.getGoogleDrive().getBaseFolderName();

        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Google Drive credentials path is required. Set STORAGE_GOOGLEDRIVE_CREDENTIALS_PATH");
        }

        if (baseFolderName == null || baseFolderName.isBlank()) {
            baseFolderName = "Simplifica";
        }

        LOGGER.info("Initializing Google Drive adapter: {}", baseFolderName);
        return new GoogleDriveStorageAdapter(credentialsPath, baseFolderName);
    }
}
