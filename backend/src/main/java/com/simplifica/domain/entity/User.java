package com.simplifica.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User entity representing a user account in the system.
 *
 * Users authenticate via OAuth2 providers (Google, Microsoft) and their
 * data is synchronized from the provider. Each user has a status
 * (PENDING/ACTIVE/INACTIVE) that controls their access. Roles are managed
 * per institution through user_institution_roles.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "picture_url", length = 512)
    private String pictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OAuth2Provider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserInstitution> institutions = new HashSet<>();

    /**
     * Sets timestamps before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the updatedAt timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the user account is in PENDING status.
     *
     * @return true if status is PENDING, false otherwise
     */
    public boolean isPending() {
        return this.status == UserStatus.PENDING;
    }

    /**
     * Checks if the user account is in ACTIVE status.
     *
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * Gets all active institutions the user is linked to.
     *
     * @return set of active institutions
     */
    public Set<Institution> getActiveInstitutions() {
        if (this.institutions == null) {
            return new HashSet<>();
        }
        return this.institutions.stream()
                .filter(UserInstitution::isActive)
                .map(UserInstitution::getInstitution)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the user belongs to a specific institution.
     *
     * @param institutionId the institution ID to check
     * @return true if the user has an active link to the institution, false otherwise
     */
    public boolean belongsToInstitution(UUID institutionId) {
        if (this.institutions == null || institutionId == null) {
            return false;
        }
        return this.institutions.stream()
                .filter(UserInstitution::isActive)
                .anyMatch(ui -> ui.getInstitution().getId().equals(institutionId));
    }

    /**
     * Gets the user's roles for a specific institution.
     *
     * @param institutionId the institution ID
     * @return set of institution roles, or empty set if user is not linked
     */
    public Set<InstitutionRole> getRolesForInstitution(UUID institutionId) {
        if (this.institutions == null || institutionId == null) {
            return new HashSet<>();
        }
        return this.institutions.stream()
                .filter(UserInstitution::isActive)
                .filter(ui -> ui.getInstitution().getId().equals(institutionId))
                .findFirst()
                .map(UserInstitution::getRoles)
                .orElse(new HashSet<>());
    }

    /**
     * Checks if the user has a specific role in an institution.
     *
     * @param institutionId the institution ID
     * @param role the role to check
     * @return true if the user has the role in the institution, false otherwise
     */
    public boolean hasRoleInInstitution(UUID institutionId, InstitutionRole role) {
        return getRolesForInstitution(institutionId).contains(role);
    }

    /**
     * Checks if the user is an admin of a specific institution.
     *
     * @param institutionId the institution ID
     * @return true if the user has ADMIN role in the institution, false otherwise
     */
    public boolean isInstitutionAdmin(UUID institutionId) {
        return hasRoleInInstitution(institutionId, InstitutionRole.ADMIN);
    }
}
