package com.simplifica.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing the result of a CSV import operation.
 *
 * Provides detailed statistics about the import including successful and
 * failed rows, with error details for failed rows and a list of successfully
 * created unit names.
 */
@Data
public class UnitImportResultDTO {

    /**
     * Total number of data rows in the CSV file (excluding header).
     */
    private int totalRows;

    /**
     * Number of rows successfully imported and created as units.
     */
    private int successCount;

    /**
     * Number of rows that failed validation or creation.
     */
    private int failedCount;

    /**
     * List of errors that occurred during import.
     * Each error contains line number, raw data, and error message.
     */
    private List<ImportErrorDTO> errors = new ArrayList<>();

    /**
     * List of names of successfully created units.
     * Useful for user feedback about what was imported.
     */
    private List<String> successfulUnits = new ArrayList<>();

    /**
     * Increments the success counter.
     * Should be called for each successfully imported row.
     */
    public void incrementSuccess() {
        successCount++;
    }

    /**
     * Increments the failed counter.
     * Should be called for each row that failed validation or creation.
     */
    public void incrementFailed() {
        failedCount++;
    }

    /**
     * Adds an error to the error list.
     *
     * @param error the error details
     */
    public void addError(ImportErrorDTO error) {
        errors.add(error);
    }

    /**
     * Adds a successfully created unit name to the success list.
     *
     * @param name the name of the created unit
     */
    public void addSuccessfulUnit(String name) {
        successfulUnits.add(name);
    }
}
