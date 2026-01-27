package com.simplifica.presentation.controller;

import com.simplifica.application.dto.CreateValueChainDTO;
import com.simplifica.application.dto.UpdateValueChainDTO;
import com.simplifica.application.dto.ValueChainDTO;
import com.simplifica.application.service.ValueChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST Controller for managing value chains (cadeias de valor).
 *
 * This controller provides endpoints for CRUD operations on value chains,
 * with multi-tenant isolation enforced by ValueChainService.
 * All endpoints require MANAGER or ADMIN role.
 */
@RestController
@RequestMapping("/value-chains")
public class ValueChainController {

    @Autowired
    private ValueChainService valueChainService;

    /**
     * Lists all value chains for the current institution with optional filters and pagination.
     *
     * @param active filter by active status (optional)
     * @param search search term for name (optional)
     * @param pageable pagination and sorting parameters
     * @return paginated list of value chains
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ValueChainDTO>> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ValueChainDTO> valueChains = valueChainService.findAll(active, search, pageable);
        return ResponseEntity.ok(valueChains);
    }

    /**
     * Gets a single value chain by ID.
     *
     * @param id the value chain's UUID
     * @return the value chain DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ValueChainDTO> getById(@PathVariable UUID id) {
        ValueChainDTO valueChain = valueChainService.findById(id);
        return ResponseEntity.ok(valueChain);
    }

    /**
     * Creates a new value chain.
     *
     * @param name value chain name (required)
     * @param description value chain description (optional)
     * @param active active status (optional, defaults to true)
     * @param image value chain image file (optional)
     * @return the created value chain DTO with HTTP 201 status
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ValueChainDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestPart(required = false) MultipartFile image) {

        CreateValueChainDTO dto = CreateValueChainDTO.builder()
                .name(name)
                .description(description)
                .active(active)
                .build();

        ValueChainDTO created = valueChainService.create(dto);

        // Upload image if provided
        if (image != null && !image.isEmpty()) {
            created = valueChainService.uploadImage(created.getId(), image);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing value chain.
     *
     * @param id the value chain's UUID
     * @param name value chain name (optional)
     * @param description value chain description (optional)
     * @param active active status (optional)
     * @param image new value chain image file (optional, replaces existing if provided)
     * @return the updated value chain DTO
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ValueChainDTO> update(
            @PathVariable UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean active,
            @RequestPart(required = false) MultipartFile image) {

        UpdateValueChainDTO dto = UpdateValueChainDTO.builder()
                .name(name)
                .description(description)
                .active(active)
                .build();

        ValueChainDTO updated = valueChainService.update(id, dto);

        // Upload new image if provided
        if (image != null && !image.isEmpty()) {
            updated = valueChainService.uploadImage(id, image);
        }

        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes the image of a value chain (keeps the value chain itself).
     *
     * @param id the value chain's UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        valueChainService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Soft deletes a value chain by setting its active status to false.
     *
     * @param id the value chain's UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        valueChainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
