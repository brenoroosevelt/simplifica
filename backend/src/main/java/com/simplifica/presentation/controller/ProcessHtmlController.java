package com.simplifica.presentation.controller;

import com.simplifica.domain.entity.ProcessMapping;
import com.simplifica.infrastructure.repository.ProcessMappingRepository;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * REST Controller for serving process mapping HTML files publicly.
 *
 * Provides public endpoints for accessing uploaded HTML mapping files
 * (typically exported from Bizagi or similar tools) with appropriate
 * security headers to prevent XSS and other attacks.
 *
 * SECURITY FEATURES:
 * - Content-Security-Policy (CSP) header to restrict inline scripts/styles
 * - X-Content-Type-Options: nosniff to prevent MIME type sniffing
 * - X-Frame-Options: SAMEORIGIN to prevent clickjacking
 * - Path traversal protection
 * - File validation (must be associated with a process)
 *
 * Endpoint:
 * - GET /public/process-mappings/{processId}/{filename} - serves HTML files
 *
 * Note: This is a PUBLIC endpoint (no authentication required) but validates
 * that the file exists and belongs to the specified process before serving.
 */
@RestController
@RequestMapping("/public/process-mappings")
@Slf4j
public class ProcessHtmlController {

    @Value("${app.storage.local.base-path:/tmp/simplifica/uploads}")
    private String basePath;

    @Autowired
    private ProcessMappingRepository processMappingRepository;

    /**
     * Serves an HTML mapping file for a process.
     *
     * SECURITY HEADERS:
     * - Content-Security-Policy: Restricts inline scripts and styles to prevent XSS
     * - X-Content-Type-Options: nosniff - Prevents MIME type sniffing
     * - X-Frame-Options: SAMEORIGIN - Prevents clickjacking
     *
     * The Content-Security-Policy allows:
     * - default-src 'self': Only load resources from same origin
     * - script-src 'unsafe-inline' 'self': Allow inline scripts and scripts from same origin
     *   (required for Bizagi exports which use inline scripts)
     * - style-src 'unsafe-inline' 'self': Allow inline styles and styles from same origin
     *   (required for Bizagi exports which use inline styles)
     * - img-src 'self' data:: Allow images from same origin and data URIs
     *   (required for embedded images in Bizagi exports)
     *
     * @param processId the process UUID (used for validation)
     * @param filename the HTML filename
     * @return the HTML file with security headers
     */
    @GetMapping("/{processId}/{filename:.+}")
    public ResponseEntity<Resource> serveProcessMapping(
            @PathVariable UUID processId,
            @PathVariable String filename) {

        log.info("Serving process mapping: processId={}, filename={}", processId, filename);

        // Security: Validate inputs before path construction
        if (!isValidPathSegment(filename)) {
            log.warn("Invalid filename segment detected: {}", filename);
            return ResponseEntity.notFound().build();
        }

        // Validate that the file exists and belongs to the specified process
        boolean mappingExists = processMappingRepository.findByProcessIdOrderByUploadedAtDesc(processId)
                .stream()
                .anyMatch(mapping -> mapping.getFilename().equals(filename));

        if (!mappingExists) {
            log.warn("Mapping file not found or does not belong to process: processId={}, filename={}",
                    processId, filename);
            return ResponseEntity.notFound().build();
        }

        try {
            // Construct file path: basePath/processes/filename
            Path filePath = Paths.get(basePath, "processes", filename).normalize();
            Resource resource = loadFileAsResource(filePath);

            // Return HTML with security headers
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    // Content Security Policy to prevent XSS attacks
                    .header("Content-Security-Policy",
                            "default-src 'self'; " +
                            "script-src 'unsafe-inline' 'self'; " +
                            "style-src 'unsafe-inline' 'self'; " +
                            "img-src 'self' data:")
                    // Prevent MIME type sniffing
                    .header("X-Content-Type-Options", "nosniff")
                    // Prevent clickjacking
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .body(resource);

        } catch (InvalidPathException e) {
            log.warn("Invalid path: processId={}, filename={}", processId, filename);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.warn("File not found: processId={}, filename={}", processId, filename);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Loads a file as a Spring Resource with security validations.
     *
     * Security validations:
     * - Ensures the resolved path is within the base directory (prevents path traversal)
     * - Checks that the file exists and is readable
     *
     * @param filePath the path to the file
     * @return the file as a Resource
     * @throws IOException if the file cannot be found or read
     */
    private Resource loadFileAsResource(Path filePath) throws IOException {
        // Security: Validate that filePath is within basePath to prevent path traversal
        Path baseDir = Paths.get(basePath).normalize().toAbsolutePath();
        Path resolvedPath = filePath.normalize().toAbsolutePath();

        if (!resolvedPath.startsWith(baseDir)) {
            log.warn("Path traversal attempt detected: {}", filePath);
            throw new IOException("Path traversal detected: " + filePath);
        }

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new IOException("File not found or not readable: " + filePath);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.debug("File loaded successfully: {}", filePath);
                return resource;
            } else {
                throw new IOException("File not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IOException("Malformed file path: " + filePath, e);
        }
    }

    /**
     * Validates that a path segment does not contain path traversal patterns.
     * Prevents attacks like: ../, ..\, absolute paths, null bytes, etc.
     *
     * This validation is critical for preventing path traversal attacks where
     * an attacker tries to access files outside the intended directory.
     *
     * @param segment the path segment to validate
     * @return true if the segment is safe, false otherwise
     */
    private boolean isValidPathSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return false;
        }

        // Check for null bytes (used to bypass extension checks in older systems)
        if (segment.contains("\0")) {
            return false;
        }

        // Check for path traversal with .. (even without slashes)
        // This catches: .., ../, ..\, ../../, etc.
        if (segment.contains("..")) {
            return false;
        }

        // Check for current directory references
        if (segment.contains("./") || segment.contains(".\\")) {
            return false;
        }

        // Check for absolute paths (Unix)
        if (segment.startsWith("/")) {
            return false;
        }

        // Check for absolute paths (Windows)
        if (segment.startsWith("\\")) {
            return false;
        }

        // Check for Windows drive letters (C:, D:, etc.)
        if (segment.length() >= 2 && Character.isLetter(segment.charAt(0)) && segment.charAt(1) == ':') {
            return false;
        }

        return true;
    }
}
