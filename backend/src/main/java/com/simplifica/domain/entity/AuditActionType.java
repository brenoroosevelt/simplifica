package com.simplifica.domain.entity;

/**
 * Enumeration of audit action types that can be logged.
 */
public enum AuditActionType {
    /**
     * User basic information updated (name, status).
     */
    USER_UPDATED,

    /**
     * User roles updated in an institution.
     */
    USER_ROLES_UPDATED,

    /**
     * User linked to an institution.
     */
    USER_LINKED_TO_INSTITUTION,

    /**
     * User unlinked from an institution.
     */
    USER_UNLINKED_FROM_INSTITUTION,

    /**
     * User automatically linked to institution by email domain.
     */
    USER_AUTO_LINKED_BY_DOMAIN,

    /**
     * User status changed from PENDING to ACTIVE.
     */
    USER_ACTIVATED,

    /**
     * User status changed to INACTIVE.
     */
    USER_DEACTIVATED,

    /**
     * Institution created.
     */
    INSTITUTION_CREATED,

    /**
     * Institution updated.
     */
    INSTITUTION_UPDATED,

    /**
     * Institution deleted.
     */
    INSTITUTION_DELETED,

    /**
     * User permissions checked.
     */
    ACCESS_DENIED,

    /**
     * Failed authentication attempt.
     */
    AUTH_FAILED
}
