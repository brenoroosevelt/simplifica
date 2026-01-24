package com.simplifica.presentation.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission for.
 *
 * This exception is distinct from Spring Security's AccessDeniedException and is used
 * for application-level authorization checks (e.g., accessing resources from a different
 * institution). Results in HTTP 403 Forbidden.
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedAccessException with a generic message.
     *
     * @param resourceName the name of the resource type being accessed
     * @param resourceId the identifier of the resource
     */
    public UnauthorizedAccessException(String resourceName, String resourceId) {
        super(String.format("Access denied to %s with identifier: %s", resourceName, resourceId));
    }

    /**
     * Constructs a new UnauthorizedAccessException with a custom message.
     *
     * @param message the detail message
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedAccessException with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
