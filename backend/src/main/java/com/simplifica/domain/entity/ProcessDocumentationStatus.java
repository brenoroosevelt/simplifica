package com.simplifica.domain.entity;

/**
 * Enum representing the documentation status of a process.
 *
 * Indicates the current state of process documentation.
 */
public enum ProcessDocumentationStatus {
    /**
     * Process is fully documented.
     */
    DOCUMENTED,

    /**
     * Process documentation is not yet available.
     */
    NOT_DOCUMENTED,

    /**
     * Process is documented but has pending items.
     */
    DOCUMENTED_WITH_PENDING
}
