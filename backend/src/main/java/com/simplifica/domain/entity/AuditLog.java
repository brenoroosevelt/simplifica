package com.simplifica.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log entity for tracking administrative operations.
 *
 * Stores information about who performed what action, when, and on what resources.
 * Used for compliance, investigation, and accountability.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * UUID of the user who performed the action (ADMIN or MANAGER).
     */
    @Column(nullable = false)
    private UUID performedByUserId;

    /**
     * Email of the user who performed the action (for audit trail readability).
     */
    @Column(nullable = false, length = 255)
    private String performedByEmail;

    /**
     * Type of action performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditActionType action;

    /**
     * UUID of the user being affected by the action (if applicable).
     */
    @Column(name = "target_user_id")
    private UUID targetUserId;

    /**
     * Email of the target user (for audit trail readability).
     */
    @Column(length = 255)
    private String targetUserEmail;

    /**
     * UUID of the institution being affected by the action (if applicable).
     */
    @Column(name = "target_institution_id")
    private UUID targetInstitutionId;

    /**
     * JSON representation of the changes made.
     * Example: {"oldName": "John", "newName": "Johnny", "oldStatus": "PENDING", "newStatus": "ACTIVE"}
     */
    @Column(columnDefinition = "TEXT")
    private String changesJson;

    /**
     * Description of the action for human-readable logs.
     */
    @Column(length = 500)
    private String description;

    /**
     * HTTP status code returned (for API calls).
     */
    @Column(name = "http_status")
    private Integer httpStatus;

    /**
     * Request path (for context).
     */
    @Column(length = 500)
    private String requestPath;

    /**
     * Timestamp when the action was performed.
     */
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
