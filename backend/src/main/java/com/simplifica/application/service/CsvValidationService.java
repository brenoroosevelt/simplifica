package com.simplifica.application.service;

import com.simplifica.presentation.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for validating and parsing CSV files.
 *
 * Provides validation of file properties (size, extension, MIME type)
 * and parsing of CSV content using Apache Commons CSV.
 *
 * Security considerations:
 * - File size limit to prevent memory exhaustion
 * - MIME type validation to prevent malicious file uploads
 * - Extension validation as additional security layer
 */
@Service
public class CsvValidationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "text/csv",
        "application/csv",
        "text/plain" // Some browsers send CSV as text/plain
    );
    private static final String CSV_EXTENSION = ".csv";

    /**
     * Validates the uploaded CSV file.
     *
     * Checks:
     * - File is not null or empty
     * - File has .csv extension
     * - File size is within limits
     * - MIME type is acceptable
     *
     * @param file the uploaded file to validate
     * @throws BadRequestException if validation fails
     */
    public void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(CSV_EXTENSION)) {
            throw new BadRequestException("File must be a CSV (.csv extension)");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid MIME type. Expected text/csv or application/csv");
        }
    }

    /**
     * Parses a CSV file into a list of records.
     *
     * Uses Apache Commons CSV with the following settings:
     * - First record is treated as header
     * - Empty lines are ignored
     * - Values are trimmed of whitespace
     *
     * @param file the CSV file to parse
     * @return list of CSV records (excluding header)
     * @throws BadRequestException if parsing fails
     */
    public List<CSVRecord> parseCsvFile(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<CSVRecord> records = parser.getRecords();

            // Validate headers after parsing
            validateHeaders(parser.getHeaderMap());

            return records;
        } catch (IOException e) {
            throw new BadRequestException("Failed to read CSV file: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that the CSV contains required headers.
     *
     * Required headers: name, acronym
     * Optional headers: description, active, institutionId, institutionAcronym
     *
     * @param headerMap the header map from CSV parser
     * @throws BadRequestException if required headers are missing
     */
    private void validateHeaders(Map<String, Integer> headerMap) {
        if (!headerMap.containsKey("name") || !headerMap.containsKey("acronym")) {
            throw new BadRequestException(
                "CSV must contain 'name' and 'acronym' columns. " +
                "Optional columns: description, active, institutionId, institutionAcronym"
            );
        }
    }
}
