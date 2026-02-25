package com.simplifica.presentation.exception;

/**
 * Exception thrown when data validation fails.
 *
 * This exception should be used when input data fails business logic validation
 * (e.g., invalid format, constraint violation, business rule violation).
 * Results in HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new ValidationException with a detail message.
     *
     * @param message the detail message explaining the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ValidationException with a message and cause.
     *
     * @param message the detail message explaining the validation failure
     * @param cause the cause of the exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
