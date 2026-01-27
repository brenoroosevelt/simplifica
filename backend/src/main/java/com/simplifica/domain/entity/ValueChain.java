package com.simplifica.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ValueChain entity representing a value chain (cadeia de valor) within an institution.
 *
 * Each value chain belongs to a specific institution (multi-tenant) and can have
 * an associated image. Value chains represent different economic activities or
 * production chains that institutions manage (e.g., "Agricultura Familiar", "Turismo Rural").
 */
@Entity
@Table(name = "value_chains")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueChain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "image_thumbnail_url", length = 1024)
    private String imageThumbnailUrl;

    @Column(name = "image_uploaded_at")
    private LocalDateTime imageUploadedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets timestamps before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }

    /**
     * Updates the updatedAt timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the value chain is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Sets the image URLs and updates the upload timestamp.
     *
     * @param imageUrl the URL of the uploaded image
     * @param thumbnailUrl the URL of the thumbnail version
     */
    public void setImageUrls(String imageUrl, String thumbnailUrl) {
        this.imageUrl = imageUrl;
        this.imageThumbnailUrl = thumbnailUrl;
        this.imageUploadedAt = LocalDateTime.now();
    }

    /**
     * Clears image information when an image is deleted.
     */
    public void clearImage() {
        this.imageUrl = null;
        this.imageThumbnailUrl = null;
        this.imageUploadedAt = null;
    }
}
