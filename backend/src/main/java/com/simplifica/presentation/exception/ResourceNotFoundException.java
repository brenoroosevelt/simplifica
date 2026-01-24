package com.simplifica.presentation.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * This exception should be used when a specific resource (identified by ID or other unique key)
 * cannot be found in the database or other data store. Results in HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with a generic message.
     *
     * @param resourceName the name of the resource type (e.g., "Institution", "User")
     * @param identifier the identifier used to search for the resource
     */
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }

    /**
     * Constructs a new ResourceNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
