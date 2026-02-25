package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.TrainingVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TrainingVideo entity operations.
 *
 * Provides CRUD operations and custom queries for managing training videos.
 */
@Repository
public interface TrainingVideoRepository extends JpaRepository<TrainingVideo, UUID> {

    /**
     * Finds all videos for a training, ordered by order_index.
     *
     * @param trainingId the training UUID
     * @return list of videos in order
     */
    List<TrainingVideo> findByTrainingIdOrderByOrderIndexAsc(UUID trainingId);

    /**
     * Finds a video by ID and training ID (for tenant verification).
     *
     * @param id the video UUID
     * @param trainingId the training UUID
     * @return an Optional containing the video if found
     */
    Optional<TrainingVideo> findByIdAndTrainingId(UUID id, UUID trainingId);

    /**
     * Checks if a video with the given order index exists for a training.
     *
     * @param trainingId the training UUID
     * @param orderIndex the order index to check
     * @return true if a video with this order index exists
     */
    boolean existsByTrainingIdAndOrderIndex(UUID trainingId, Integer orderIndex);

    /**
     * Checks if a video with the given order index exists for a training,
     * excluding a specific video ID (useful for updates).
     *
     * @param trainingId the training UUID
     * @param orderIndex the order index to check
     * @param excludeId the ID to exclude from the check
     * @return true if a video with this order index exists
     */
    boolean existsByTrainingIdAndOrderIndexAndIdNot(UUID trainingId, Integer orderIndex, UUID excludeId);

    /**
     * Counts videos for a training.
     *
     * @param trainingId the training UUID
     * @return number of videos
     */
    long countByTrainingId(UUID trainingId);

    /**
     * Deletes all videos for a training.
     *
     * @param trainingId the training UUID
     */
    void deleteByTrainingId(UUID trainingId);

    /**
     * Finds a video by ID that belongs to a training owned by an institution (for tenant verification).
     *
     * @param videoId the video UUID
     * @param institutionId the institution UUID
     * @return an Optional containing the video if found
     */
    @Query("SELECT v FROM TrainingVideo v " +
           "JOIN v.training t " +
           "WHERE v.id = :videoId AND t.institution.id = :institutionId")
    Optional<TrainingVideo> findByIdAndInstitutionId(@Param("videoId") UUID videoId,
                                                      @Param("institutionId") UUID institutionId);
}
