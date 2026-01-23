package com.simplifica.domain.entity;

/**
 * Enum representing user roles in the system.
 *
 * Defines the authorization levels for users:
 * - USER: Standard user with basic permissions
 * - ADMIN: Administrator with elevated permissions
 */
public enum UserRole {
    USER,
    ADMIN;

    /**
     * Converts a string representation to a UserRole enum value.
     *
     * @param role the string representation of the role
     * @return the corresponding UserRole enum value
     * @throws IllegalArgumentException if the role string is invalid
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid user role: " + role + ". Supported roles: USER, ADMIN"
            );
        }
    }
}
