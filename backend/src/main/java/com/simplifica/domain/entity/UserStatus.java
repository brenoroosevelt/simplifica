package com.simplifica.domain.entity;

/**
 * Enum representing the status of a user account.
 *
 * Lifecycle states:
 * - PENDING: Account created but awaiting approval (initial state for new users)
 * - ACTIVE: Account approved and active
 * - INACTIVE: Account deactivated or suspended
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    INACTIVE;

    /**
     * Converts a string representation to a UserStatus enum value.
     *
     * @param status the string representation of the status
     * @return the corresponding UserStatus enum value
     * @throws IllegalArgumentException if the status string is invalid
     */
    public static UserStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid user status: " + status + ". Supported statuses: PENDING, ACTIVE, INACTIVE"
            );
        }
    }
}
