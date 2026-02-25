package com.simplifica.presentation.controller;

import com.simplifica.application.dto.AssignUserToInstitutionDTO;
import com.simplifica.application.dto.CreateInstitutionDTO;
import com.simplifica.application.dto.InstitutionDTO;
import com.simplifica.application.dto.UpdateInstitutionDTO;
import com.simplifica.application.dto.UserInstitutionDTO;
import com.simplifica.application.service.InstitutionService;
import com.simplifica.application.service.UserInstitutionService;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionType;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.presentation.exception.BadRequestException;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing institutions (tenants).
 *
 * This controller provides endpoints for CRUD operations on institutions,
 * managing user-institution relationships, and querying institution data.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private UserInstitutionService userInstitutionService;

    /**
     * Lists all institutions with optional filters and pagination.
     *
     * @param active filter by active status (optional)
     * @param type filter by institution type (optional)
     * @param search search term for name or acronym (optional)
     * @param pageable pagination and sorting parameters
     * @return paginated list of institutions
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<InstitutionDTO>> listInstitutions(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) InstitutionType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Institution> institutions = institutionService.findAll(active, type, search, pageable);
        Page<InstitutionDTO> dtos = institutions.map(InstitutionDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets a single institution by ID.
     *
     * @param id the institution's UUID
     * @return the institution DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> getInstitution(@PathVariable UUID id) {
        Institution institution = institutionService.findById(id);
        return ResponseEntity.ok(InstitutionDTO.fromEntity(institution));
    }

    /**
     * Creates a new institution.
     *
     * @param name institution name
     * @param acronym institution acronym
     * @param type institution type
     * @param domain institution domain (optional)
     * @param active active status (optional, defaults to true)
     * @param logo institution logo file (optional)
     * @return the created institution DTO with HTTP 201 status
     */
    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> createInstitution(
            @RequestParam String name,
            @RequestParam String acronym,
            @RequestParam InstitutionType type,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestPart(required = false) MultipartFile logo) {

        CreateInstitutionDTO dto = CreateInstitutionDTO.builder()
                .name(name)
                .acronym(acronym)
                .type(type)
                .domain(domain)
                .active(active)
                .build();

        Institution institution = institutionService.create(dto, logo);
        return ResponseEntity.status(HttpStatus.CREATED).body(InstitutionDTO.fromEntity(institution));
    }

    /**
     * Updates an existing institution.
     *
     * @param id the institution's UUID
     * @param name institution name (optional)
     * @param type institution type (optional)
     * @param domain institution domain (optional)
     * @param active active status (optional)
     * @param logo institution logo file (optional)
     * @return the updated institution DTO
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data", "application/json"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> updateInstitution(
            @PathVariable UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) InstitutionType type,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Boolean active,
            @RequestPart(required = false) MultipartFile logo) {

        UpdateInstitutionDTO dto = UpdateInstitutionDTO.builder()
                .name(name)
                .type(type)
                .domain(domain)
                .active(active)
                .build();

        Institution institution = institutionService.update(id, dto, logo);
        return ResponseEntity.ok(InstitutionDTO.fromEntity(institution));
    }

    /**
     * Removes the logo of an institution.
     *
     * @param id the institution's UUID
     * @return the updated institution DTO
     */
    @DeleteMapping("/{id}/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> deleteInstitutionLogo(@PathVariable UUID id) {
        Institution institution = institutionService.deleteLogo(id);
        return ResponseEntity.ok(InstitutionDTO.fromEntity(institution));
    }

    /**
     * Soft deletes an institution by setting its active status to false.
     *
     * @param id the institution's UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInstitution(@PathVariable UUID id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all users linked to an institution.
     *
     * @param id the institution's UUID
     * @return list of user-institution relationships
     */
    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInstitutionDTO>> getInstitutionUsers(@PathVariable UUID id) {
        List<UserInstitution> userInstitutions = userInstitutionService.getInstitutionUsers(id);
        List<UserInstitutionDTO> dtos = userInstitutions.stream()
                .map(UserInstitutionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Assigns a user to an institution with specific roles.
     *
     * @param id the institution's UUID
     * @param dto the assignment data (userId, institutionId, roles)
     * @return the created user-institution relationship with HTTP 201 status
     */
    @PostMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInstitutionDTO> assignUserToInstitution(
            @PathVariable UUID id,
            @Valid @RequestBody AssignUserToInstitutionDTO dto) {
        if (!id.equals(dto.getInstitutionId())) {
            throw new BadRequestException("Institution ID in path does not match institution ID in request body");
        }
        UserInstitution userInstitution = userInstitutionService.assignUserToInstitution(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserInstitutionDTO.fromEntity(userInstitution));
    }

    /**
     * Removes a user from an institution (soft delete).
     *
     * @param institutionId the institution's UUID
     * @param userId the user's UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{institutionId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserFromInstitution(
            @PathVariable UUID institutionId,
            @PathVariable UUID userId) {
        userInstitutionService.removeUserFromInstitution(userId, institutionId);
        return ResponseEntity.noContent().build();
    }
}
