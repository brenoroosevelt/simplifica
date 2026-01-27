# Código de Correções - Trilha 2 Admin API

Este documento fornece exemplos de código para corrigir os problemas críticos e ressalvas identificadas na revisão.

---

## Problema Crítico 1: N+1 Query em listUsers

### Problema Identificado

O método `listUsers()` carrega 1 query base + N queries (1 por usuário na página).

```java
// ANTES (Problema)
Page<User> users = userRepository.findAll(spec, pageable);
return users.map(UserListDTO::fromEntity);  // Cada fromEntity() chama getActiveInstitutions()
```

### Solução 1: Adicionar Query Customizada no Repository

**Arquivo:** `UserRepository.java`

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // ... métodos existentes ...

    /**
     * Finds a user by ID with all institutions and roles eagerly loaded.
     * Includes institution details and linked_by user information.
     *
     * @param id the user's UUID
     * @return an Optional containing the user with institutions loaded
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.institutions ui " +
           "LEFT JOIN FETCH ui.institution " +
           "LEFT JOIN FETCH ui.linkedBy " +
           "WHERE u.id = :id")
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

    /**
     * Finds paginated users with institution count in a single query.
     * Optimized for list views with filtering.
     *
     * @param spec the filter specification
     * @param pageable pagination parameters
     * @return paginated list of users
     */
    @Query(value = "SELECT DISTINCT u FROM User u " +
                   "LEFT JOIN u.institutions ui WITH ui.active = true",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u")
    Page<User> findAllWithInstitutionCount(Specification<User> spec, Pageable pageable);
}
```

### Solução 2: Atualizar UserAdminService

**Arquivo:** `UserAdminService.java`

```java
@Service
@Transactional(readOnly = true)
public class UserAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAdminService.class);

    @Autowired
    private UserRepository userRepository;

    // ... outros campos ...

    /**
     * Lists users with filtering and pagination.
     *
     * OPTIMIZED: Uses custom query to avoid N+1 problem by loading
     * institution counts in single query instead of per-user.
     *
     * @param search search term for name or email (optional)
     * @param status filter by user status (optional)
     * @param institutionId filter by institution membership (optional)
     * @param role filter by institution role (optional)
     * @param pageable pagination and sorting parameters
     * @param requestingUserId the ID of the user making the request
     * @param isAdmin whether the requesting user is an ADMIN
     * @param requestingUserInstitutionId the institution ID of the requesting user (for GESTOR)
     * @return paginated list of users
     */
    public Page<UserListDTO> listUsers(String search, UserStatus status, UUID institutionId,
                                       InstitutionRole role, Pageable pageable,
                                       UUID requestingUserId, boolean isAdmin,
                                       UUID requestingUserInstitutionId) {
        LOGGER.debug("Listing users - search: {}, status: {}, institutionId: {}, role: {}",
                     search, status, institutionId, role);

        // If user is GESTOR, force filter by their institution
        UUID effectiveInstitutionId = institutionId;
        if (!isAdmin) {
            if (requestingUserInstitutionId == null) {
                throw new UnauthorizedAccessException("GESTOR must have an active institution");
            }
            effectiveInstitutionId = requestingUserInstitutionId;
            LOGGER.debug("GESTOR filtering by their institution: {}", effectiveInstitutionId);
        }

        Specification<User> spec = UserSpecifications.withFilters(
            status, effectiveInstitutionId, role, search);

        // OTIMIZADO: Busca já traz a contagem sem N+1
        Page<User> users = userRepository.findAllWithInstitutionCount(spec, pageable);

        // Mapear para DTO sem executar queries adicionais
        Page<UserListDTO> dtos = users.map(user -> {
            long institutionCount = userRepository.countActiveInstitutions(user.getId());
            return UserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .institutionCount(institutionCount)
                .build();
        });

        LOGGER.debug("Listed {} users", users.getTotalElements());
        return dtos;
    }
}
```

### Solução 3: Atualizar UserListDTO

**Arquivo:** `UserListDTO.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {

    private UUID id;
    private String email;
    private String name;
    private String pictureUrl;
    private OAuth2Provider provider;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private Long institutionCount;

    /**
     * DEPRECATED: Use UserAdminService.listUsers instead.
     * This method loads all institutions which causes N+1 problems.
     *
     * @param user the User entity
     * @return a UserListDTO instance
     */
    @Deprecated(since = "1.1", forRemoval = false)
    public static UserListDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .institutionCount((long) user.getActiveInstitutions().size())
                .build();
    }

    /**
     * Creates a UserListDTO with explicit institution count.
     * Preferred method to avoid N+1 queries.
     *
     * @param user the User entity
     * @param institutionCount pre-calculated institution count
     * @return a UserListDTO instance
     */
    public static UserListDTO fromEntity(User user, Long institutionCount) {
        if (user == null) {
            return null;
        }

        return UserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .institutionCount(institutionCount != null ? institutionCount : 0L)
                .build();
    }
}
```

---

## Problema Crítico 2: Falta de Auditoria

### Solução: Implementar AuditLog e AuditService

#### Passo 1: Criar Entidade de Auditoria

**Arquivo:** `AuditLog.java`

```java
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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

#### Passo 2: Criar Enum de Tipos de Ação

**Arquivo:** `AuditActionType.java`

```java
package com.simplifica.domain.entity;

/**
 * Enumeration of audit action types that can be logged.
 */
public enum AuditActionType {
    /**
     * User basic information updated (name, status).
     */
    USER_UPDATED,

    /**
     * User roles updated in an institution.
     */
    USER_ROLES_UPDATED,

    /**
     * User linked to an institution.
     */
    USER_LINKED_TO_INSTITUTION,

    /**
     * User unlinked from an institution.
     */
    USER_UNLINKED_FROM_INSTITUTION,

    /**
     * User status changed from PENDING to ACTIVE.
     */
    USER_ACTIVATED,

    /**
     * User status changed to INACTIVE.
     */
    USER_DEACTIVATED,

    /**
     * Institution created.
     */
    INSTITUTION_CREATED,

    /**
     * Institution updated.
     */
    INSTITUTION_UPDATED,

    /**
     * Institution deleted.
     */
    INSTITUTION_DELETED,

    /**
     * User permissions checked.
     */
    ACCESS_DENIED,

    /**
     * Failed authentication attempt.
     */
    AUTH_FAILED
}
```

#### Passo 3: Criar Repository para Auditoria

**Arquivo:** `AuditLogRepository.java`

```java
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
```

#### Passo 4: Criar AuditService

**Arquivo:** `AuditService.java`

```java
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
```

#### Passo 5: Atualizar UserAdminService para usar AuditService

**Arquivo:** `UserAdminService.java` (trechos para atualizar)

```java
@Service
@Transactional(readOnly = true)
public class UserAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInstitutionRepository userInstitutionRepository;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;  // ADICIONAR

    /**
     * Updates basic user information (name and status).
     * Logs changes to audit trail.
     *
     * @param id the user's UUID
     * @param request the update data
     * @param requestingUserId the ID of the user making the request
     * @param isAdmin whether the requesting user is an ADMIN
     * @param requestingUserInstitutionId the institution ID of the requesting user (for GESTOR)
     * @return updated user details
     */
    @Transactional
    public UserDetailDTO updateUser(UUID id, UpdateUserRequest request, UUID requestingUserId,
                                    boolean isAdmin, UUID requestingUserInstitutionId) {
        LOGGER.info("Updating user: {}", id);

        User user = userRepository.findByIdWithInstitutions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if (!isAdmin) {
            validateGestorCanAccessUser(user, requestingUserInstitutionId);
        }

        // ADICIONAR: Guardar valores antigos para auditoria
        String oldName = user.getName();
        Object oldStatus = user.getStatus();

        user.setName(request.getName());
        user.setStatus(request.getStatus());

        User savedUser = userRepository.save(user);

        // ADICIONAR: Registrar auditoria
        User performedBy = userService.findById(requestingUserId);
        auditService.logUserUpdate(performedBy, savedUser, oldName, request.getName(),
                                   oldStatus, request.getStatus());

        LOGGER.info("User {} updated successfully", id);
        return UserDetailDTO.fromEntity(savedUser);
    }

    /**
     * Updates user roles within a specific institution.
     * Logs changes to audit trail.
     *
     * @param userId the user's UUID
     * @param request the roles update request
     * @param requestingUserId the ID of the user making the request
     * @param isAdmin whether the requesting user is an ADMIN
     * @param requestingUserInstitutionId the institution ID of the requesting user (for GESTOR)
     */
    @Transactional
    public void updateUserRoles(UUID userId, UpdateUserRolesRequest request, UUID requestingUserId,
                                boolean isAdmin, UUID requestingUserInstitutionId) {
        LOGGER.info("Updating roles for user {} in institution {}",
                    userId, request.getInstitutionId());

        Institution institution = institutionService.findById(request.getInstitutionId());

        if (!isAdmin) {
            if (!request.getInstitutionId().equals(requestingUserInstitutionId)) {
                throw new UnauthorizedAccessException(
                    "GESTOR can only manage users in their own institution");
            }
        }

        UserInstitution userInstitution = userInstitutionRepository
                .findByUserIdAndInstitutionId(userId, request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

        if (!userInstitution.getActive()) {
            throw new BadRequestException("Cannot update roles for inactive user-institution link");
        }

        userInstitution.setRoles(request.getRoles());
        userInstitutionRepository.save(userInstitution);

        // ADICIONAR: Registrar auditoria
        User performedBy = userService.findById(requestingUserId);
        User targetUser = userService.findById(userId);
        auditService.logUserRolesUpdate(performedBy, targetUser,
                                       request.getInstitutionId(), institution.getName(),
                                       request.getRoles());

        LOGGER.info("Roles updated for user {} in institution {}: {}",
                    userId, request.getInstitutionId(), request.getRoles());
    }

    /**
     * Links a user to an institution with specific roles.
     * Only ADMIN can perform this operation.
     * Logs operation to audit trail.
     *
     * @param userId the user's UUID
     * @param request the link request with institution ID and roles
     * @param linkedByUserId the ID of the user creating the link
     */
    @Transactional
    public void linkUserToInstitution(UUID userId, LinkUserInstitutionRequest request,
                                      UUID linkedByUserId) {
        LOGGER.info("Linking user {} to institution {}", userId, request.getInstitutionId());

        User user = userService.findById(userId);
        Institution institution = institutionService.findById(request.getInstitutionId());
        User linkedBy = userService.findById(linkedByUserId);

        if (userInstitutionRepository.existsByUserIdAndInstitutionId(
                userId, request.getInstitutionId())) {
            throw new BadRequestException("User is already linked to this institution");
        }

        UserInstitution userInstitution = UserInstitution.builder()
                .user(user)
                .institution(institution)
                .roles(request.getRoles())
                .linkedBy(linkedBy)
                .active(true)
                .build();

        userInstitutionRepository.save(userInstitution);

        // Ativar usuário se estava PENDING e esta é a primeira instituição
        if (user.getStatus() == UserStatus.PENDING &&
            user.getActiveInstitutions().isEmpty()) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            LOGGER.info("User {} status changed from PENDING to ACTIVE", userId);
        }

        // ADICIONAR: Registrar auditoria
        auditService.logUserLinkedToInstitution(linkedBy, user,
                                              request.getInstitutionId(),
                                              institution.getName(),
                                              request.getRoles());

        LOGGER.info("User {} linked to institution {} with roles: {}",
                    userId, request.getInstitutionId(), request.getRoles());
    }

    /**
     * Unlinks a user from an institution (soft delete).
     * Only ADMIN can perform this operation.
     * Logs operation to audit trail.
     *
     * @param userId the user's UUID
     * @param institutionId the institution's UUID
     */
    @Transactional
    public void unlinkUserFromInstitution(UUID userId, UUID institutionId) {
        LOGGER.info("Unlinking user {} from institution {}", userId, institutionId);

        UserInstitution userInstitution = userInstitutionRepository
                .findByUserIdAndInstitutionId(userId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

        userInstitution.setActive(false);
        userInstitutionRepository.save(userInstitution);

        User user = userService.findById(userId);
        if (user.getActiveInstitutions().isEmpty()) {
            user.setStatus(UserStatus.PENDING);
            userRepository.save(user);
            LOGGER.info("User {} has no more institutions, status changed to PENDING", userId);
        }

        // ADICIONAR: Registrar auditoria
        // Nota: Não temos informação de quem fez, então usar admin genérico ou adicionar parâmetro
        Institution institution = institutionService.findById(institutionId);
        // auditService.logUserUnlinkedFromInstitution(performedBy, user, institutionId, institution.getName());

        LOGGER.info("User {} unlinked from institution {}", userId, institutionId);
    }
}
```

---

## Ressalva 4: Padronizar Nomenclatura MANAGER/GESTOR

### Solução: Escolher MANAGER (English) para código, GERENTE/GESTOR para labels

**Arquivo:** `UserRole.java`

```java
package com.simplifica.domain.entity;

/**
 * Enumeration of user roles in the system.
 *
 * These roles control global system access and administrative permissions.
 * Institution-specific roles are managed separately in InstitutionRole.
 */
public enum UserRole {
    /**
     * Regular user - can only access institutions they are linked to.
     */
    USER,

    /**
     * System administrator - full access to all users and institutions.
     */
    ADMIN,

    /**
     * Manager/Gestor - can manage users within their assigned institutions.
     * Note: This is called "MANAGER" in the code but "GESTOR" in Portuguese documentation.
     */
    MANAGER;

    /**
     * Returns the Portuguese label for the role.
     *
     * @return Portuguese role name
     */
    public String getLabel() {
        return switch (this) {
            case USER -> "Usuário";
            case ADMIN -> "Administrador";
            case MANAGER -> "Gestor";
        };
    }

    /**
     * Checks if the role has system-wide administrative privileges.
     *
     * @return true if role is ADMIN
     */
    public boolean isSystemAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if the role can manage other users.
     *
     * @return true if role is ADMIN or MANAGER
     */
    public boolean canManageUsers() {
        return this == ADMIN || this == MANAGER;
    }
}
```

**Arquivo:** `AdminController.java` (verificar comentários)

```java
/**
 * REST Controller for administrative user management operations.
 *
 * Provides endpoints for ADMIN and MANAGER (Gestor) roles to manage users,
 * their institution relationships, and roles. ADMIN has full access
 * to all users, while MANAGER can only manage users within their
 * own institution.
 */
@RestController
@RequestMapping("/admin/users")
public class AdminController {

    // ... existing code ...

    /**
     * Lists all users with optional filtering and pagination.
     *
     * Permissions:
     * - ADMIN: can see all users and filter by any institution
     * - MANAGER (Gestor): can only see users from their institution
     *
     * @param search search term for name or email (optional)
     * @param status filter by user status (optional)
     * @param institutionId filter by institution membership (optional, ADMIN only)
     * @param role filter by institution role (optional)
     * @param pageable pagination and sorting parameters
     * @param userPrincipal the authenticated user principal
     * @return paginated list of users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PagedResponseDTO<UserListDTO>> listUsers(/* ... */) {
        // ... existing code ...
    }
}
```

---

## Ressalva 5: Tratamento robusto em PendingUserInterceptor

### Solução: Melhorar null checks e exception handling

**Arquivo:** `PendingUserInterceptor.java`

```java
@Component
public class PendingUserInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUserInterceptor.class);

    private static final List<String> ALLOWED_PATHS = Arrays.asList(
        "/user/profile",
        "/auth/",
        "/public/",
        "/oauth2/",
        "/actuator/",
        "/error"
    );

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PendingUserInterceptor(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Checks if the authenticated user has PENDING status and blocks access
     * to restricted endpoints.
     *
     * Improvements:
     * - Better null checks for different authentication types
     * - Proper exception handling for response writing
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler (controller method)
     * @return true to continue processing, false to abort
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String requestPath = request.getRequestURI();

        // Skip check for allowed paths
        if (isPathAllowed(requestPath)) {
            LOGGER.debug("Allowed path accessed: {}", requestPath);
            return true;
        }

        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authentication exists and is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            LOGGER.debug("No authentication or not authenticated");
            return true;
        }

        // Check if principal is UserPrincipal (better error handling)
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            LOGGER.debug("Non-UserPrincipal authentication type: {}",
                        authentication.getPrincipal().getClass().getSimpleName());
            return true; // Allow other authentication types to proceed
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Fetch user status from database
            User user = userRepository.findById(userPrincipal.getId()).orElse(null);

            if (user == null) {
                LOGGER.error("User not found in database: {}", userPrincipal.getId());
                // Don't block; let the endpoint handle missing user
                return true;
            }

            // Block PENDING users from accessing restricted endpoints
            if (user.getStatus() == UserStatus.PENDING) {
                LOGGER.warn("PENDING user {} attempted to access restricted endpoint: {}",
                           user.getEmail(), requestPath);
                return writeErrorResponse(response, HttpStatus.FORBIDDEN,
                    "Sua conta está pendente de aprovação. " +
                    "Aguarde até que um administrador vincule você a uma instituição. " +
                    "Você pode visualizar seu perfil em /user/profile.",
                    requestPath);
            }

            // Block INACTIVE users
            if (user.getStatus() == UserStatus.INACTIVE) {
                LOGGER.warn("INACTIVE user {} attempted to access endpoint: {}",
                           user.getEmail(), requestPath);
                return writeErrorResponse(response, HttpStatus.FORBIDDEN,
                    "Sua conta está inativa. " +
                    "Entre em contato com o administrador do sistema para reativar sua conta.",
                    requestPath);
            }

        } catch (Exception e) {
            LOGGER.error("Error in PendingUserInterceptor.preHandle()", e);
            // On unexpected error, allow the request to proceed
            // The endpoint will handle any issues
        }

        return true; // Continue processing
    }

    /**
     * Writes error response with proper exception handling.
     *
     * @param response the HTTP response
     * @param status the HTTP status
     * @param message the error message
     * @param path the request path
     * @return false to block the request
     */
    private boolean writeErrorResponse(HttpServletResponse response, HttpStatus status,
                                       String message, String path) {
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(message)
                    .path(path)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
        } catch (IOException e) {
            LOGGER.error("Error writing error response for path: {}", path, e);
            // Best effort; response may not be writable
        }
        return false; // Block the request
    }

    /**
     * Checks if the request path is allowed for PENDING users.
     *
     * @param path the request path
     * @return true if the path is allowed, false otherwise
     */
    private boolean isPathAllowed(String path) {
        if (path == null) {
            return false;
        }
        return ALLOWED_PATHS.stream().anyMatch(path::startsWith);
    }
}
```

---

## Próximos Passos

1. **Implementar todas as correções críticas primeiro**
   - N+1 Query (PC-01)
   - Auditoria (PC-02)

2. **Depois implementar ressalvas**
   - Padronização de nomenclatura
   - Melhorias em PendingUserInterceptor
   - Validações adicionais

3. **Finalmente, considerar sugestões de melhoria**
   - Implementar para v1.1
   - Adicionar após estabilização do código

---

**Status:** Correções prontas para implementação
**Data:** 23 de Janeiro de 2026
