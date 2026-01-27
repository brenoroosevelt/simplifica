package com.simplifica.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for file storage.
 *
 * Maps configuration from application.yml under the prefix "app.storage".
 * Supports both local filesystem storage and cloud storage (S3).
 *
 * Configuration example in application.yml:
 * <pre>
 * app:
 *   storage:
 *     provider: local
 *     local:
 *       base-path: /tmp/simplifica/uploads
 *       public-url: http://localhost:8080/api/public/uploads
 *     max-file-size-mb: 5
 *     allowed-extensions: jpg,jpeg,png,gif,webp
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class FileStorageProperties {

    /**
     * Storage provider type (local or s3).
     */
    private String provider = "local";

    /**
     * Local filesystem storage configuration.
     */
    private Local local = new Local();

    /**
     * Maximum file size in megabytes.
     */
    private Integer maxFileSizeMb = 5;

    /**
     * List of allowed file extensions (without dots).
     */
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "gif", "webp"));

    /**
     * Local filesystem storage settings.
     */
    @Getter
    @Setter
    public static class Local {

        /**
         * Base path for file storage on local filesystem.
         */
        private String basePath = "/tmp/simplifica/uploads";

        /**
         * Public URL prefix for accessing uploaded files.
         */
        private String publicUrl = "http://localhost:8080/api/public/uploads";
    }

    /**
     * Gets the maximum file size in bytes.
     *
     * @return the maximum file size in bytes
     */
    public long getMaxFileSizeBytes() {
        return (long) maxFileSizeMb * 1024 * 1024;
    }
}
