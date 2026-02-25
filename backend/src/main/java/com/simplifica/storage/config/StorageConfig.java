package com.simplifica.storage.config;

import com.simplifica.storage.adapter.StorageAdapterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private StorageAdapterFactory storageAdapterFactory;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Creates the storage adapter bean based on the configured provider.
     * The adapter is selected at startup via StorageAdapterFactory and can be
     * swapped between filesystem, Dropbox, or Google Drive by configuration.
     *
     * @return the configured StorageAdapter
     */
    @Bean
    public com.simplifica.storage.adapter.StorageAdapter storageAdapter() {
        return storageAdapterFactory.createAdapter(storageProperties, baseUrl);
    }

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
