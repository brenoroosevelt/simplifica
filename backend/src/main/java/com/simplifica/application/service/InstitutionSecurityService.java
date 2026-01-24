package com.simplifica.application.service;

import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

/**
 * Service for institution-level security checks.
 *
 * This service provides methods to verify if the current user has access to
 * specific institutions and has required roles within those institutions.
 * Used by custom security annotations and method-level security.
 */
@Service
public class InstitutionSecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionSecurityService.class);

    @Autowired
    private UserInstitutionRepository userInstitutionRepository;

    /**
     * Checks if the current user has access to the specified institution.
     *
     * @param institutionId the institution's UUID
     * @return true if the user has an active link to the institution
     */
    public boolean hasAccessToInstitution(UUID institutionId) {
        UserPrincipal principal = getCurrentUser();
        if (principal == null) {
            LOGGER.warn("No authenticated user found in security context");
            return false;
        }

        // Global admin has access to all institutions
        if (principal.isAdmin()) {
            LOGGER.debug("User {} has admin access to all institutions", principal.getId());
            return true;
        }

        boolean hasAccess = userInstitutionRepository
                .findActiveByUserAndInstitution(principal.getId(), institutionId)
                .isPresent();

        LOGGER.debug("User {} access to institution {}: {}",
                     principal.getId(), institutionId, hasAccess);
        return hasAccess;
    }

    /**
     * Checks if the current user has any of the specified roles in the institution.
     *
     * @param institutionId the institution's UUID
     * @param roles the required roles (user needs at least one)
     * @return true if the user has any of the specified roles
     */
    public boolean hasRoleInInstitution(UUID institutionId, InstitutionRole... roles) {
        UserPrincipal principal = getCurrentUser();
        if (principal == null) {
            LOGGER.warn("No authenticated user found in security context");
            return false;
        }

        // Global admin bypasses institution role checks
        if (principal.isAdmin()) {
            LOGGER.debug("User {} has admin access, bypassing role check", principal.getId());
            return true;
        }

        UserInstitution userInstitution = userInstitutionRepository
                .findActiveByUserAndInstitution(principal.getId(), institutionId)
                .orElse(null);

        if (userInstitution == null) {
            LOGGER.debug("User {} has no link to institution {}", principal.getId(), institutionId);
            return false;
        }

        boolean hasRole = userInstitution.getRoles().stream()
                .anyMatch(role -> Arrays.asList(roles).contains(role));

        LOGGER.debug("User {} has required role in institution {}: {}",
                     principal.getId(), institutionId, hasRole);
        return hasRole;
    }

    /**
     * Checks if the current user can manage the specified institution.
     * Management requires either global admin role or institution ADMIN role.
     *
     * @param institutionId the institution's UUID
     * @return true if the user can manage the institution
     */
    public boolean canManageInstitution(UUID institutionId) {
        UserPrincipal principal = getCurrentUser();
        if (principal == null) {
            return false;
        }

        // Global admin can manage all institutions
        if (principal.isAdmin()) {
            LOGGER.debug("User {} has global admin access", principal.getId());
            return true;
        }

        // Check for institution ADMIN role
        return hasRoleInInstitution(institutionId, InstitutionRole.ADMIN);
    }

    /**
     * Gets the current authenticated user from the security context.
     *
     * @return the UserPrincipal or null if not authenticated
     */
    private UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return (UserPrincipal) auth.getPrincipal();
    }
}
