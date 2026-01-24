package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.UserInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserInstitution entity operations.
 *
 * Manages the many-to-many relationship between users and institutions,
 * including role assignments and link status.
 */
@Repository
public interface UserInstitutionRepository extends JpaRepository<UserInstitution, UUID> {

    /**
     * Finds all user-institution links for a specific user.
     *
     * @param userId the user ID
     * @return list of user-institution links
     */
    List<UserInstitution> findByUserId(UUID userId);

    /**
     * Finds all user-institution links for a specific institution.
     *
     * @param institutionId the institution ID
     * @return list of user-institution links
     */
    List<UserInstitution> findByInstitutionId(UUID institutionId);

    /**
     * Finds all active user-institution links for a specific user.
     *
     * @param userId the user ID
     * @return list of active user-institution links
     */
    List<UserInstitution> findByUserIdAndActiveTrue(UUID userId);

    /**
     * Finds all active user-institution links for a specific institution.
     *
     * @param institutionId the institution ID
     * @return list of active user-institution links
     */
    List<UserInstitution> findByInstitutionIdAndActiveTrue(UUID institutionId);

    /**
     * Finds a user-institution link by user and institution.
     *
     * @param userId the user ID
     * @param institutionId the institution ID
     * @return an Optional containing the link if found
     */
    Optional<UserInstitution> findByUserIdAndInstitutionId(UUID userId, UUID institutionId);

    /**
     * Finds an active user-institution link by user and institution.
     *
     * Uses JPQL query with explicit JOIN FETCH to avoid N+1 queries
     * when accessing related entities.
     *
     * @param userId the user ID
     * @param institutionId the institution ID
     * @return an Optional containing the active link if found
     */
    @Query("SELECT ui FROM UserInstitution ui "
            + "WHERE ui.user.id = :userId "
            + "AND ui.institution.id = :institutionId "
            + "AND ui.active = true")
    Optional<UserInstitution> findActiveByUserAndInstitution(
            @Param("userId") UUID userId,
            @Param("institutionId") UUID institutionId
    );

    /**
     * Checks if a user-institution link exists.
     *
     * @param userId the user ID
     * @param institutionId the institution ID
     * @return true if a link exists (active or inactive), false otherwise
     */
    boolean existsByUserIdAndInstitutionId(UUID userId, UUID institutionId);

    /**
     * Counts active users in an institution.
     *
     * @param institutionId the institution ID
     * @return the number of active user links
     */
    @Query("SELECT COUNT(ui) FROM UserInstitution ui "
            + "WHERE ui.institution.id = :institutionId "
            + "AND ui.active = true")
    long countActiveUsersByInstitution(@Param("institutionId") UUID institutionId);

    /**
     * Finds all active users for an institution with eager loading.
     *
     * Uses JOIN FETCH to eagerly load user and institution entities,
     * avoiding N+1 query problems. Useful for displaying user lists.
     *
     * @param institutionId the institution ID
     * @return list of active user-institution links with loaded entities
     */
    @Query("SELECT ui FROM UserInstitution ui "
            + "JOIN FETCH ui.user "
            + "JOIN FETCH ui.institution "
            + "WHERE ui.institution.id = :institutionId "
            + "AND ui.active = true")
    List<UserInstitution> findActiveUsersWithInstitution(@Param("institutionId") UUID institutionId);

    /**
     * Finds all active institutions for a user with eager loading.
     *
     * Uses JOIN FETCH to eagerly load user and institution entities,
     * avoiding N+1 query problems. Useful for displaying institution lists.
     *
     * @param userId the user ID
     * @return list of active user-institution links with loaded entities
     */
    @Query("SELECT ui FROM UserInstitution ui "
            + "JOIN FETCH ui.user "
            + "JOIN FETCH ui.institution "
            + "WHERE ui.user.id = :userId "
            + "AND ui.active = true")
    List<UserInstitution> findActiveInstitutionsWithUser(@Param("userId") UUID userId);
}
