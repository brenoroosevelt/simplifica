package com.simplifica.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

/**
 * REST Controller for serving uploaded files publicly.
 *
 * Provides public endpoints for accessing uploaded files and their thumbnails.
 * No authentication required - files are served directly from the filesystem.
 *
 * Endpoints:
 * - GET /public/uploads/{folder}/{filename} - serves the original file
 * - GET /public/uploads/{folder}/thumbnails/{filename} - serves the thumbnail
 */
@RestController
@RequestMapping("/public/uploads")
@Slf4j
public class FileController {

    @Value("${app.storage.local.base-path:/tmp/simplifica/uploads}")
    private String basePath;

    /**
     * Serves an uploaded file (original version).
     *
     * @param folder the folder name (e.g., "institutions")
     * @param filename the file name
     * @return the file as a response entity with appropriate content type
     */
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        log.info("Serving file: folder={}, filename={}", folder, filename);

        // Security: Validate inputs before path construction
        if (!isValidPathSegment(folder)) {
            log.warn("Invalid folder segment detected: {}", folder);
            return ResponseEntity.notFound().build();
        }

        if (!isValidPathSegment(filename)) {
            log.warn("Invalid filename segment detected: {}", filename);
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(basePath, folder, filename).normalize();
            Resource resource = loadFileAsResource(filePath);

            MediaType mediaType = determineMediaType(filePath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (InvalidPathException e) {
            log.warn("Invalid path: folder={}, filename={}", folder, filename);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.warn("File not found: folder={}, filename={}", folder, filename);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Serves a thumbnail version of an uploaded file.
     *
     * @param folder the folder name (e.g., "institutions")
     * @param filename the file name
     * @return the thumbnail as a response entity with appropriate content type
     */
    @GetMapping("/{folder}/thumbnails/{filename:.+}")
    public ResponseEntity<Resource> serveThumbnail(
            @PathVariable String folder,
            @PathVariable String filename) {

        log.info("Serving thumbnail: folder={}, filename={}", folder, filename);

        // Security: Validate inputs before path construction
        if (!isValidPathSegment(folder)) {
            log.warn("Invalid folder segment detected in thumbnail: {}", folder);
            return ResponseEntity.notFound().build();
        }

        if (!isValidPathSegment(filename)) {
            log.warn("Invalid filename segment detected in thumbnail: {}", filename);
            return ResponseEntity.notFound().build();
        }

        try {
            Path thumbnailPath = Paths.get(basePath, folder, "thumbnails", filename).normalize();
            Resource resource = loadFileAsResource(thumbnailPath);

            MediaType mediaType = determineMediaType(thumbnailPath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (InvalidPathException e) {
            log.warn("Invalid path: folder={}, filename={}", folder, filename);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.warn("Thumbnail not found: folder={}, filename={}", folder, filename);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Loads a file as a Spring Resource.
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

    /**
     * Determines the media type based on file extension.
     *
     * @param filePath the path to the file
     * @return the appropriate MediaType
     * @throws IOException if the media type cannot be determined
     */
    private MediaType determineMediaType(Path filePath) throws IOException {
        String contentType = Files.probeContentType(filePath);

        if (contentType == null) {
            String filename = filePath.getFileName().toString().toLowerCase();
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                return MediaType.IMAGE_JPEG;
            } else if (filename.endsWith(".png")) {
                return MediaType.IMAGE_PNG;
            } else if (filename.endsWith(".gif")) {
                return MediaType.IMAGE_GIF;
            } else if (filename.endsWith(".webp")) {
                return MediaType.parseMediaType("image/webp");
            } else {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return MediaType.parseMediaType(contentType);
    }
}
