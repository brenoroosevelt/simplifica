package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 *
 * Provides data access methods for user-related operations including
 * finding users by email, OAuth2 provider credentials, and checking existence.
 * Supports complex queries through JpaSpecificationExecutor for filtering
 * and searching users by multiple criteria.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email address
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their OAuth2 provider and provider ID.
     *
     * @param provider the OAuth2 provider (GOOGLE, MICROSOFT)
     * @param providerId the unique identifier from the OAuth2 provider
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByProviderAndProviderId(OAuth2Provider provider, String providerId);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by ID with all institutions eagerly loaded.
     * Loads all user-institution relationships (active and inactive).
     * The DTO layer filters for active institutions only.
     *
     * @param id the user's UUID
     * @return an Optional containing the user with all institutions loaded
     */
    @Query("SELECT DISTINCT u FROM User u "
         + "LEFT JOIN FETCH u.institutions ui "
         + "LEFT JOIN FETCH ui.institution "
         + "LEFT JOIN FETCH ui.linkedBy "
         + "WHERE u.id = :id")
    Optional<User> findByIdWithInstitutions(@Param("id") UUID id);

    /**
     * Counts active institutions for a user without loading all associations.
     * Used for listing views to avoid memory overhead.
     *
     * @param userId the user's UUID
     * @return count of active institutions
     */
    @Query("SELECT COUNT(ui) FROM UserInstitution ui " +
           "WHERE ui.user.id = :userId AND ui.active = true")
    long countActiveInstitutions(@Param("userId") UUID userId);

    // Removed custom @Query - using standard JpaSpecificationExecutor.findAll() instead
    // The findAll(Specification, Pageable) method from JpaSpecificationExecutor
    // correctly applies Specification filters for multi-tenant isolation
}
