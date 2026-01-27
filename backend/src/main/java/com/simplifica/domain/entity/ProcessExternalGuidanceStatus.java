package com.simplifica.domain.entity;

/**
 * Enum representing the external user guidance status of a process.
 *
 * Indicates whether external user guidance is available for this process.
 */
public enum ProcessExternalGuidanceStatus {
    /**
     * External user guidance is fully available.
     */
    AVAILABLE,

    /**
     * External user guidance is not yet available.
     */
    NOT_AVAILABLE,

    /**
     * External user guidance is available but has pending items.
     */
    AVAILABLE_WITH_PENDING,

    /**
     * External user guidance is not necessary for this process.
     */
    NOT_NECESSARY
}
