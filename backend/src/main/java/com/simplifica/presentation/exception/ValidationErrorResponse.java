package com.simplifica.presentation.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response format for validation errors.
 *
 * This DTO extends the standard error response to include field-level
 * validation errors, making it easier for clients to display specific
 * error messages for each invalid field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Map of field names to error messages.
     * Key: field name (e.g., "email", "name")
     * Value: validation error message
     */
    private Map<String, String> fieldErrors;
}
