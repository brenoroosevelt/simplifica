package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Unit entity operations.
 *
 * Provides CRUD operations and custom queries for managing units
 * with multi-tenant support.
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID>,
                                        JpaSpecificationExecutor<Unit> {

    /**
     * Finds a unit by ID and institution (tenant-aware).
     *
     * @param id the unit UUID
     * @param institutionId the institution UUID
     * @return an Optional containing the unit if found
     */
    Optional<Unit> findByIdAndInstitutionId(UUID id, UUID institutionId);

    /**
     * Finds all units for a specific institution with pagination.
     *
     * @param institutionId the institution UUID
     * @param pageable pagination information
     * @return a page of units
     */
    Page<Unit> findByInstitutionId(UUID institutionId, Pageable pageable);

    /**
     * Finds active units for a specific institution with pagination.
     *
     * @param institutionId the institution UUID
     * @param pageable pagination information
     * @return a page of active units
     */
    Page<Unit> findByInstitutionIdAndActiveTrue(UUID institutionId, Pageable pageable);

    /**
     * Checks if a unit with the given acronym exists for an institution.
     *
     * @param acronym the acronym to check
     * @param institutionId the institution UUID
     * @return true if a unit with this acronym exists
     */
    boolean existsByAcronymAndInstitutionId(String acronym, UUID institutionId);

    /**
     * Checks if a unit with the given acronym exists for an institution,
     * excluding a specific unit ID (useful for updates).
     *
     * @param acronym the acronym to check
     * @param institutionId the institution UUID
     * @param excludeId the ID to exclude from the check
     * @return true if a unit with this acronym exists
     */
    boolean existsByAcronymAndInstitutionIdAndIdNot(String acronym, UUID institutionId, UUID excludeId);
}
