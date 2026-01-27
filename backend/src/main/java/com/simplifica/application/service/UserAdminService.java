package com.simplifica.application.service;

import com.simplifica.application.dto.LinkUserInstitutionRequest;
import com.simplifica.application.dto.UpdateUserRequest;
import com.simplifica.application.dto.UpdateUserRolesRequest;
import com.simplifica.application.dto.UserDetailDTO;
import com.simplifica.application.dto.UserInstitutionSummaryDTO;
import com.simplifica.application.dto.UserListDTO;
import com.simplifica.domain.constants.InstitutionConstants;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import com.simplifica.infrastructure.repository.UserRepository;
import com.simplifica.infrastructure.repository.UserSpecifications;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for administrative user management operations.
 *
 * Provides functionality for listing, updating, and managing users
 * and their institution relationships. Implements permission checks
 * to ensure ADMIN can manage all users while GESTOR can only manage
 * users within their own institution.
 */
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
    private AuditService auditService;

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

        // Use standard Spring Data JPA method that correctly applies Specification
        Page<User> users = userRepository.findAll(spec, pageable);

        // Mapear para DTO sem executar queries adicionais
        Page<UserListDTO> dtos = users.map(user -> {
            long institutionCount = userRepository.countActiveInstitutions(user.getId());

            // Mapear instituições do usuário para exibição na listagem (apenas para admin)
            List<UserInstitutionSummaryDTO> institutions = user.getInstitutions().stream()
                .filter(UserInstitution::getActive)
                .map(ui -> UserInstitutionSummaryDTO.builder()
                    .institutionId(ui.getInstitution().getId())
                    .institutionName(ui.getInstitution().getName())
                    .institutionAcronym(ui.getInstitution().getAcronym())
                    .build())
                .collect(Collectors.toList());

            return UserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .institutionCount(institutionCount)
                .institutions(institutions)
                .build();
        });

        LOGGER.debug("Listed {} users", users.getTotalElements());
        return dtos;
    }

    /**
     * Gets detailed information about a specific user.
     *
     * @param id the user's UUID
     * @param requestingUserId the ID of the user making the request
     * @param isAdmin whether the requesting user is an ADMIN
     * @param requestingUserInstitutionId the institution ID of the requesting user (for GESTOR)
     * @return detailed user information with institutions and roles
     */
    public UserDetailDTO getUserById(UUID id, UUID requestingUserId, boolean isAdmin,
                                     UUID requestingUserInstitutionId) {
        LOGGER.debug("Getting user details for ID: {}", id);

        User user = userRepository.findByIdWithInstitutions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // If GESTOR, verify they can access this user
        if (!isAdmin) {
            validateGestorCanAccessUser(user, requestingUserInstitutionId);
        }

        return UserDetailDTO.fromEntity(user);
    }

    /**
     * Updates basic user information (name and status).
     * Logs changes to audit trail.
     *
     * Uses SERIALIZABLE isolation to prevent race conditions when multiple
     * admins/managers update the same user concurrently.
     *
     * IMPORTANT: Status PENDING is managed automatically based on institution links.
     * - User with no institution links: status is PENDING (automatic, cannot be changed manually)
     * - User with institution links: status can be ACTIVE or INACTIVE (manual control)
     * - PENDING status cannot be manually set
     *
     * @param id the user's UUID
     * @param request the update data
     * @param requestingUserId the ID of the user making the request
     * @param isAdmin whether the requesting user is an ADMIN
     * @param requestingUserInstitutionId the institution ID of the requesting user (for GESTOR)
     * @return updated user details
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDetailDTO updateUser(UUID id, UpdateUserRequest request, UUID requestingUserId,
                                    boolean isAdmin, UUID requestingUserInstitutionId) {
        LOGGER.info("Updating user: {}", id);

        User user = userRepository.findByIdWithInstitutions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // If GESTOR, verify they can access this user
        if (!isAdmin) {
            validateGestorCanAccessUser(user, requestingUserInstitutionId);
        }

        // Validate status change based on institution links
        validateStatusChange(user, request.getStatus());

        // Guardar valores antigos para auditoria
        String oldName = user.getName();
        Object oldStatus = user.getStatus();

        user.setName(request.getName());
        user.setStatus(request.getStatus());

        User savedUser = userRepository.save(user);

        // Registrar auditoria
        User performedBy = userService.findById(requestingUserId);
        auditService.logUserUpdate(performedBy, savedUser, oldName, request.getName(),
                                   oldStatus, request.getStatus());

        LOGGER.info("User {} updated successfully", id);

        return UserDetailDTO.fromEntity(savedUser);
    }

    /**
     * Validates if a status change is allowed based on institution links.
     *
     * Rules:
     * - PENDING status cannot be manually set (it's automatic)
     * - Users without institution links must remain PENDING
     * - Users with institution links can only be ACTIVE or INACTIVE
     *
     * @param user the user being updated
     * @param newStatus the new status to be set
     * @throws BadRequestException if the status change is not allowed
     */
    private void validateStatusChange(User user, UserStatus newStatus) {
        Set<Institution> activeInstitutions = user.getActiveInstitutions();
        boolean hasInstitutions = !activeInstitutions.isEmpty();

        // Rule 1: PENDING status cannot be manually set
        if (newStatus == UserStatus.PENDING) {
            LOGGER.warn("Attempt to manually set PENDING status for user {}", user.getId());
            throw new BadRequestException(
                "Status PENDING is managed automatically and cannot be set manually. " +
                "Users without institution links are automatically set to PENDING."
            );
        }

        // Rule 2: Users without institutions must remain PENDING
        if (!hasInstitutions && user.getStatus() == UserStatus.PENDING) {
            LOGGER.warn("Attempt to change status of user {} without institutions (current: PENDING, requested: {})",
                       user.getId(), newStatus);
            throw new BadRequestException(
                "Cannot change status of users without institution links. " +
                "Users must be linked to at least one institution before their status can be changed to ACTIVE or INACTIVE."
            );
        }

        // Rule 3: Users with institutions can only be ACTIVE or INACTIVE (not PENDING)
        if (hasInstitutions && newStatus == UserStatus.PENDING) {
            LOGGER.warn("Attempt to set PENDING status for user {} with {} active institutions",
                       user.getId(), activeInstitutions.size());
            throw new BadRequestException(
                "Cannot set status to PENDING for users with institution links. " +
                "Remove all institution links first, and the status will be automatically set to PENDING."
            );
        }

        LOGGER.debug("Status change validated for user {}: {} -> {}",
                    user.getId(), user.getStatus(), newStatus);
    }

    /**
     * Updates user roles within a specific institution.
     * Logs changes to audit trail.
     *
     * SECURITY: Only system ADMIN can update roles in the SIMP-ADMIN institution.
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

        // Verify institution exists
        var institution = institutionService.findById(request.getInstitutionId());

        // SECURITY: Only ADMIN can update roles in SIMP-ADMIN institution
        if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
            if (!isAdmin) {
                LOGGER.warn("SECURITY VIOLATION: Non-ADMIN user {} attempted to update roles in SIMP-ADMIN institution",
                           requestingUserId);
                throw new UnauthorizedAccessException(
                    "Only system administrators can update roles in the " +
                    InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
            }
        }

        // If GESTOR, verify they can manage this institution
        if (!isAdmin) {
            if (!request.getInstitutionId().equals(requestingUserInstitutionId)) {
                throw new UnauthorizedAccessException(
                    "GESTOR can only manage users in their own institution");
            }
        }

        // Validate that ADMIN role can only be assigned to SIMP-ADMIN institution
        if (request.getRoles() != null && request.getRoles().contains(InstitutionRole.ADMIN)) {
            if (!InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
                LOGGER.warn("Attempt to assign ADMIN role to non-admin institution: {} (acronym: {})",
                           institution.getName(), institution.getAcronym());
                throw new BadRequestException(
                    "ADMIN role can only be assigned to users in the " +
                    InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
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

        // Registrar auditoria
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
     * SECURITY: Only system ADMIN can link users to the SIMP-ADMIN institution.
     *
     * @param userId the user's UUID
     * @param request the link request with institution ID and roles
     * @param linkedByUserId the ID of the user creating the link
     * @param isAdmin whether the requesting user is a system ADMIN
     */
    @Transactional
    public void linkUserToInstitution(UUID userId, LinkUserInstitutionRequest request,
                                      UUID linkedByUserId, boolean isAdmin) {
        LOGGER.info("Linking user {} to institution {}", userId, request.getInstitutionId());

        User user = userService.findById(userId);
        var institution = institutionService.findById(request.getInstitutionId());
        User linkedBy = userService.findById(linkedByUserId);

        // SECURITY: Only ADMIN can link users to SIMP-ADMIN institution
        if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
            if (!isAdmin) {
                LOGGER.warn("SECURITY VIOLATION: Non-ADMIN user {} attempted to link user {} to SIMP-ADMIN institution",
                           linkedByUserId, userId);
                throw new UnauthorizedAccessException(
                    "Only system administrators can link users to the " +
                    InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
            }
        }

        // Validate that ADMIN role can only be assigned to SIMP-ADMIN institution
        if (request.getRoles() != null && request.getRoles().contains(InstitutionRole.ADMIN)) {
            if (!InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
                LOGGER.warn("Attempt to link user with ADMIN role to non-admin institution: {} (acronym: {})",
                           institution.getName(), institution.getAcronym());
                throw new BadRequestException(
                    "ADMIN role can only be assigned to users in the " +
                    InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
            }
        }

        // Check if link already exists (active or inactive)
        Optional<UserInstitution> existingLink = userInstitutionRepository
                .findByUserIdAndInstitutionId(userId, request.getInstitutionId());

        UserInstitution userInstitution;
        if (existingLink.isPresent()) {
            userInstitution = existingLink.get();

            if (userInstitution.getActive()) {
                // Link is already active
                throw new BadRequestException("User is already linked to this institution");
            }

            // Reactivate inactive link
            LOGGER.info("Reactivating existing inactive link for user {} and institution {}",
                       userId, request.getInstitutionId());
            userInstitution.reactivate();
            userInstitution.setRoles(request.getRoles());
            userInstitution.setLinkedBy(linkedBy);
        } else {
            // Create new link
            userInstitution = UserInstitution.builder()
                    .user(user)
                    .institution(institution)
                    .roles(request.getRoles())
                    .linkedBy(linkedBy)
                    .active(true)
                    .build();
        }

        userInstitutionRepository.save(userInstitution);

        // If user was PENDING and this is their first institution, activate them
        if (user.getStatus() == UserStatus.PENDING &&
            user.getActiveInstitutions().size() == 0) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            LOGGER.info("User {} status changed from PENDING to ACTIVE", userId);
        }

        // Registrar auditoria
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
     * SECURITY: Only system ADMIN can unlink users from the SIMP-ADMIN institution.
     *
     * @param userId the user's UUID
     * @param institutionId the institution's UUID
     * @param requestingUserId the ID of the user making the request (for audit)
     * @param isAdmin whether the requesting user is a system ADMIN
     */
    @Transactional
    public void unlinkUserFromInstitution(UUID userId, UUID institutionId,
                                          UUID requestingUserId, boolean isAdmin) {
        LOGGER.info("Unlinking user {} from institution {}", userId, institutionId);

        // First check if the link exists (to throw ResourceNotFoundException if not)
        UserInstitution userInstitution = userInstitutionRepository
                .findByUserIdAndInstitutionId(userId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

        // Now load institution and check security constraints
        var institution = institutionService.findById(institutionId);

        // SECURITY: Only ADMIN can unlink users from SIMP-ADMIN institution
        if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
            if (!isAdmin) {
                LOGGER.warn("SECURITY VIOLATION: Non-ADMIN user {} attempted to unlink user {} from SIMP-ADMIN institution",
                           requestingUserId, userId);
                throw new UnauthorizedAccessException(
                    "Only system administrators can unlink users from the " +
                    InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
            }
        }

        userInstitution.setActive(false);
        userInstitutionRepository.save(userInstitution);

        // If user has no more active institutions, set status back to PENDING
        User user = userService.findById(userId);

        if (user.getActiveInstitutions().isEmpty()) {
            user.setStatus(UserStatus.PENDING);
            userRepository.save(user);
            LOGGER.info("User {} has no more institutions, status changed to PENDING", userId);
        }

        // Registrar auditoria
        User performedBy = userService.findById(requestingUserId);
        auditService.logUserUnlinkedFromInstitution(performedBy, user, institutionId, institution.getName());

        LOGGER.info("User {} unlinked from institution {}", userId, institutionId);
    }

    /**
     * Validates that a GESTOR can access a user (user must belong to GESTOR's institution).
     *
     * @param user the user to validate access to
     * @param gestorInstitutionId the GESTOR's institution ID
     * @throws UnauthorizedAccessException if the user doesn't belong to the institution
     */
    private void validateGestorCanAccessUser(User user, UUID gestorInstitutionId) {
        if (gestorInstitutionId == null) {
            throw new UnauthorizedAccessException("GESTOR must have an active institution");
        }

        boolean belongsToInstitution = user.getInstitutions().stream()
                .anyMatch(ui -> ui.getActive() &&
                               ui.getInstitution().getId().equals(gestorInstitutionId));

        if (!belongsToInstitution) {
            throw new UnauthorizedAccessException(
                "GESTOR can only access users from their own institution");
        }
    }
}
