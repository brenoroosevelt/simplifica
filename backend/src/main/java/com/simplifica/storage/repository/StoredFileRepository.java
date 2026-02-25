package com.simplifica.storage.repository;

import com.simplifica.storage.domain.FileCategory;
import com.simplifica.storage.domain.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for StoredFile entity.
 * Provides methods to query file metadata.
 */
@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    /**
     * Finds all files associated with a specific entity.
     *
     * @param entityType the type of entity (e.g., "ValueChain")
     * @param entityId the entity's UUID
     * @return list of stored files
     */
    List<StoredFile> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Finds all files of a specific category for an entity.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @param category the file category
     * @return list of stored files
     */
    List<StoredFile> findByEntityTypeAndEntityIdAndCategory(
            String entityType, UUID entityId, FileCategory category);

    /**
     * Finds a single file by entity and category (useful for single-file associations).
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @param category the file category
     * @return optional stored file
     */
    Optional<StoredFile> findFirstByEntityTypeAndEntityIdAndCategory(
            String entityType, UUID entityId, FileCategory category);

    /**
     * Checks if any files exist for a specific entity.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @return true if files exist
     */
    boolean existsByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Deletes all files associated with an entity.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     */
    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Deletes all files of a specific category for an entity.
     *
     * @param entityType the type of entity
     * @param entityId the entity's UUID
     * @param category the file category
     */
    void deleteByEntityTypeAndEntityIdAndCategory(
            String entityType, UUID entityId, FileCategory category);
}
