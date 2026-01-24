package com.simplifica.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Security audit log entity for tracking security-related events.
 *
 * This entity records all security-relevant actions in the system, including
 * access attempts, permission checks, and resource access. It provides an
 * audit trail for compliance and security monitoring.
 */
@Entity
@Table(name = "security_audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "institution_id")
    private UUID institutionId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditResult result;

    /**
     * Sets the timestamp before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * Enum representing the result of an audited action.
     */
    public enum AuditResult {
        SUCCESS,
        FAILURE,
        DENIED
    }

    /**
     * Builder helper method to create a successful audit log entry.
     *
     * @param userId the ID of the user performing the action
     * @param institutionId the ID of the institution (can be null)
     * @param action the action performed
     * @param resource the resource type accessed
     * @param resourceId the ID of the resource accessed
     * @return a SecurityAuditLog builder
     */
    public static SecurityAuditLogBuilder success(
            UUID userId,
            UUID institutionId,
            String action,
            String resource,
            UUID resourceId
    ) {
        return SecurityAuditLog.builder()
                .userId(userId)
                .institutionId(institutionId)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(AuditResult.SUCCESS);
    }

    /**
     * Builder helper method to create a denied access audit log entry.
     *
     * @param userId the ID of the user attempting the action
     * @param institutionId the ID of the institution (can be null)
     * @param action the action attempted
     * @param resource the resource type
     * @param resourceId the ID of the resource
     * @param reason the reason for denial
     * @return a SecurityAuditLog builder
     */
    public static SecurityAuditLogBuilder denied(
            UUID userId,
            UUID institutionId,
            String action,
            String resource,
            UUID resourceId,
            String reason
    ) {
        return SecurityAuditLog.builder()
                .userId(userId)
                .institutionId(institutionId)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(AuditResult.DENIED)
                .details(reason);
    }

    /**
     * Builder helper method to create a failed action audit log entry.
     *
     * @param userId the ID of the user performing the action
     * @param institutionId the ID of the institution (can be null)
     * @param action the action attempted
     * @param resource the resource type
     * @param errorMessage the error message
     * @return a SecurityAuditLog builder
     */
    public static SecurityAuditLogBuilder failure(
            UUID userId,
            UUID institutionId,
            String action,
            String resource,
            String errorMessage
    ) {
        return SecurityAuditLog.builder()
                .userId(userId)
                .institutionId(institutionId)
                .action(action)
                .resource(resource)
                .result(AuditResult.FAILURE)
                .details(errorMessage);
    }
}
