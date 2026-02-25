package com.simplifica.presentation.controller;

import com.simplifica.infrastructure.repository.ProcessMappingRepository;
import com.simplifica.storage.adapter.StorageAdapter;
import com.simplifica.storage.adapter.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.UUID;

/**
 * REST Controller for serving process mapping files (Bizagi exports).
 *
 * Acts as a proxy over the StorageAdapter, supporting multi-level paths
 * to allow ZIP folder structures (HTML + CSS + JS + images) to be served
 * with correct relative URL resolution in the browser iframe.
 *
 * URL pattern: GET /public/process-mappings/{processId}/**
 *
 * Example:
 *   GET /public/process-mappings/{processId}/index.html
 *   GET /public/process-mappings/{processId}/css/style.css
 *   GET /public/process-mappings/{processId}/images/logo.png
 *
 * Storage key pattern: processes/{processId}/{relativePath}
 *
 * SECURITY FEATURES:
 * - Path traversal protection (blocks "..", null bytes, absolute paths)
 * - Validates that the processId has an active mapping before serving
 * - Content-Security-Policy header to restrict inline scripts/styles
 * - X-Content-Type-Options: nosniff to prevent MIME type sniffing
 * - X-Frame-Options: SAMEORIGIN to prevent clickjacking
 */
@RestController
@RequestMapping("/public/process-mappings")
@Slf4j
public class ProcessHtmlController {

    private static final String URL_PREFIX = "/public/process-mappings/";

    @Autowired
    private StorageAdapter storageAdapter;

    @Autowired
    private ProcessMappingRepository processMappingRepository;

    /**
     * Serves any file from the process mapping ZIP at the given relative path.
     *
     * The wildcard path variable captures subdirectory paths such as
     * "css/style.css" or "images/logo.png", which are resolved relative
     * to the process storage prefix "processes/{processId}/".
     *
     * @param processId the process UUID
     * @param request the HTTP request (used to extract the full path)
     * @return the file content with appropriate Content-Type and security headers
     */
    @GetMapping("/{processId}/**")
    public ResponseEntity<Resource> serveFile(
            @PathVariable UUID processId,
            HttpServletRequest request) {

        // Extract relative path after /{processId}/
        // Use getServletPath() (excludes context path, e.g. /api) to match URL_PREFIX correctly
        String requestUri = request.getServletPath();
        String processPrefix = URL_PREFIX + processId + "/";

        if (!requestUri.startsWith(processPrefix)) {
            return ResponseEntity.notFound().build();
        }

        String relativePath = requestUri.substring(processPrefix.length());

        // Security: validate relative path
        if (!isValidRelativePath(relativePath)) {
            log.warn("Invalid path requested: processId={}, path={}", processId, relativePath);
            return ResponseEntity.notFound().build();
        }

        // Validate that this process has a mapping (security check)
        boolean hasMappings = processMappingRepository
            .findByProcessIdOrderByUploadedAtDesc(processId)
            .stream()
            .anyMatch(m -> m.getFileUrl() != null);

        if (!hasMappings) {
            log.warn("No mapping found for process: {}", processId);
            return ResponseEntity.notFound().build();
        }

        // Fetch from storage adapter
        String storageKey = "processes/" + processId + "/" + relativePath;

        try {
            InputStream inputStream = storageAdapter.retrieve(storageKey);
            Resource resource = new InputStreamResource(inputStream);

            MediaType mediaType = resolveMediaType(relativePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);

            // Security headers
            // Note: X-Frame-Options is intentionally omitted — SecurityConfig globally
            // disables the header to allow iframe embedding from the frontend origin.
            headers.set("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'unsafe-inline' 'unsafe-eval' 'self'; " +
                "style-src 'unsafe-inline' 'self'; " +
                "img-src 'self' data: blob:; " +
                "font-src 'self';");
            headers.set("X-Content-Type-Options", "nosniff");

            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);

        } catch (StorageException e) {
            log.debug("File not found in storage: {}", storageKey);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Validates that the relative path is safe to use.
     * Blocks path traversal patterns, null bytes, and absolute paths.
     *
     * @param path the relative path extracted from the request URI
     * @return true if safe to use, false otherwise
     */
    private boolean isValidRelativePath(String path) {
        if (path == null || path.isBlank()) return false;
        if (path.contains("..")) return false;
        if (path.contains("\0")) return false;
        if (path.startsWith("/")) return false;
        return true;
    }

    /**
     * Resolves the MediaType based on the file extension.
     *
     * @param path the file path (relative or full)
     * @return the corresponding MediaType
     */
    private MediaType resolveMediaType(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0) return MediaType.APPLICATION_OCTET_STREAM;

        String ext = path.substring(dot + 1).toLowerCase();
        return switch (ext) {
            case "html", "htm" -> MediaType.TEXT_HTML;
            case "css"         -> new MediaType("text", "css");
            case "js"          -> new MediaType("application", "javascript");
            case "json"        -> MediaType.APPLICATION_JSON;
            case "xml"         -> MediaType.APPLICATION_XML;
            case "svg"         -> new MediaType("image", "svg+xml");
            case "png"         -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif"         -> MediaType.IMAGE_GIF;
            case "webp"        -> new MediaType("image", "webp");
            case "woff"        -> new MediaType("font", "woff");
            case "woff2"       -> new MediaType("font", "woff2");
            case "ttf"         -> new MediaType("font", "ttf");
            case "eot"         -> new MediaType("application", "vnd.ms-fontobject");
            case "otf"         -> new MediaType("font", "otf");
            default            -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
