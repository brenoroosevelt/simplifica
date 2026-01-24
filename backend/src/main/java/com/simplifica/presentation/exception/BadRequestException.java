package com.simplifica.presentation.exception;

/**
 * Exception thrown when a request contains invalid data or parameter mismatches.
 *
 * This exception should be used when the request syntax is valid but the content
 * is semantically incorrect or contains logical errors (e.g., ID mismatch between
 * path and body). Results in HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructs a new BadRequestException with a detail message.
     *
     * @param message the detail message explaining the bad request
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new BadRequestException with a message and cause.
     *
     * @param message the detail message explaining the bad request
     * @param cause the cause of the exception
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
