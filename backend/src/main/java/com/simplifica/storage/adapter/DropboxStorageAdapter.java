package com.simplifica.storage.adapter;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dropbox storage adapter.
 *
 * Stores files in Dropbox using the Dropbox API.
 *
 * Configuration needed:
 * - Access Token (from Dropbox App Console)
 */
public class DropboxStorageAdapter implements StorageAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropboxStorageAdapter.class);

    private final DbxClientV2 client;
    private final String basePath;

    /**
     * Creates a Dropbox storage adapter.
     *
     * @param accessToken Dropbox access token
     * @param basePath base path in Dropbox (e.g., "/simplifica")
     */
    public DropboxStorageAdapter(String accessToken, String basePath) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("simplifica-backend").build();
        this.client = new DbxClientV2(config, accessToken);
        this.basePath = basePath.startsWith("/") ? basePath : "/" + basePath;

        LOGGER.info("Dropbox adapter initialized with base path: {}", this.basePath);
    }

    @Override
    public String store(String path, InputStream inputStream, String contentType) throws StorageException {
        String fullPath = getFullPath(path);

        try {
            FileMetadata metadata = client.files()
                    .uploadBuilder(fullPath)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);

            LOGGER.debug("File stored in Dropbox: {}", fullPath);
            return metadata.getPathDisplay();

        } catch (DbxException | IOException e) {
            throw new StorageException("Failed to store file in Dropbox: " + path, e);
        }
    }

    @Override
    public InputStream retrieve(String path) throws StorageException {
        String fullPath = getFullPath(path);

        try {
            return client.files().download(fullPath).getInputStream();
        } catch (DbxException e) {
            throw new StorageException("Failed to retrieve file from Dropbox: " + path, e);
        }
    }

    @Override
    public boolean delete(String path) throws StorageException {
        String fullPath = getFullPath(path);

        try {
            client.files().deleteV2(fullPath);
            LOGGER.debug("File deleted from Dropbox: {}", fullPath);
            return true;
        } catch (DbxException e) {
            if (e.getMessage().contains("not_found")) {
                return false;
            }
            throw new StorageException("Failed to delete file from Dropbox: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        String fullPath = getFullPath(path);

        try {
            client.files().getMetadata(fullPath);
            return true;
        } catch (DbxException e) {
            return false;
        }
    }

    @Override
    public String getPublicUrl(String path) {
        String fullPath = getFullPath(path);

        try {
            // Create shared link if doesn't exist
            SharedLinkMetadata linkMetadata = client.sharing()
                    .createSharedLinkWithSettings(fullPath);

            // Convert to direct download URL
            return linkMetadata.getUrl().replace("?dl=0", "?dl=1");

        } catch (DbxException e) {
            // Link might already exist, try to get existing links
            try {
                var links = client.sharing().listSharedLinksBuilder()
                        .withPath(fullPath)
                        .withDirectOnly(true)
                        .start();

                if (!links.getLinks().isEmpty()) {
                    return links.getLinks().get(0).getUrl().replace("?dl=0", "?dl=1");
                }
            } catch (DbxException ex) {
                LOGGER.warn("Failed to get public URL for: {}", path, ex);
            }

            return null;
        }
    }

    @Override
    public String getAdapterType() {
        return "dropbox";
    }

    private String getFullPath(String path) {
        // Ensure path starts with /
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return basePath + normalizedPath;
    }
}
