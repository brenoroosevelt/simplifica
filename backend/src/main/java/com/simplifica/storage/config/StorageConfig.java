package com.simplifica.storage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.content.fs.config.EnableFilesystemStores;
import org.springframework.content.fs.io.FileSystemResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Configuration for Spring Content file storage.
 *
 * Enables filesystem stores and configures the storage root directory.
 * In the future, this can be extended to support S3, Azure, or GCS
 * by adding conditional beans based on storage type.
 */
@Configuration
@EnableFilesystemStores
public class StorageConfig {

    @Autowired
    private StorageProperties storageProperties;

    /**
     * Configures the filesystem resource loader with the storage root directory.
     * Creates the directory if it doesn't exist.
     *
     * @return configured FileSystemResourceLoader
     */
    @Bean
    public FileSystemResourceLoader fileSystemResourceLoader() {
        File root = new File(storageProperties.getFilesystem().getRootPath());

        // Create storage root directory if it doesn't exist
        if (!root.exists()) {
            boolean created = root.mkdirs();
            if (!created) {
                throw new IllegalStateException(
                        "Failed to create storage root directory: " + storageProperties.getFilesystem().getRootPath());
            }
        }

        return new FileSystemResourceLoader(root.getAbsolutePath());
    }

    /**
     * Future: Add S3 configuration when needed
     */
    // @Bean
    // @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
    // public S3ResourceLoader s3ResourceLoader() {
    //     return new S3ResourceLoader(s3Client, bucketName);
    // }
}
