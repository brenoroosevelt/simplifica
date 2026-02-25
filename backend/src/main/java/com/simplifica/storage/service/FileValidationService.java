package com.simplifica.storage.service;

import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.storage.config.StorageProperties;
import com.simplifica.storage.domain.FileCategory;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Service for validating uploaded files.
 *
 * Uses Apache Tika to detect actual MIME types (not just from file extension)
 * and validates against allowed types and size limits.
 */
@Service
public class FileValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidationService.class);

    private final Tika tika = new Tika();

    @Autowired
    private StorageProperties storageProperties;

    /**
     * Validates a file for a specific category.
     *
     * @param file the uploaded file
     * @param category the file category
     * @throws BadRequestException if validation fails
     */
    public void validateFile(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or null");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("File name is missing");
        }

        // Detect actual MIME type
        String detectedMimeType;
        try (InputStream inputStream = file.getInputStream()) {
            detectedMimeType = tika.detect(inputStream, originalFilename);
            LOGGER.debug("Detected MIME type for {}: {}", originalFilename, detectedMimeType);
        } catch (IOException e) {
            LOGGER.error("Failed to detect MIME type for file: {}", originalFilename, e);
            throw new BadRequestException("Failed to read file: " + e.getMessage());
        }

        // Validate MIME type based on category
        validateMimeType(detectedMimeType, category, originalFilename);

        // Validate file size
        validateFileSize(file.getSize(), category, originalFilename);
    }

    /**
     * Validates that the MIME type is allowed for the category.
     */
    private void validateMimeType(String mimeType, FileCategory category, String filename) {
        Set<String> allowedTypes = getAllowedTypesForCategory(category);

        if (!allowedTypes.contains(mimeType)) {
            String message = String.format(
                    "File type '%s' is not allowed for %s. Allowed types: %s",
                    mimeType, category, allowedTypes);
            LOGGER.warn("Invalid file type for {}: {}", filename, mimeType);
            throw new BadRequestException(message);
        }
    }

    /**
     * Validates that the file size is within limits for the category.
     */
    private void validateFileSize(long size, FileCategory category, String filename) {
        long maxSize = getMaxSizeForCategory(category);

        if (size > maxSize) {
            String message = String.format(
                    "File '%s' is too large (%.2f MB). Maximum allowed: %.2f MB",
                    filename, size / 1024.0 / 1024.0, maxSize / 1024.0 / 1024.0);
            LOGGER.warn("File too large: {} ({} bytes, max: {})", filename, size, maxSize);
            throw new BadRequestException(message);
        }

        if (size == 0) {
            throw new BadRequestException("File is empty: " + filename);
        }
    }

    /**
     * Gets allowed MIME types for a category.
     */
    private Set<String> getAllowedTypesForCategory(FileCategory category) {
        return switch (category) {
            case INSTITUTION_LOGO, VALUE_CHAIN_IMAGE, TRAINING_COVER ->
                    storageProperties.getAllowedImageTypes();
            case PROCESS_MAPPING, PROCESS_DOCUMENT, TRAINING_ATTACHMENT ->
                    storageProperties.getAllowedDocumentTypes();
            case TRAINING_VIDEO ->
                    storageProperties.getAllowedVideoTypes();
            case OTHER ->
                    // For OTHER, allow all image and document types
                    Set.of(); // Will be handled specially if needed
        };
    }

    /**
     * Gets maximum file size for a category.
     */
    private long getMaxSizeForCategory(FileCategory category) {
        return switch (category) {
            case TRAINING_VIDEO -> storageProperties.getMaxVideoSize();
            default -> storageProperties.getMaxFileSize();
        };
    }

    /**
     * Detects the MIME type of a file.
     *
     * @param inputStream the file input stream
     * @param filename the original filename (helps with detection)
     * @return the detected MIME type
     * @throws IOException if reading fails
     */
    public String detectMimeType(InputStream inputStream, String filename) throws IOException {
        return tika.detect(inputStream, filename);
    }

    /**
     * Checks if a MIME type represents an image.
     *
     * @param mimeType the MIME type
     * @return true if it's an image type
     */
    public boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Checks if a MIME type represents a video.
     *
     * @param mimeType the MIME type
     * @return true if it's a video type
     */
    public boolean isVideo(String mimeType) {
        return mimeType != null && mimeType.startsWith("video/");
    }
}
