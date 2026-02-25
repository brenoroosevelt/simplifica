package com.simplifica.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an error that occurred
 * during CSV import processing.
 *
 * Contains information about the line number, raw data,
 * error message, and optionally the field that caused the error.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDTO {

    /**
     * The line number in the CSV file where the error occurred.
     * Line 1 is the header, so data rows start at line 2.
     */
    private int lineNumber;

    /**
     * The raw CSV row data that caused the error.
     * Useful for debugging and re-attempting import.
     */
    private String rowData;

    /**
     * Human-readable error message explaining what went wrong.
     */
    private String errorMessage;

    /**
     * The field name that caused the error, if applicable.
     * Examples: "name", "acronym", "institutionId"
     * Null if the error is not field-specific.
     */
    private String field;
}
