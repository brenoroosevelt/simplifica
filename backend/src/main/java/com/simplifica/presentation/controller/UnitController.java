package com.simplifica.presentation.controller;

import com.simplifica.application.dto.CreateUnitDTO;
import com.simplifica.application.dto.UpdateUnitDTO;
import com.simplifica.application.dto.UnitDTO;
import com.simplifica.application.dto.UnitImportResultDTO;
import com.simplifica.application.service.UnitImportService;
import com.simplifica.application.service.UnitService;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.Unit;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.util.UUID;

/**
 * REST Controller for managing units (organizational units).
 *
 * Provides endpoints for CRUD operations on units with multi-tenant support.
 * All operations are automatically scoped to the current institution from the
 * X-Institution-Id header via TenantInterceptor.
 *
 * Access Control:
 * - ROLE_MANAGER (GESTOR): Can manage units in their institution
 * - ROLE_ADMIN: Can manage units in any institution (when context is set)
 */
@RestController
@RequestMapping("/units")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitImportService unitImportService;

    /**
     * Lists all units with optional filters and pagination.
     * Automatically filtered by the current institution from TenantContext.
     *
     * @param active filter by active status (optional)
     * @param search search term for name or acronym (optional)
     * @param pageable pagination and sorting parameters
     * @return paginated list of units
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<UnitDTO>> listUnits(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<Unit> units = unitService.findAll(active, search, pageable);
        Page<UnitDTO> dtos = units.map(UnitDTO::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets a single unit by ID.
     *
     * @param id the unit UUID
     * @return the unit DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitDTO> getUnit(@PathVariable UUID id) {
        Unit unit = unitService.findById(id);
        return ResponseEntity.ok(UnitDTO.fromEntity(unit));
    }

    /**
     * Creates a new unit.
     * Automatically assigned to the current institution from TenantContext.
     *
     * @param dto the unit data
     * @return the created unit DTO with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitDTO> createUnit(@Valid @RequestBody CreateUnitDTO dto) {
        Unit unit = unitService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UnitDTO.fromEntity(unit));
    }

    /**
     * Updates an existing unit.
     * Note: Acronym cannot be changed after creation.
     *
     * @param id the unit UUID
     * @param dto the updated data
     * @return the updated unit DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitDTO> updateUnit(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUnitDTO dto) {

        Unit unit = unitService.update(id, dto);
        return ResponseEntity.ok(UnitDTO.fromEntity(unit));
    }

    /**
     * Soft deletes a unit by setting its active status to false.
     *
     * @param id the unit UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteUnit(@PathVariable UUID id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Imports units from a CSV file.
     *
     * Supports bulk import with partial success - individual row failures
     * do not stop processing of other rows.
     *
     * Multi-tenancy:
     * - MANAGER: All units imported to their current institution
     * - ADMIN: Can specify institutionId or institutionAcronym in CSV
     *
     * CSV format:
     * Required columns: name, acronym
     * Optional columns: description, active, institutionId, institutionAcronym
     *
     * @param file the CSV file containing unit data
     * @param userPrincipal the authenticated user
     * @return import result with success/failure counts and error details
     */
    @PostMapping("/import-csv")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitImportResultDTO> importUnitsFromCsv(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UnitImportResultDTO result = unitImportService.importUnitsFromCsv(file, userPrincipal);
        return ResponseEntity.ok(result);
    }
}
