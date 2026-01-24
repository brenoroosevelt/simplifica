package com.simplifica.presentation.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 *
 * This exception should be used when a unique constraint violation occurs,
 * such as attempting to create an institution with an acronym or domain
 * that already exists. Results in HTTP 409 Conflict.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new ResourceAlreadyExistsException with a generic message.
     *
     * @param resourceName the name of the resource type (e.g., "Institution")
     * @param fieldName the name of the field that must be unique (e.g., "acronym")
     * @param fieldValue the conflicting value
     */
    public ResourceAlreadyExistsException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
    }

    /**
     * Constructs a new ResourceAlreadyExistsException with a custom message.
     *
     * @param message the detail message
     */
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceAlreadyExistsException with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
