package com.simplifica.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User-Institution link entity representing a user's membership in an institution.
 *
 * This entity manages the many-to-many relationship between users and institutions,
 * storing additional information like roles and link status. A user can have
 * different roles in different institutions.
 */
@Entity
@Table(
    name = "user_institutions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "institution_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_institution_roles",
        joinColumns = @JoinColumn(name = "user_institution_id")
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<InstitutionRole> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "linked_at", nullable = false, updatable = false)
    private LocalDateTime linkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_by")
    private User linkedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets timestamps before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.linkedAt = LocalDateTime.now();
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
     * Checks if the link is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Checks if the user has a specific role in this institution.
     *
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(InstitutionRole role) {
        return this.roles != null && this.roles.contains(role);
    }

    /**
     * Adds a role to the user for this institution.
     *
     * @param role the role to add
     */
    public void addRole(InstitutionRole role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Removes a role from the user for this institution.
     *
     * @param role the role to remove
     */
    public void removeRole(InstitutionRole role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * Checks if the user is an admin of this institution.
     *
     * @return true if the user has the ADMIN role, false otherwise
     */
    public boolean isInstitutionAdmin() {
        return hasRole(InstitutionRole.ADMIN);
    }

    /**
     * Deactivates the user-institution link (soft delete).
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Reactivates the user-institution link.
     */
    public void reactivate() {
        this.active = true;
    }
}
