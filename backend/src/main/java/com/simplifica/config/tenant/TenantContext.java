package com.simplifica.config.tenant;

import java.util.UUID;

/**
 * Thread-local storage for the current tenant/institution context.
 *
 * This class manages the institution ID associated with the current request thread,
 * enabling tenant-aware data filtering throughout the application lifecycle.
 * The context is set by interceptors and cleared after request completion.
 *
 * Thread-safety: Uses ThreadLocal to ensure each thread has its own context.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_INSTITUTION = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private TenantContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sets the current institution ID for this thread.
     *
     * @param institutionId the institution ID to set, or null to clear
     */
    public static void setCurrentInstitution(UUID institutionId) {
        if (institutionId != null) {
            CURRENT_INSTITUTION.set(institutionId);
        } else {
            CURRENT_INSTITUTION.remove();
        }
    }

    /**
     * Gets the current institution ID for this thread.
     *
     * @return the institution ID, or null if not set
     */
    public static UUID getCurrentInstitution() {
        return CURRENT_INSTITUTION.get();
    }

    /**
     * Clears the current institution context for this thread.
     *
     * This should be called after request completion to prevent memory leaks
     * in thread-pooled environments.
     */
    public static void clear() {
        CURRENT_INSTITUTION.remove();
    }

    /**
     * Checks if an institution context is set for this thread.
     *
     * @return true if an institution ID is set, false otherwise
     */
    public static boolean isSet() {
        return CURRENT_INSTITUTION.get() != null;
    }
}
