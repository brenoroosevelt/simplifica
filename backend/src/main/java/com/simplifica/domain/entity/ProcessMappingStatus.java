package com.simplifica.domain.entity;

/**
 * Enum representing the mapping completion status of a process.
 *
 * Indicates whether the process has been mapped (e.g., with Bizagi diagrams).
 */
public enum ProcessMappingStatus {
    /**
     * Process is fully mapped.
     */
    MAPPED,

    /**
     * Process is not yet mapped.
     */
    NOT_MAPPED,

    /**
     * Process is mapped but has pending items.
     */
    MAPPED_WITH_PENDING
}
