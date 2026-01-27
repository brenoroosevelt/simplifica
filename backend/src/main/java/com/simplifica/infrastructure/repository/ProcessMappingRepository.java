package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.ProcessMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ProcessMapping entity operations.
 *
 * Provides CRUD operations for managing process mapping files
 * (HTML files uploaded for process visualization).
 */
@Repository
public interface ProcessMappingRepository extends JpaRepository<ProcessMapping, UUID> {

    /**
     * Finds all mappings for a specific process, ordered by upload date (newest first).
     *
     * @param processId the process UUID
     * @return a list of process mappings
     */
    List<ProcessMapping> findByProcessIdOrderByUploadedAtDesc(UUID processId);

    /**
     * Deletes all mappings for a specific process.
     *
     * @param processId the process UUID
     */
    void deleteByProcessId(UUID processId);
}
