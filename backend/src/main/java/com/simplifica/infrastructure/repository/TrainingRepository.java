package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Training entity operations.
 *
 * Provides CRUD operations and custom queries for managing trainings
 * with multi-tenant support.
 */
@Repository
public interface TrainingRepository extends JpaRepository<Training, UUID>,
                                            JpaSpecificationExecutor<Training> {

    /**
     * Finds a training by ID and institution (tenant-aware).
     *
     * @param id the training UUID
     * @param institutionId the institution UUID
     * @return an Optional containing the training if found
     */
    Optional<Training> findByIdAndInstitutionId(UUID id, UUID institutionId);

    /**
     * Checks if a training with the given title exists for an institution.
     *
     * @param title the title to check
     * @param institutionId the institution UUID
     * @return true if a training with this title exists
     */
    boolean existsByTitleAndInstitutionId(String title, UUID institutionId);

    /**
     * Checks if a training with the given title exists for an institution,
     * excluding a specific training ID (useful for updates).
     *
     * @param title the title to check
     * @param institutionId the institution UUID
     * @param excludeId the ID to exclude from the check
     * @return true if a training with this title exists
     */
    boolean existsByTitleAndInstitutionIdAndIdNot(String title, UUID institutionId, UUID excludeId);
}
