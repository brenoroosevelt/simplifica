package com.simplifica.presentation.controller;

import com.simplifica.application.dto.LinkUserInstitutionRequest;
import com.simplifica.application.dto.PagedResponseDTO;
import com.simplifica.application.dto.UpdateUserRequest;
import com.simplifica.application.dto.UpdateUserRolesRequest;
import com.simplifica.application.dto.UserDetailDTO;
import com.simplifica.application.dto.UserListDTO;
import com.simplifica.application.service.UserAdminService;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for administrative user management operations.
 *
 * Provides endpoints for ADMIN and MANAGER (Gestor) roles to manage users,
 * their institution relationships, and roles. ADMIN has full access
 * to all users, while MANAGER can only manage users within their
 * own institution.
 *
 * Note: MANAGER role is referred to as "GESTOR" in Portuguese UI/documentation.
 */
@RestController
@RequestMapping("/admin/users")
public class AdminController {

    @Autowired
    private UserAdminService userAdminService;

    /**
     * Lists all users with optional filtering and pagination.
     *
     * Permissions:
     * - ADMIN: can see all users and filter by any institution
     * - GESTOR: can only see users from their institution (institutionId filter is ignored)
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
    public ResponseEntity<PagedResponseDTO<UserListDTO>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) InstitutionRole role,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<UserListDTO> users = userAdminService.listUsers(
            search,
            status,
            institutionId,
            role,
            pageable,
            userPrincipal.getId(),
            userPrincipal.isAdmin(),
            userPrincipal.getCurrentInstitutionId()
        );

        return ResponseEntity.ok(PagedResponseDTO.fromPage(users));
    }

    /**
     * Gets detailed information about a specific user.
     *
     * Permissions:
     * - ADMIN: can access any user
     * - GESTOR: can only access users from their institution
     *
     * @param id the user's UUID
     * @param userPrincipal the authenticated user principal
     * @return detailed user information
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDetailDTO> getUserById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserDetailDTO user = userAdminService.getUserById(
            id,
            userPrincipal.getId(),
            userPrincipal.isAdmin(),
            userPrincipal.getCurrentInstitutionId()
        );

        return ResponseEntity.ok(user);
    }

    /**
     * Updates basic user information (name and status).
     *
     * Permissions:
     * - ADMIN: can update any user
     * - GESTOR: can only update users from their institution
     *
     * @param id the user's UUID
     * @param request the update request
     * @param userPrincipal the authenticated user principal
     * @return updated user details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDetailDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserDetailDTO updatedUser = userAdminService.updateUser(
            id,
            request,
            userPrincipal.getId(),
            userPrincipal.isAdmin(),
            userPrincipal.getCurrentInstitutionId()
        );

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Updates user roles within a specific institution.
     *
     * Permissions:
     * - ADMIN: can update roles for any user in any institution
     * - GESTOR: can only update roles for users in their own institution
     *
     * @param id the user's UUID
     * @param request the roles update request
     * @param userPrincipal the authenticated user principal
     * @return no content (HTTP 204)
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateUserRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRolesRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        userAdminService.updateUserRoles(
            id,
            request,
            userPrincipal.getId(),
            userPrincipal.isAdmin(),
            userPrincipal.getCurrentInstitutionId()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Links a user to an institution with specific roles.
     *
     * Permissions:
     * - ADMIN only: GESTOR cannot link users to institutions
     * - Additional validation: only SIMP-ADMIN administrators can link users
     *
     * @param id the user's UUID
     * @param request the link request with institution ID and roles
     * @param userPrincipal the authenticated user principal
     * @return created (HTTP 201)
     * @throws UnauthorizedAccessException if user is not a SIMP-ADMIN administrator
     */
    @PostMapping("/{id}/institutions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> linkUserToInstitution(
            @PathVariable UUID id,
            @Valid @RequestBody LinkUserInstitutionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Validação adicional: apenas admins de SIMP-ADMIN podem vincular
        if (!userPrincipal.isAdmin()) {
            throw new UnauthorizedAccessException(
                "Only SIMP-ADMIN administrators can link users to institutions");
        }

        userAdminService.linkUserToInstitution(
            id,
            request,
            userPrincipal.getId(),
            userPrincipal.isAdmin()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Unlinks a user from an institution (soft delete).
     *
     * Permissions:
     * - ADMIN only: GESTOR cannot unlink users from institutions
     * - Additional validation: only SIMP-ADMIN administrators can unlink users
     *
     * @param id the user's UUID
     * @param institutionId the institution's UUID
     * @param userPrincipal the authenticated user principal
     * @return no content (HTTP 204)
     * @throws UnauthorizedAccessException if user is not a SIMP-ADMIN administrator
     */
    @DeleteMapping("/{id}/institutions/{institutionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unlinkUserFromInstitution(
            @PathVariable UUID id,
            @PathVariable UUID institutionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Validação adicional: apenas admins de SIMP-ADMIN podem desvincular
        if (!userPrincipal.isAdmin()) {
            throw new UnauthorizedAccessException(
                "Only SIMP-ADMIN administrators can unlink users from institutions");
        }

        userAdminService.unlinkUserFromInstitution(
            id,
            institutionId,
            userPrincipal.getId(),
            userPrincipal.isAdmin()
        );

        return ResponseEntity.noContent().build();
    }
}
