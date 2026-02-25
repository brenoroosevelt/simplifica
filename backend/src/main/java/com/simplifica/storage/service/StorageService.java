package com.simplifica.storage.service;

import com.simplifica.storage.domain.FileCategory;
import com.simplifica.storage.domain.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * High-level interface for file storage operations.
 *
 * Provides abstraction over the underlying storage implementation
 * (Spring Content, S3, Azure, etc). Clients should use this interface
 * instead of directly accessing storage backends.
 */
public interface StorageService {

    /**
     * Stores a file for a specific entity with thumbnail generation.
     *
     * @param file the file to store
     * @param entityType the type of entity (e.g., "ValueChain")
     * @param entityId the entity's UUID
     * @param category the file category
     * @param generateThumbnail whether to generate a thumbnail (for images)
     * @return the stored file metadata including thumbnail URL if generated
     */
    FileUploadResult storeFile(
            MultipartFile file,
            String entityType,
            UUID entityId,
            FileCategory category,
            boolean generateThumbnail
    );

    /**
     * Loads a file as a Spring Resource.
     *
     * @param fileId the stored file's UUID
     * @return the file as a Resource
     */
    Resource loadAsResource(UUID fileId);

    /**
     * Loads a file as an InputStream.
     *
     * @param fileId the stored file's UUID
     * @return the file as an InputStream
     */
    InputStream loadAsStream(UUID fileId);

    /**
     * Gets file metadata.
     *
     * @param fileId the stored file's UUID
     * @return the stored file metadata
     */
    StoredFile getMetadata(UUID fileId);

    /**
     * Finds all files for a specific entity.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @return list of stored files
     */
    List<StoredFile> findByEntity(String entityType, UUID entityId);

    /**
     * Finds files by entity and category.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @param category the file category
     * @return list of stored files
     */
    List<StoredFile> findByEntityAndCategory(String entityType, UUID entityId, FileCategory category);

    /**
     * Deletes a file by its UUID.
     *
     * @param fileId the stored file's UUID
     */
    void delete(UUID fileId);

    /**
     * Deletes all files for a specific entity.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     */
    void deleteByEntity(String entityType, UUID entityId);

    /**
     * Deletes files by entity and category.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @param category the file category
     */
    void deleteByEntityAndCategory(String entityType, UUID entityId, FileCategory category);

    /**
     * Gets the public URL for a file.
     *
     * @param fileId the stored file's UUID
     * @return the public URL
     */
    String getFileUrl(UUID fileId);

    /**
     * Result of a file upload operation.
     */
    record FileUploadResult(
            StoredFile storedFile,
            String fileUrl,
            String thumbnailUrl
    ) {}
}
