package com.simplifica.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Institution entity representing a tenant in the multi-tenant system.
 *
 * Each institution (university, government agency, etc.) is a separate tenant
 * with isolated data. Users can be linked to one or more institutions with
 * specific roles per institution.
 */
@Entity
@Table(name = "institutions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private String acronym;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @Column(name = "logo_thumbnail_url", length = 1024)
    private String logoThumbnailUrl;

    @Column(name = "logo_uploaded_at")
    private LocalDateTime logoUploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InstitutionType type;

    @Column(unique = true, length = 255)
    private String domain;

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
     * Checks if the institution is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Sets the logo URL and updates the upload timestamp.
     *
     * @param logoUrl the URL of the uploaded logo
     * @param thumbnailUrl the URL of the thumbnail version
     */
    public void setLogoUrls(String logoUrl, String thumbnailUrl) {
        this.logoUrl = logoUrl;
        this.logoThumbnailUrl = thumbnailUrl;
        this.logoUploadedAt = LocalDateTime.now();
    }

    /**
     * Clears logo information when a logo is deleted.
     */
    public void clearLogo() {
        this.logoUrl = null;
        this.logoThumbnailUrl = null;
        this.logoUploadedAt = null;
    }
}
