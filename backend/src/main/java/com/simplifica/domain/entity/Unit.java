package com.simplifica.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unit entity representing an organizational unit.
 *
 * Each unit belongs to a specific institution (tenant) and represents
 * a division, department, sector, or any organizational structure.
 * Supports soft delete via the active flag.
 */
@Entity
@Table(
    name = "units",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_units_institution_acronym",
        columnNames = {"institution_id", "acronym"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String acronym;

    @Column(columnDefinition = "TEXT")
    private String description;

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
        // Normalize acronym to uppercase
        if (this.acronym != null) {
            this.acronym = this.acronym.toUpperCase().trim();
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
     * Checks if the unit is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Normalizes acronym to uppercase.
     * Used before setting the acronym field.
     *
     * @param acronym the acronym to normalize
     */
    public void setAcronym(String acronym) {
        this.acronym = acronym != null ? acronym.toUpperCase().trim() : null;
    }
}
