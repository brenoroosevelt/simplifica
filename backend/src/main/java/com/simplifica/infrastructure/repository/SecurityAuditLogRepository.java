package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SecurityAuditLog entity operations.
 *
 * Provides access to security audit logs for compliance, monitoring,
 * and security analysis purposes.
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, UUID> {

    /**
     * Finds audit logs for a specific user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return a page of audit logs for the user
     */
    Page<SecurityAuditLog> findByUserId(UUID userId, Pageable pageable);

    /**
     * Finds audit logs for a specific institution with pagination.
     *
     * @param institutionId the institution ID
     * @param pageable pagination information
     * @return a page of audit logs for the institution
     */
    Page<SecurityAuditLog> findByInstitutionId(UUID institutionId, Pageable pageable);

    /**
     * Finds audit logs for a specific action with pagination.
     *
     * @param action the action name
     * @param pageable pagination information
     * @return a page of audit logs for the action
     */
    Page<SecurityAuditLog> findByAction(String action, Pageable pageable);

    /**
     * Finds audit logs for a specific result type with pagination.
     *
     * @param result the result type (SUCCESS, FAILURE, DENIED)
     * @param pageable pagination information
     * @return a page of audit logs with the specified result
     */
    Page<SecurityAuditLog> findByResult(SecurityAuditLog.AuditResult result, Pageable pageable);

    /**
     * Finds audit logs within a time range.
     *
     * Useful for generating reports or analyzing security events
     * during a specific period.
     *
     * @param startDate the start of the time range (inclusive)
     * @param endDate the end of the time range (inclusive)
     * @return list of audit logs in the time range
     */
    @Query("SELECT sal FROM SecurityAuditLog sal "
            + "WHERE sal.timestamp BETWEEN :startDate AND :endDate "
            + "ORDER BY sal.timestamp DESC")
    List<SecurityAuditLog> findByTimestampBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds audit logs for a specific user and institution with pagination.
     *
     * @param userId the user ID
     * @param institutionId the institution ID
     * @param pageable pagination information
     * @return a page of audit logs matching the criteria
     */
    Page<SecurityAuditLog> findByUserIdAndInstitutionId(
            UUID userId,
            UUID institutionId,
            Pageable pageable
    );

    /**
     * Finds recent denied access attempts for security monitoring.
     *
     * This query helps identify potential security threats by finding
     * recent access denial events.
     *
     * @param sinceDate the date to search from
     * @param pageable pagination information
     * @return a page of denied access audit logs
     */
    @Query("SELECT sal FROM SecurityAuditLog sal "
            + "WHERE sal.result = 'DENIED' "
            + "AND sal.timestamp >= :sinceDate "
            + "ORDER BY sal.timestamp DESC")
    Page<SecurityAuditLog> findRecentDeniedAccess(
            @Param("sinceDate") LocalDateTime sinceDate,
            Pageable pageable
    );

    /**
     * Counts failed access attempts for a user within a time period.
     *
     * Useful for detecting brute force attacks or suspicious behavior.
     *
     * @param userId the user ID
     * @param sinceDate the date to count from
     * @return the number of failed/denied attempts
     */
    @Query("SELECT COUNT(sal) FROM SecurityAuditLog sal "
            + "WHERE sal.userId = :userId "
            + "AND sal.result IN ('FAILURE', 'DENIED') "
            + "AND sal.timestamp >= :sinceDate")
    long countFailedAttemptsForUser(
            @Param("userId") UUID userId,
            @Param("sinceDate") LocalDateTime sinceDate
    );
}
