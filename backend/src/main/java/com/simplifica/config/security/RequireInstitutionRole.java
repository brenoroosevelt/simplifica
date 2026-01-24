package com.simplifica.config.security;

import com.simplifica.domain.entity.InstitutionRole;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to check if the current user has specific roles in an institution.
 *
 * This annotation uses Spring Security's @PreAuthorize internally and delegates
 * to the InstitutionSecurityService for role validation.
 *
 * Usage example:
 * <pre>
 * {@code
 * @PutMapping("/institutions/{institutionId}/settings")
 * @RequireInstitutionRole({InstitutionRole.ADMIN, InstitutionRole.MANAGER})
 * public ResponseEntity<?> updateSettings(@PathVariable UUID institutionId) {
 *     // User has ADMIN or MANAGER role in this institution
 * }
 * }
 * </pre>
 *
 * The annotation expects a method parameter named "institutionId" of type UUID.
 * Global admins automatically pass all role checks.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@institutionSecurityService.hasRoleInInstitution(#institutionId, #roles)")
public @interface RequireInstitutionRole {
    /**
     * The required roles. User must have at least one of these roles.
     */
    InstitutionRole[] value();
}
