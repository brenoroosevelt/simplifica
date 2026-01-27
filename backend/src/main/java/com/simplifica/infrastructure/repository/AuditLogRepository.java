package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.AuditActionType;
import com.simplifica.domain.entity.AuditLog;
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
 * Repository for AuditLog entity.
 *
 * Provides data access methods for audit trail operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Finds all audit logs for a specific user action.
     *
     * @param userId the user who performed the action
     * @param pageable pagination parameters
     * @return paginated audit logs
     */
    Page<AuditLog> findByPerformedByUserId(UUID userId, Pageable pageable);

    /**
     * Finds all audit logs affecting a specific target user.
     *
     * @param targetUserId the affected user
     * @param pageable pagination parameters
     * @return paginated audit logs
     */
    Page<AuditLog> findByTargetUserId(UUID targetUserId, Pageable pageable);

    /**
     * Finds all audit logs for a specific action type.
     *
     * @param action the action type
     * @param pageable pagination parameters
     * @return paginated audit logs
     */
    Page<AuditLog> findByAction(AuditActionType action, Pageable pageable);

    /**
     * Finds audit logs within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @param pageable pagination parameters
     * @return paginated audit logs
     */
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    /**
     * Finds audit logs for a specific resource in a date range.
     *
     * @param targetUserId the affected user
     * @param action the action type
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of matching audit logs
     */
    @Query("SELECT al FROM AuditLog al " +
           "WHERE al.targetUserId = :targetUserId " +
           "AND al.action = :action " +
           "AND al.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findUserActionHistory(@Param("targetUserId") UUID targetUserId,
                                          @Param("action") AuditActionType action,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
