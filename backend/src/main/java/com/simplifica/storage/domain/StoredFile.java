package com.simplifica.storage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing metadata for a stored file.
 *
 * This entity stores metadata about files managed by Spring Content,
 * including their location, size, content type, and associated entity.
 * The actual file content is managed by Spring Content stores.
 *
 * The storageKey field contains either:
 * - For filesystem: relative path from storage root
 * - For S3/cloud: the object key/path in the bucket
 */
@Entity
@Table(name = "stored_files", indexes = {
        @Index(name = "idx_stored_files_entity", columnList = "entity_type,entity_id"),
        @Index(name = "idx_stored_files_category", columnList = "category"),
        @Index(name = "idx_stored_files_uploaded_at", columnList = "uploaded_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The type of entity this file belongs to (e.g., "ValueChain", "Institution")
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * The ID of the entity this file belongs to
     */
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /**
     * Category of this file
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private FileCategory category = FileCategory.OTHER;

    /**
     * Original filename as uploaded by the user
     */
    @Column(nullable = false, length = 512)
    private String filename;

    /**
     * MIME type of the file (detected, not from extension)
     */
    @Column(name = "content_type", nullable = false, length = 127)
    private String contentType;

    /**
     * File size in bytes
     */
    @Column(nullable = false)
    private Long size;

    /**
     * Storage key - path or key where file is stored
     * For filesystem: relative path from root
     * For S3/cloud: object key
     */
    @Column(name = "storage_key", nullable = false, length = 1024)
    private String storageKey;

    /**
     * ETag/hash for caching and integrity verification
     * Typically MD5 hash of file content
     */
    @Column(length = 64)
    private String etag;

    /**
     * Storage backend type used for this file
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    @Builder.Default
    private StorageType storageType = StorageType.FILESYSTEM;

    /**
     * Timestamp when file was uploaded
     */
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * User who uploaded the file (optional)
     */
    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
        if (this.category == null) {
            this.category = FileCategory.OTHER;
        }
        if (this.storageType == null) {
            this.storageType = StorageType.FILESYSTEM;
        }
    }
}
