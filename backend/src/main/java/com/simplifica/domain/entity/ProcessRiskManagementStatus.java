package com.simplifica.domain.entity;

/**
 * Enum representing the risk management status of a process.
 *
 * Indicates the current state of risk management preparation for a process.
 */
public enum ProcessRiskManagementStatus {
    /**
     * Risk management is fully prepared.
     */
    PREPARED,

    /**
     * Risk management is prepared but has pending items.
     */
    PREPARED_WITH_PENDING,

    /**
     * Risk management is not yet prepared.
     */
    NOT_PREPARED
}
