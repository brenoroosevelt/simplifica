package com.simplifica.presentation.controller;

import com.simplifica.storage.domain.StoredFile;
import com.simplifica.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * REST controller for serving stored files.
 *
 * Provides public endpoints for accessing files by their UUID.
 * Implements HTTP caching headers (ETag, Cache-Control) for performance.
 */
@RestController
@RequestMapping("/files")
public class FileServeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServeController.class);

    @Autowired
    private StorageService storageService;

    /**
     * Serves a file by its UUID.
     *
     * Supports HTTP caching with ETag and If-None-Match headers.
     *
     * @param fileId the file's UUID
     * @param ifNoneMatch the If-None-Match header (ETag from previous request)
     * @return the file as a Resource with appropriate headers
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable UUID fileId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        LOGGER.debug("Serving file: {}", fileId);

        // Get metadata
        StoredFile metadata = storageService.getMetadata(fileId);
        String etag = metadata.getEtag();

        // Check if client has cached version
        if (etag != null && etag.equals(ifNoneMatch)) {
            LOGGER.debug("File {} not modified (ETag match)", fileId);
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        // Load file
        Resource resource = storageService.loadAsResource(fileId);

        // Build response with caching headers
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getSize())
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).mustRevalidate())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + metadata.getFilename() + "\"")
                .body(resource);
    }
}
