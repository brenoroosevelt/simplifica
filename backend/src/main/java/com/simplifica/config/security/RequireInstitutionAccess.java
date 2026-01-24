package com.simplifica.config.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to check if the current user has access to a specific institution.
 *
 * This annotation uses Spring Security's @PreAuthorize internally and delegates
 * to the InstitutionSecurityService for access validation.
 *
 * Usage example:
 * <pre>
 * {@code
 * @GetMapping("/institutions/{institutionId}/data")
 * @RequireInstitutionAccess
 * public ResponseEntity<?> getData(@PathVariable UUID institutionId) {
 *     // User has verified access to this institution
 * }
 * }
 * </pre>
 *
 * The annotation expects a method parameter named "institutionId" of type UUID.
 * Global admins automatically have access to all institutions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@institutionSecurityService.hasAccessToInstitution(#institutionId)")
public @interface RequireInstitutionAccess {
}
