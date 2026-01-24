package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Institution entity operations.
 *
 * Provides CRUD operations and custom queries for managing institutions
 * in the multi-tenant system.
 */
@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID>, JpaSpecificationExecutor<Institution> {

    /**
     * Finds an institution by its acronym.
     *
     * @param acronym the acronym to search for
     * @return an Optional containing the institution if found
     */
    Optional<Institution> findByAcronym(String acronym);

    /**
     * Finds an institution by its domain.
     *
     * @param domain the domain to search for
     * @return an Optional containing the institution if found
     */
    Optional<Institution> findByDomain(String domain);

    /**
     * Checks if an institution with the given acronym exists.
     *
     * @param acronym the acronym to check
     * @return true if an institution with this acronym exists, false otherwise
     */
    boolean existsByAcronym(String acronym);

    /**
     * Checks if an institution with the given domain exists.
     *
     * @param domain the domain to check
     * @return true if an institution with this domain exists, false otherwise
     */
    boolean existsByDomain(String domain);

    /**
     * Finds all active institutions with pagination.
     *
     * @param pageable pagination information
     * @return a page of active institutions
     */
    Page<Institution> findByActiveTrue(Pageable pageable);

    /**
     * Finds institutions by type with pagination.
     *
     * @param type the institution type
     * @param pageable pagination information
     * @return a page of institutions of the specified type
     */
    Page<Institution> findByType(InstitutionType type, Pageable pageable);

    /**
     * Finds institutions by type and active status with pagination.
     *
     * @param type the institution type
     * @param active the active status
     * @param pageable pagination information
     * @return a page of institutions matching the criteria
     */
    Page<Institution> findByTypeAndActive(InstitutionType type, Boolean active, Pageable pageable);

}
