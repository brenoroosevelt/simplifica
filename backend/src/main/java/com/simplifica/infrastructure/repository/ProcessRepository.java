package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Process entity operations.
 *
 * Provides CRUD operations and custom queries for managing processes
 * with multi-tenant support.
 */
@Repository
public interface ProcessRepository extends JpaRepository<Process, UUID>,
                                           JpaSpecificationExecutor<Process> {

    /**
     * Finds a process by ID and institution (tenant-aware).
     *
     * @param id the process UUID
     * @param institutionId the institution UUID
     * @return an Optional containing the process if found
     */
    Optional<Process> findByIdAndInstitutionId(UUID id, UUID institutionId);

    /**
     * Checks if a process with the given name exists for an institution.
     *
     * @param name the name to check
     * @param institutionId the institution UUID
     * @return true if a process with this name exists
     */
    boolean existsByNameAndInstitutionId(String name, UUID institutionId);

    /**
     * Checks if a process with the given name exists for an institution,
     * excluding a specific process ID (useful for updates).
     *
     * @param name the name to check
     * @param institutionId the institution UUID
     * @param excludeId the ID to exclude from the check
     * @return true if a process with this name exists
     */
    boolean existsByNameAndInstitutionIdAndIdNot(String name, UUID institutionId, UUID excludeId);
}
