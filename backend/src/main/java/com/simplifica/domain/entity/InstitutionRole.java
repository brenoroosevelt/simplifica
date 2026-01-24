package com.simplifica.domain.entity;

/**
 * Enum representing user roles within an institution.
 *
 * These roles are specific to each institution and define what actions
 * a user can perform within that institution's context:
 * - ADMIN: Full management access to the institution
 * - MANAGER: Moderate access, can manage content but not users
 * - VIEWER: Read-only access to institution data
 */
public enum InstitutionRole {
    ADMIN,
    MANAGER,
    VIEWER;

    /**
     * Converts a string representation to an InstitutionRole enum value.
     *
     * @param role the string representation of the role
     * @return the corresponding InstitutionRole enum value
     * @throws IllegalArgumentException if the role string is invalid
     */
    public static InstitutionRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Institution role cannot be null");
        }

        try {
            return InstitutionRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid institution role: " + role
                + ". Supported roles: ADMIN, MANAGER, VIEWER"
            );
        }
    }
}
