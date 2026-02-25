package com.simplifica.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Configuration properties for file storage (Flysystem-style).
 *
 * Supports multiple storage providers: local, dropbox, googledrive
 * Switch provider via STORAGE_PROVIDER environment variable.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /**
     * Storage provider: local, dropbox, googledrive
     */
    private String provider = "local";

    /**
     * Filesystem (local) configuration
     */
    private Filesystem filesystem = new Filesystem();

    /**
     * Dropbox configuration
     */
    private Dropbox dropbox = new Dropbox();

    /**
     * Google Drive configuration
     */
    private GoogleDrive googleDrive = new GoogleDrive();

    /**
     * Maximum file size for regular files (in bytes)
     */
    private long maxFileSize = 100 * 1024 * 1024; // 100MB

    /**
     * Maximum file size for videos (in bytes)
     */
    private long maxVideoSize = 500 * 1024 * 1024; // 500MB

    /**
     * Allowed MIME types for images
     */
    private Set<String> allowedImageTypes = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    /**
     * Allowed MIME types for documents
     */
    private Set<String> allowedDocumentTypes = Set.of(
            "application/pdf",
            "application/zip"
    );

    /**
     * Allowed MIME types for videos
     */
    private Set<String> allowedVideoTypes = Set.of(
            "video/mp4",
            "video/webm"
    );

    /**
     * Cache configuration
     */
    private Cache cache = new Cache();

    @Data
    public static class Filesystem {
        private String rootPath = "/tmp/simplifica/storage";
    }

    @Data
    public static class Dropbox {
        private String accessToken;
        private String basePath = "/simplifica";
    }

    @Data
    public static class GoogleDrive {
        private String credentialsPath;
        private String baseFolderName = "Simplifica";
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private long maxAge = 86400; // 24 hours in seconds
    }
}
