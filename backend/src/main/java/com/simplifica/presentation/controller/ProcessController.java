package com.simplifica.presentation.controller;

import com.simplifica.application.dto.CreateProcessDTO;
import com.simplifica.application.dto.ProcessDTO;
import com.simplifica.application.dto.UpdateProcessDTO;
import com.simplifica.application.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing processes (business processes within institutions).
 *
 * Provides endpoints for CRUD operations on processes with multi-tenant support.
 * All operations are automatically scoped to the current institution from the
 * X-Institution-Id header via TenantInterceptor.
 *
 * Access Control:
 * - ROLE_MANAGER (GESTOR): Can manage processes in their institution
 * - ROLE_ADMIN: Can manage processes in any institution (when context is set)
 *
 * Key Features:
 * - Comprehensive filtering (active, search, valueChain, isCritical)
 * - Process mapping file uploads (HTML)
 * - Pagination and sorting support
 * - Soft delete functionality
 */
@RestController
@RequestMapping("/processes")
public class ProcessController {

    @Autowired
    private ProcessService processService;

    /**
     * Lists all processes with optional filters and pagination.
     * Automatically filtered by the current institution from TenantContext.
     *
     * Query Parameters:
     * - active: Filter by active status (optional)
     * - search: Search term for name (optional)
     * - valueChainId: Filter by value chain (optional)
     * - isCritical: Filter by critical status (optional)
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort field (default: name)
     * - direction: Sort direction (default: ASC)
     *
     * @param active filter by active status (optional)
     * @param search search term for name (optional)
     * @param valueChainId filter by value chain (optional)
     * @param isCritical filter by critical status (optional)
     * @param pageable pagination and sorting parameters
     * @return paginated list of processes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ProcessDTO>> listProcesses(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID valueChainId,
            @RequestParam(required = false) Boolean isCritical,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ProcessDTO> processes = processService.findAll(active, search, valueChainId, isCritical, pageable);
        return ResponseEntity.ok(processes);
    }

    /**
     * Gets a single process by ID.
     *
     * @param id the process UUID
     * @return the process DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProcessDTO> getProcess(@PathVariable UUID id) {
        ProcessDTO process = processService.findById(id);
        return ResponseEntity.ok(process);
    }

    /**
     * Creates a new process.
     * Automatically assigned to the current institution from TenantContext.
     *
     * Request Body:
     * - name: Process name (required, max 255 chars)
     * - description: Process description (optional, max 5000 chars)
     * - valueChainId: Value chain UUID (optional)
     * - responsibleUnitId: Responsible unit UUID (optional)
     * - directUnitId: Direct unit UUID (optional)
     * - isCritical: Critical flag (optional, default: false)
     * - documentationStatus: Documentation status enum (optional)
     * - documentationUrl: Documentation URL (optional, max 1024 chars)
     * - externalGuidanceStatus: External guidance status enum (optional)
     * - externalGuidanceUrl: External guidance URL (optional, max 1024 chars)
     * - riskManagementStatus: Risk management status enum (optional)
     * - riskManagementUrl: Risk management URL (optional, max 1024 chars)
     * - mappingStatus: Mapping status enum (optional)
     * - active: Active flag (optional, default: true)
     *
     * @param dto the process data
     * @return the created process DTO with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProcessDTO> createProcess(@Valid @RequestBody CreateProcessDTO dto) {
        ProcessDTO process = processService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(process);
    }

    /**
     * Updates an existing process.
     * Only non-null fields in the request body are updated (partial update).
     *
     * Request Body (all fields optional):
     * - name: Process name (max 255 chars)
     * - description: Process description (max 5000 chars)
     * - valueChainId: Value chain UUID
     * - responsibleUnitId: Responsible unit UUID
     * - directUnitId: Direct unit UUID
     * - isCritical: Critical flag
     * - documentationStatus: Documentation status enum
     * - documentationUrl: Documentation URL (max 1024 chars)
     * - externalGuidanceStatus: External guidance status enum
     * - externalGuidanceUrl: External guidance URL (max 1024 chars)
     * - riskManagementStatus: Risk management status enum
     * - riskManagementUrl: Risk management URL (max 1024 chars)
     * - mappingStatus: Mapping status enum
     * - active: Active flag
     *
     * @param id the process UUID
     * @param dto the updated data
     * @return the updated process DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProcessDTO> updateProcess(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProcessDTO dto) {

        ProcessDTO process = processService.update(id, dto);
        return ResponseEntity.ok(process);
    }

    /**
     * Soft deletes a process by setting its active status to false.
     * The process and its related data remain in the database for audit purposes.
     *
     * @param id the process UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteProcess(@PathVariable UUID id) {
        processService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads multiple HTML mapping files for a process.
     * Accepts multipart/form-data with one or more HTML files.
     *
     * Each file is validated to ensure it's a valid HTML file:
     * - MIME type must be text/html
     * - File extension must be .html
     * - File size must not exceed 10MB
     * - File content must contain valid HTML tags
     *
     * The uploaded files are stored in the "processes" folder and
     * associated with the process as ProcessMapping entities.
     *
     * @param id the process UUID
     * @param files list of HTML files to upload
     * @return the updated process DTO with new mappings
     */
    @PostMapping("/{id}/mappings")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProcessDTO> uploadMappings(
            @PathVariable UUID id,
            @RequestParam("files") List<MultipartFile> files) {

        ProcessDTO process = processService.uploadMappings(id, files);
        return ResponseEntity.ok(process);
    }

    /**
     * Deletes a specific mapping file from a process.
     * The file is removed from storage and the database.
     *
     * @param id the process UUID
     * @param mappingId the mapping UUID to delete
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}/mappings/{mappingId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteMapping(
            @PathVariable UUID id,
            @PathVariable UUID mappingId) {

        processService.deleteMapping(id, mappingId);
        return ResponseEntity.noContent().build();
    }
}
