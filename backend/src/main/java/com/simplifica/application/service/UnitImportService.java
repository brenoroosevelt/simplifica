package com.simplifica.application.service;

import com.simplifica.application.dto.CreateUnitDTO;
import com.simplifica.application.dto.ImportErrorDTO;
import com.simplifica.application.dto.UnitImportResultDTO;
import com.simplifica.application.dto.UnitImportRowDTO;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ValidationException;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service for importing units from CSV files.
 *
 * Handles the complete import workflow:
 * 1. File validation
 * 2. CSV parsing
 * 3. Institution resolution (based on user role and CSV data)
 * 4. Line-by-line processing with partial success support
 * 5. Result aggregation
 *
 * Multi-tenancy rules:
 * - MANAGER: Always imports to their current institution (cannot change)
 * - ADMIN: Can specify institutionId or institutionAcronym in CSV, or use current institution
 *
 * Error handling:
 * - Errors on individual lines do not stop processing
 * - All errors are collected and returned in the result
 * - Successful rows are imported even if other rows fail
 */
@Service
public class UnitImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitImportService.class);

    @Autowired
    private UnitService unitService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private CsvValidationService csvValidationService;

    /**
     * Imports units from a CSV file.
     *
     * @param file the CSV file containing unit data
     * @param userPrincipal the authenticated user
     * @return result containing success/failure counts and error details
     * @throws BadRequestException if file validation fails
     */
    @Transactional
    public UnitImportResultDTO importUnitsFromCsv(MultipartFile file, UserPrincipal userPrincipal) {
        LOGGER.info("Starting CSV import for user: {}", userPrincipal.getEmail());

        // 1. Validate and parse CSV file
        csvValidationService.validateCsvFile(file);
        List<CSVRecord> records = csvValidationService.parseCsvFile(file);

        LOGGER.info("Parsed {} records from CSV", records.size());

        // 2. Get default institution from tenant context
        UUID defaultInstitutionId = TenantContext.getCurrentInstitution();
        if (defaultInstitutionId == null) {
            throw new BadRequestException("No institution context set");
        }

        // 3. Process each row
        UnitImportResultDTO result = new UnitImportResultDTO();
        result.setTotalRows(records.size());

        boolean isAdmin = userPrincipal.isAdmin();
        LOGGER.debug("User is admin: {}", isAdmin);

        for (int i = 0; i < records.size(); i++) {
            CSVRecord record = records.get(i);
            int lineNumber = i + 2; // +1 for header, +1 for 1-based indexing

            try {
                // Parse row data
                UnitImportRowDTO row = parseRow(record);

                // Resolve target institution
                UUID targetInstitutionId = resolveInstitutionId(row, defaultInstitutionId, isAdmin);

                // Validate and create unit
                validateAndCreateUnit(row, targetInstitutionId);

                result.incrementSuccess();
                result.addSuccessfulUnit(row.getName());

                LOGGER.debug("Successfully imported unit '{}' at line {}", row.getName(), lineNumber);

            } catch (Exception e) {
                result.incrementFailed();
                result.addError(new ImportErrorDTO(
                    lineNumber,
                    record.toString(),
                    e.getMessage(),
                    extractFieldFromException(e)
                ));

                LOGGER.warn("Failed to import line {}: {}", lineNumber, e.getMessage());
            }
        }

        LOGGER.info("Import completed. Success: {}, Failed: {}",
                   result.getSuccessCount(), result.getFailedCount());

        return result;
    }

    /**
     * Parses a CSV record into a UnitImportRowDTO.
     *
     * @param record the CSV record
     * @return parsed DTO
     */
    private UnitImportRowDTO parseRow(CSVRecord record) {
        return UnitImportRowDTO.builder()
                .name(getColumnValue(record, "name"))
                .acronym(getColumnValue(record, "acronym"))
                .description(getColumnValue(record, "description"))
                .active(parseBoolean(getColumnValue(record, "active"), true))
                .institutionId(getColumnValue(record, "institutionId"))
                .institutionAcronym(getColumnValue(record, "institutionAcronym"))
                .build();
    }

    /**
     * Gets a column value from CSV record, returning null if not mapped.
     *
     * @param record the CSV record
     * @param columnName the column name
     * @return the value, or null if column doesn't exist
     */
    private String getColumnValue(CSVRecord record, String columnName) {
        if (!record.isMapped(columnName)) {
            return null;
        }
        String value = record.get(columnName);
        return (value == null || value.isBlank()) ? null : value;
    }

    /**
     * Parses a string to boolean, with default value if null/empty.
     *
     * @param value the string value
     * @param defaultValue the default if value is null/empty
     * @return parsed boolean
     */
    private Boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Resolves the target institution ID based on user role and CSV data.
     *
     * Rules:
     * - MANAGER: Always uses current institution (ignores CSV institution fields)
     * - ADMIN: Can specify institutionId or institutionAcronym in CSV,
     *          otherwise uses current institution
     *
     * @param row the CSV row data
     * @param defaultInstitutionId the current institution from context
     * @param isAdmin whether the user has admin role
     * @return the resolved institution ID
     * @throws ValidationException if institution is invalid or inactive
     */
    private UUID resolveInstitutionId(UnitImportRowDTO row, UUID defaultInstitutionId, boolean isAdmin) {
        // Non-admin ALWAYS uses institution from context
        if (!isAdmin) {
            return defaultInstitutionId;
        }

        // Admin: use institutionId from CSV if provided
        if (row.getInstitutionId() != null && !row.getInstitutionId().isBlank()) {
            try {
                UUID institutionId = UUID.fromString(row.getInstitutionId());
                Institution institution = institutionService.findById(institutionId);
                if (!institution.getActive()) {
                    throw new ValidationException("Institution is inactive: " + row.getInstitutionId());
                }
                return institutionId;
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid institution ID format: " + row.getInstitutionId());
            }
        }

        // Admin: use institutionAcronym if provided
        if (row.getInstitutionAcronym() != null && !row.getInstitutionAcronym().isBlank()) {
            Institution institution = institutionService.findByAcronym(row.getInstitutionAcronym());
            if (!institution.getActive()) {
                throw new ValidationException("Institution is inactive: " + row.getInstitutionAcronym());
            }
            return institution.getId();
        }

        // Otherwise, use institution from context
        return defaultInstitutionId;
    }

    /**
     * Validates and creates a unit from CSV row data.
     *
     * Performs validations:
     * - Required fields (name, acronym)
     * - Field length constraints
     * - Business rules (via UnitService.create)
     *
     * @param row the CSV row data
     * @param institutionId the target institution ID
     * @throws ValidationException if validation fails
     */
    private void validateAndCreateUnit(UnitImportRowDTO row, UUID institutionId) {
        // Validate required fields
        if (row.getName() == null || row.getName().isBlank()) {
            throw new ValidationException("Name is required");
        }
        if (row.getAcronym() == null || row.getAcronym().isBlank()) {
            throw new ValidationException("Acronym is required");
        }

        // Validate field lengths
        if (row.getName().length() > 255) {
            throw new ValidationException("Name exceeds maximum length of 255 characters");
        }
        if (row.getAcronym().length() > 50) {
            throw new ValidationException("Acronym exceeds maximum length of 50 characters");
        }
        if (row.getDescription() != null && row.getDescription().length() > 5000) {
            throw new ValidationException("Description exceeds maximum length of 5000 characters");
        }

        // Create DTO
        CreateUnitDTO createDTO = CreateUnitDTO.builder()
                .name(row.getName())
                .acronym(row.getAcronym())
                .description(row.getDescription())
                .active(row.getActive() != null ? row.getActive() : true)
                .build();

        // Temporarily change context if needed
        UUID originalInstitutionId = TenantContext.getCurrentInstitution();
        try {
            TenantContext.setCurrentInstitution(institutionId);
            unitService.create(createDTO);
        } finally {
            // Always restore original context
            TenantContext.setCurrentInstitution(originalInstitutionId);
        }
    }

    /**
     * Extracts the field name from an exception message.
     *
     * Attempts to identify which field caused the error based on
     * keywords in the exception message.
     *
     * @param e the exception
     * @return field name if identifiable, null otherwise
     */
    private String extractFieldFromException(Exception e) {
        String message = e.getMessage().toLowerCase();
        if (message.contains("name")) {
            return "name";
        }
        if (message.contains("acronym")) {
            return "acronym";
        }
        if (message.contains("description")) {
            return "description";
        }
        if (message.contains("institution")) {
            return "institution";
        }
        return null;
    }
}
