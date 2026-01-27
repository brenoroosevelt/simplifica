package com.simplifica.domain.constants;

/**
 * Constants related to institutions and multi-tenant configuration.
 *
 * This class contains application-level constants that define institution
 * behavior and tenant rules.
 */
public final class InstitutionConstants {

    /**
     * Acronym of the administrative institution.
     *
     * This institution is special and has system-level administrative privileges.
     * Only users linked to this institution with ADMIN role have full system access.
     * ADMIN role should only be assigned to users in this institution.
     */
    public static final String ADMIN_INSTITUTION_ACRONYM = "SIMP-ADMIN";

    /**
     * Private constructor to prevent instantiation.
     */
    private InstitutionConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
