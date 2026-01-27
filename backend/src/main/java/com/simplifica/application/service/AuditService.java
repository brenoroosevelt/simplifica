package com.simplifica.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifica.domain.entity.AuditActionType;
import com.simplifica.domain.entity.AuditLog;
import com.simplifica.domain.entity.User;
import com.simplifica.infrastructure.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for audit logging operations.
 *
 * Centralizes audit trail recording for all administrative operations.
 * Provides a convenient API for logging various action types.
 */
@Service
@Transactional
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Logs a user update operation.
     *
     * @param performedBy the user performing the action
     * @param targetUser the user being updated
     * @param oldName previous name
     * @param newName new name
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void logUserUpdate(User performedBy, User targetUser,
                             String oldName, String newName,
                             Object oldStatus, Object newStatus) {
        Map<String, Object> changes = new HashMap<>();
        if (!oldName.equals(newName)) {
            changes.put("name", Map.of("old", oldName, "new", newName));
        }
        if (!oldStatus.equals(newStatus)) {
            changes.put("status", Map.of("old", oldStatus, "new", newStatus));
        }

        logAuditAction(
            performedBy,
            targetUser,
            null, // no institution
            AuditActionType.USER_UPDATED,
            changes,
            String.format("User %s updated: %s", targetUser.getEmail(), changes.keySet())
        );
    }

    /**
     * Logs a user roles update operation.
     *
     * @param performedBy the user performing the action
     * @param targetUser the user whose roles are being changed
     * @param institutionId the institution where roles changed
     * @param institutionName the name of the institution
     * @param newRoles the roles assigned
     */
    public void logUserRolesUpdate(User performedBy, User targetUser,
                                   UUID institutionId, String institutionName,
                                   Object newRoles) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("roles", newRoles);
        changes.put("institution", institutionName);

        logAuditAction(
            performedBy,
            targetUser,
            institutionId,
            AuditActionType.USER_ROLES_UPDATED,
            changes,
            String.format("Roles updated for %s in %s: %s",
                         targetUser.getEmail(), institutionName, newRoles)
        );
    }

    /**
     * Logs a user-institution link creation.
     *
     * @param performedBy the user performing the action
     * @param targetUser the user being linked
     * @param institutionId the institution ID
     * @param institutionName the institution name
     * @param roles the roles granted
     */
    public void logUserLinkedToInstitution(User performedBy, User targetUser,
                                          UUID institutionId, String institutionName,
                                          Object roles) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("action", "LINKED");
        changes.put("roles", roles);
        changes.put("linkedBy", performedBy.getEmail());

        logAuditAction(
            performedBy,
            targetUser,
            institutionId,
            AuditActionType.USER_LINKED_TO_INSTITUTION,
            changes,
            String.format("%s linked to %s with roles: %s",
                         targetUser.getEmail(), institutionName, roles)
        );
    }

    /**
     * Logs a user-institution unlink.
     *
     * @param performedBy the user performing the action
     * @param targetUser the user being unlinked
     * @param institutionId the institution ID
     * @param institutionName the institution name
     */
    public void logUserUnlinkedFromInstitution(User performedBy, User targetUser,
                                              UUID institutionId, String institutionName) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("action", "UNLINKED");
        changes.put("unlinkedBy", performedBy.getEmail());

        logAuditAction(
            performedBy,
            targetUser,
            institutionId,
            AuditActionType.USER_UNLINKED_FROM_INSTITUTION,
            changes,
            String.format("%s unlinked from %s", targetUser.getEmail(), institutionName)
        );
    }

    /**
     * Logs an automatic user-institution link by email domain.
     *
     * @param targetUser the user being linked
     * @param institutionId the institution ID
     * @param institutionName the institution name
     * @param domain the email domain that triggered the link
     * @param roles the roles granted
     */
    public void logUserAutoLinkedByDomain(User targetUser,
                                          UUID institutionId, String institutionName,
                                          String domain, Object roles) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("action", "AUTO_LINKED");
        changes.put("domain", domain);
        changes.put("roles", roles);
        changes.put("institution", institutionName);

        logAuditAction(
            targetUser, // performed by the user themselves (OAuth login)
            targetUser,
            institutionId,
            AuditActionType.USER_AUTO_LINKED_BY_DOMAIN,
            changes,
            String.format("%s auto-linked to %s via domain %s with roles: %s",
                         targetUser.getEmail(), institutionName, domain, roles)
        );
    }

    /**
     * Logs an access denied event.
     *
     * @param userId the user who was denied access
     * @param userEmail the user's email
     * @param reason reason for denial
     * @param requestPath the attempted request path
     */
    public void logAccessDenied(UUID userId, String userEmail, String reason, String requestPath) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("reason", reason);

        AuditLog auditLog = AuditLog.builder()
            .performedByUserId(userId)
            .performedByEmail(userEmail)
            .action(AuditActionType.ACCESS_DENIED)
            .description(String.format("Access denied: %s - Path: %s", reason, requestPath))
            .requestPath(requestPath)
            .changesJson(toJson(changes))
            .build();

        auditLogRepository.save(auditLog);
        LOGGER.warn("Access denied for user {}: {} on path {}", userEmail, reason, requestPath);
    }

    /**
     * Generic method for logging audit actions.
     *
     * @param performedBy the user performing the action
     * @param targetUser the user affected (optional)
     * @param institutionId the institution affected (optional)
     * @param action the type of action
     * @param changes map of changes made
     * @param description human-readable description
     */
    private void logAuditAction(User performedBy, User targetUser, UUID institutionId,
                               AuditActionType action, Map<String, Object> changes,
                               String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .performedByUserId(performedBy.getId())
                .performedByEmail(performedBy.getEmail())
                .targetUserId(targetUser != null ? targetUser.getId() : null)
                .targetUserEmail(targetUser != null ? targetUser.getEmail() : null)
                .targetInstitutionId(institutionId)
                .action(action)
                .description(description)
                .changesJson(toJson(changes))
                .build();

            auditLogRepository.save(auditLog);
            LOGGER.info("Audit log created: {} by {} on {}", action, performedBy.getEmail(),
                       targetUser != null ? targetUser.getEmail() : institutionId);
        } catch (Exception e) {
            LOGGER.error("Failed to create audit log for action: {}", action, e);
            // Don't throw exception; audit failure shouldn't block operations
        }
    }

    /**
     * Converts object to JSON string for storage.
     *
     * @param obj object to convert
     * @return JSON string representation
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.error("Error serializing changes to JSON", e);
            return "{}";
        }
    }
}
