package com.simplifica.application.service;

import com.simplifica.application.dto.CreateUnitDTO;
import com.simplifica.application.dto.UpdateUnitDTO;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.Unit;
import com.simplifica.infrastructure.repository.UnitRepository;
import com.simplifica.infrastructure.repository.UnitSpecifications;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ResourceAlreadyExistsException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing units (organizational units).
 *
 * Provides business logic for CRUD operations on units with
 * multi-tenant support. All operations are scoped to the current institution
 * from the TenantContext.
 *
 * Security:
 * - GESTOR: Can only access units from their own institution
 * - ADMIN: Can access units from any institution (if institution context is set)
 */
@Service
@Transactional(readOnly = true)
public class UnitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitService.class);

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private InstitutionService institutionService;

    /**
     * Finds a unit by ID with tenant validation.
     *
     * @param id the unit UUID
     * @return the Unit entity
     * @throws ResourceNotFoundException if not found
     * @throws UnauthorizedAccessException if not authorized to access
     */
    public Unit findById(UUID id) {
        LOGGER.debug("Finding unit by ID: {}", id);

        // Use specification with fetch join to avoid LazyInitializationException
        Specification<Unit> spec = Specification
                .where(UnitSpecifications.withInstitution())
                .and((root, query, cb) -> cb.equal(root.get("id"), id));

        Unit unit = unitRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id.toString()));

        // Validate tenant access
        validateTenantAccess(unit);

        return unit;
    }

    /**
     * Finds all units with optional filtering and pagination.
     * Automatically scoped to the current institution from TenantContext.
     *
     * @param active filter by active status (null returns ALL units - both active and inactive)
     * @param search search term for name or acronym (null for no search)
     * @param pageable pagination and sorting parameters
     * @return paginated list of units
     */
    public Page<Unit> findAll(Boolean active, String search, Pageable pageable) {
        UUID institutionId = getCurrentInstitutionId();

        LOGGER.debug("Finding units for institution {} with filters - active: {}, search: {}",
                     institutionId, active, search);

        Specification<Unit> spec = Specification
                .where(UnitSpecifications.withInstitution())
                .and(UnitSpecifications.belongsToInstitution(institutionId));

        if (active != null) {
            spec = spec.and(UnitSpecifications.hasActive(active));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(UnitSpecifications.searchByNameOrAcronym(search));
        }

        return unitRepository.findAll(spec, pageable);
    }

    /**
     * Creates a new unit.
     * Automatically assigned to the current institution from TenantContext.
     *
     * @param dto the unit data
     * @return the created Unit entity
     * @throws ResourceAlreadyExistsException if acronym already exists for this institution
     */
    @Transactional
    public Unit create(CreateUnitDTO dto) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Creating new unit '{}' for institution: {}", dto.getAcronym(), institutionId);

        // Normalize acronym to uppercase
        String normalizedAcronym = dto.getAcronym().toUpperCase().trim();

        // Validate acronym uniqueness within institution
        if (unitRepository.existsByAcronymAndInstitutionId(normalizedAcronym, institutionId)) {
            throw new ResourceAlreadyExistsException("Unit", "acronym", normalizedAcronym);
        }

        // Load institution
        Institution institution = institutionService.findById(institutionId);

        // Build entity
        Unit unit = Unit.builder()
                .institution(institution)
                .name(dto.getName())
                .acronym(normalizedAcronym)
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Unit saved = unitRepository.save(unit);
        LOGGER.info("Created unit with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Updates an existing unit.
     *
     * @param id the unit UUID
     * @param dto the updated data (only non-null fields are updated)
     * @return the updated Unit entity
     * @throws ResourceNotFoundException if not found
     * @throws UnauthorizedAccessException if not authorized
     */
    @Transactional
    public Unit update(UUID id, UpdateUnitDTO dto) {
        LOGGER.info("Updating unit: {}", id);

        Unit unit = findById(id); // Includes tenant validation

        // Update name if provided
        if (dto.getName() != null && !dto.getName().isBlank()) {
            unit.setName(dto.getName());
        }

        // Update description if provided
        if (dto.getDescription() != null) {
            unit.setDescription(dto.getDescription());
        }

        // Update active status if provided
        if (dto.getActive() != null) {
            unit.setActive(dto.getActive());
        }

        // Note: acronym is immutable - cannot be changed after creation

        Unit saved = unitRepository.save(unit);
        LOGGER.info("Updated unit: {}", id);

        return saved;
    }

    /**
     * Soft deletes a unit by setting its active status to false.
     *
     * @param id the unit UUID
     * @throws ResourceNotFoundException if not found
     * @throws UnauthorizedAccessException if not authorized
     */
    @Transactional
    public void delete(UUID id) {
        LOGGER.info("Soft deleting unit: {}", id);

        Unit unit = findById(id);
        unit.setActive(false);
        unitRepository.save(unit);

        LOGGER.info("Unit {} marked as inactive", id);
    }

    /**
     * Gets the current institution ID from TenantContext.
     *
     * @return the institution UUID
     * @throws BadRequestException if no institution context is set
     */
    private UUID getCurrentInstitutionId() {
        UUID institutionId = TenantContext.getCurrentInstitution();
        if (institutionId == null) {
            throw new BadRequestException("No institution context set. Please select an institution.");
        }
        return institutionId;
    }

    /**
     * Validates that the current user has access to the unit's institution.
     *
     * @param unit the unit to validate
     * @throws UnauthorizedAccessException if not authorized
     */
    private void validateTenantAccess(Unit unit) {
        UUID currentInstitutionId = getCurrentInstitutionId();

        if (!unit.getInstitution().getId().equals(currentInstitutionId)) {
            LOGGER.warn("Unauthorized access attempt to unit {} from institution {}",
                       unit.getId(), currentInstitutionId);
            throw new UnauthorizedAccessException(
                "You do not have permission to access this unit"
            );
        }
    }
}
