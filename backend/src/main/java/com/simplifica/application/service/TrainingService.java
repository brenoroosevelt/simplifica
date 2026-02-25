package com.simplifica.application.service;

import com.simplifica.application.dto.CreateTrainingDTO;
import com.simplifica.application.dto.CreateTrainingVideoDTO;
import com.simplifica.application.dto.ReorderVideosDTO;
import com.simplifica.application.dto.TrainingDTO;
import com.simplifica.application.dto.TrainingVideoDTO;
import com.simplifica.application.dto.UpdateTrainingDTO;
import com.simplifica.application.dto.UpdateTrainingVideoDTO;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.Training;
import com.simplifica.domain.entity.TrainingVideo;
import com.simplifica.infrastructure.repository.TrainingRepository;
import com.simplifica.infrastructure.repository.TrainingSpecifications;
import com.simplifica.infrastructure.repository.TrainingVideoRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing trainings (institutional training courses).
 *
 * Provides business logic for CRUD operations on trainings with strict multi-tenant
 * isolation. All operations are scoped to the current institution from TenantContext.
 *
 * CRITICAL SECURITY:
 * - All queries MUST filter by institution_id from TenantContext
 * - validateTenantAccess MUST be called before any modification
 * - Never expose trainings from other institutions
 *
 * Key Features:
 * - Multi-tenant isolation with institution-scoped queries
 * - Comprehensive filtering (active, search)
 * - Video playlist management (add, update, remove, reorder)
 * - Cover image upload support
 * - Soft delete (active flag)
 * - Validation: minimum 1 video per training
 */
@Service
@Transactional(readOnly = true)
public class TrainingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingVideoRepository trainingVideoRepository;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Gets the current institution ID from TenantContext.
     * CRITICAL: This should always be set by the request interceptor.
     *
     * @return the current institution ID
     * @throws BadRequestException if no institution context is set
     */
    private UUID getCurrentInstitutionId() {
        UUID institutionId = TenantContext.getCurrentInstitution();
        if (institutionId == null) {
            LOGGER.error("SECURITY ALERT: TenantContext is null. Interceptor may have failed to set institution context.");
            throw new BadRequestException("No institution context set. Please select an institution.");
        }
        return institutionId;
    }

    /**
     * Validates that the training belongs to the current institution.
     * CRITICAL: Must be called before any modification operation.
     *
     * @param training the training to validate
     * @throws UnauthorizedAccessException if the training belongs to a different institution
     */
    private void validateTenantAccess(Training training) {
        UUID currentInstitutionId = getCurrentInstitutionId();
        if (!training.getInstitution().getId().equals(currentInstitutionId)) {
            LOGGER.warn("Unauthorized access attempt to training {} from institution {}",
                    training.getId(), currentInstitutionId);
            throw new UnauthorizedAccessException(
                    "You do not have permission to access this training"
            );
        }
    }

    /**
     * Finds all trainings for the current institution with optional filtering and pagination.
     *
     * @param active filter by active status (null for all)
     * @param search search term for title and description (null for no search)
     * @param pageable pagination and sorting parameters
     * @return paginated list of training DTOs
     */
    public Page<TrainingDTO> findAll(Boolean active, String search, Pageable pageable) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.debug("Finding trainings for institution {} with filters - active: {}, search: {}",
                institutionId, active, search);

        // Build specification with MANDATORY institution filter
        Specification<Training> spec = Specification
                .where(TrainingSpecifications.withRelations())
                .and(TrainingSpecifications.belongsToInstitution(institutionId));

        if (active != null) {
            spec = spec.and(TrainingSpecifications.hasActive(active));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(TrainingSpecifications.searchByMultipleFields(search));
        }

        Page<Training> trainings = trainingRepository.findAll(spec, pageable);
        return trainings.map(TrainingDTO::fromEntity);
    }

    /**
     * Finds a training by ID, ensuring it belongs to the current institution.
     *
     * @param id the training UUID
     * @return the training DTO
     * @throws ResourceNotFoundException if the training is not found
     * @throws UnauthorizedAccessException if the training belongs to another institution
     */
    public TrainingDTO findById(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.debug("Finding training {} for institution {}", id, institutionId);

        Training training = trainingRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + id));

        return TrainingDTO.fromEntity(training);
    }

    /**
     * Creates a new training.
     *
     * @param createDTO the training data
     * @return the created training DTO
     * @throws ResourceAlreadyExistsException if a training with the same title already exists
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public TrainingDTO create(CreateTrainingDTO createDTO) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Creating training '{}' for institution {}", createDTO.getTitle(), institutionId);

        // Validate title uniqueness
        if (trainingRepository.existsByTitleAndInstitutionId(createDTO.getTitle(), institutionId)) {
            throw new ResourceAlreadyExistsException(
                    "A training with title '" + createDTO.getTitle() + "' already exists for this institution"
            );
        }

        // Validate at least one video
        if (createDTO.getVideos() == null || createDTO.getVideos().isEmpty()) {
            throw new BadRequestException("At least one video is required for a training");
        }

        // Validate unique order indexes
        validateUniqueOrderIndexes(createDTO.getVideos().stream()
                .map(CreateTrainingVideoDTO::getOrderIndex)
                .collect(Collectors.toList()));

        // Get institution
        Institution institution = institutionService.findById(institutionId);

        // Create training entity
        Training training = Training.builder()
                .institution(institution)
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .content(createDTO.getContent())
                .active(createDTO.getActive() != null ? createDTO.getActive() : true)
                .build();

        // Create video entities
        for (CreateTrainingVideoDTO videoDTO : createDTO.getVideos()) {
            TrainingVideo video = TrainingVideo.builder()
                    .training(training)
                    .title(videoDTO.getTitle())
                    .youtubeUrl(videoDTO.getYoutubeUrl())
                    .content(videoDTO.getContent())
                    .durationMinutes(videoDTO.getDurationMinutes())
                    .orderIndex(videoDTO.getOrderIndex())
                    .build();
            training.addVideo(video);
        }

        // Save and return
        Training savedTraining = trainingRepository.save(training);
        LOGGER.info("Training created successfully with id: {}", savedTraining.getId());

        return TrainingDTO.fromEntity(savedTraining);
    }

    /**
     * Updates an existing training.
     *
     * @param id the training UUID
     * @param updateDTO the updated training data
     * @return the updated training DTO
     * @throws ResourceNotFoundException if the training is not found
     * @throws ResourceAlreadyExistsException if the title conflicts with another training
     */
    @Transactional
    public TrainingDTO update(UUID id, UpdateTrainingDTO updateDTO) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Updating training {} for institution {}", id, institutionId);

        Training training = trainingRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + id));

        validateTenantAccess(training);

        // Validate title uniqueness (excluding this training)
        if (trainingRepository.existsByTitleAndInstitutionIdAndIdNot(updateDTO.getTitle(), institutionId, id)) {
            throw new ResourceAlreadyExistsException(
                    "A training with title '" + updateDTO.getTitle() + "' already exists for this institution"
            );
        }

        // Update fields
        training.setTitle(updateDTO.getTitle());
        training.setDescription(updateDTO.getDescription());
        training.setContent(updateDTO.getContent());
        if (updateDTO.getActive() != null) {
            training.setActive(updateDTO.getActive());
        }

        Training updatedTraining = trainingRepository.save(training);
        LOGGER.info("Training {} updated successfully", id);

        return TrainingDTO.fromEntity(updatedTraining);
    }

    /**
     * Deletes a training (soft delete by setting active = false).
     *
     * @param id the training UUID
     * @throws ResourceNotFoundException if the training is not found
     */
    @Transactional
    public void delete(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Deleting training {} for institution {}", id, institutionId);

        Training training = trainingRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + id));

        validateTenantAccess(training);

        // Soft delete
        training.setActive(false);
        trainingRepository.save(training);

        // Delete cover image if exists
        if (training.getCoverImageUrl() != null) {
            fileStorageService.deleteFile(training.getCoverImageUrl());
        }

        LOGGER.info("Training {} deleted successfully", id);
    }

    /**
     * Uploads a cover image for a training.
     *
     * @param id the training UUID
     * @param file the image file
     * @return the updated training DTO
     * @throws ResourceNotFoundException if the training is not found
     * @throws BadRequestException if file validation fails
     */
    @Transactional
    public TrainingDTO uploadCoverImage(UUID id, MultipartFile file) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Uploading cover image for training {} for institution {}", id, institutionId);

        Training training = trainingRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + id));

        validateTenantAccess(training);

        // Delete old cover image if exists
        if (training.getCoverImageUrl() != null) {
            fileStorageService.deleteFile(training.getCoverImageUrl());
        }

        // Upload new cover image
        FileStorageService.FileUploadResult uploadResult = fileStorageService.storeImage(file, "trainings");
        training.setCoverImageUrl(uploadResult.getFileUrl());

        Training updatedTraining = trainingRepository.save(training);
        LOGGER.info("Cover image uploaded successfully for training {}", id);

        return TrainingDTO.fromEntity(updatedTraining);
    }

    /**
     * Deletes the cover image of a training.
     *
     * @param id the training UUID
     * @return the updated training DTO
     * @throws ResourceNotFoundException if the training is not found
     */
    @Transactional
    public TrainingDTO deleteCoverImage(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Deleting cover image for training {} for institution {}", id, institutionId);

        Training training = trainingRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + id));

        validateTenantAccess(training);

        if (training.getCoverImageUrl() != null) {
            fileStorageService.deleteFile(training.getCoverImageUrl());
            training.setCoverImageUrl(null);
            trainingRepository.save(training);
        }

        LOGGER.info("Cover image deleted successfully for training {}", id);

        return TrainingDTO.fromEntity(training);
    }

    /**
     * Adds a video to a training.
     *
     * @param trainingId the training UUID
     * @param videoDTO the video data
     * @return the created video DTO
     * @throws ResourceNotFoundException if the training is not found
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public TrainingVideoDTO addVideo(UUID trainingId, CreateTrainingVideoDTO videoDTO) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Adding video to training {} for institution {}", trainingId, institutionId);

        Training training = trainingRepository.findByIdAndInstitutionId(trainingId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + trainingId));

        validateTenantAccess(training);

        // Validate order index uniqueness
        if (trainingVideoRepository.existsByTrainingIdAndOrderIndex(trainingId, videoDTO.getOrderIndex())) {
            throw new BadRequestException(
                    "A video with order index " + videoDTO.getOrderIndex() + " already exists for this training"
            );
        }

        // Create video
        TrainingVideo video = TrainingVideo.builder()
                .training(training)
                .title(videoDTO.getTitle())
                .youtubeUrl(videoDTO.getYoutubeUrl())
                .content(videoDTO.getContent())
                .durationMinutes(videoDTO.getDurationMinutes())
                .orderIndex(videoDTO.getOrderIndex())
                .build();

        TrainingVideo savedVideo = trainingVideoRepository.save(video);
        LOGGER.info("Video added successfully to training {}", trainingId);

        return TrainingVideoDTO.fromEntity(savedVideo);
    }

    /**
     * Updates a video.
     *
     * @param trainingId the training UUID
     * @param videoId the video UUID
     * @param updateDTO the updated video data
     * @return the updated video DTO
     * @throws ResourceNotFoundException if the video is not found
     */
    @Transactional
    public TrainingVideoDTO updateVideo(UUID trainingId, UUID videoId, UpdateTrainingVideoDTO updateDTO) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Updating video {} for training {} for institution {}", videoId, trainingId, institutionId);

        // Verify training ownership
        Training training = trainingRepository.findByIdAndInstitutionId(trainingId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + trainingId));

        validateTenantAccess(training);

        // Find video
        TrainingVideo video = trainingVideoRepository.findByIdAndTrainingId(videoId, trainingId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));

        // Update fields
        video.setTitle(updateDTO.getTitle());
        video.setYoutubeUrl(updateDTO.getYoutubeUrl());
        video.setContent(updateDTO.getContent());
        video.setDurationMinutes(updateDTO.getDurationMinutes());

        TrainingVideo updatedVideo = trainingVideoRepository.save(video);
        LOGGER.info("Video {} updated successfully", videoId);

        return TrainingVideoDTO.fromEntity(updatedVideo);
    }

    /**
     * Deletes a video from a training.
     *
     * @param trainingId the training UUID
     * @param videoId the video UUID
     * @throws ResourceNotFoundException if the video is not found
     * @throws BadRequestException if this is the last video
     */
    @Transactional
    public void deleteVideo(UUID trainingId, UUID videoId) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Deleting video {} from training {} for institution {}", videoId, trainingId, institutionId);

        // Verify training ownership
        Training training = trainingRepository.findByIdAndInstitutionId(trainingId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + trainingId));

        validateTenantAccess(training);

        // Find video
        TrainingVideo video = trainingVideoRepository.findByIdAndTrainingId(videoId, trainingId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));

        // Validate at least one video remains
        long videoCount = trainingVideoRepository.countByTrainingId(trainingId);
        if (videoCount <= 1) {
            throw new BadRequestException("Cannot delete the last video. A training must have at least one video.");
        }

        trainingVideoRepository.delete(video);
        LOGGER.info("Video {} deleted successfully", videoId);
    }

    /**
     * Reorders videos in a training.
     *
     * @param trainingId the training UUID
     * @param reorderDTO the new order (list of video IDs)
     * @return list of reordered video DTOs
     * @throws ResourceNotFoundException if the training or videos are not found
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public List<TrainingVideoDTO> reorderVideos(UUID trainingId, ReorderVideosDTO reorderDTO) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Reordering videos for training {} for institution {}", trainingId, institutionId);

        // Verify training ownership
        Training training = trainingRepository.findByIdAndInstitutionId(trainingId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + trainingId));

        validateTenantAccess(training);

        // Get all videos for this training
        List<TrainingVideo> videos = trainingVideoRepository.findByTrainingIdOrderByOrderIndexAsc(trainingId);

        // Validate that all videos are included
        if (videos.size() != reorderDTO.getVideoIds().size()) {
            throw new BadRequestException("All videos must be included in the reorder operation");
        }

        // Validate that all video IDs exist
        for (UUID videoId : reorderDTO.getVideoIds()) {
            boolean exists = videos.stream().anyMatch(v -> v.getId().equals(videoId));
            if (!exists) {
                throw new BadRequestException("Video not found with id: " + videoId);
            }
        }

        // Update order indexes
        for (int i = 0; i < reorderDTO.getVideoIds().size(); i++) {
            UUID videoId = reorderDTO.getVideoIds().get(i);
            TrainingVideo video = videos.stream()
                    .filter(v -> v.getId().equals(videoId))
                    .findFirst()
                    .orElseThrow();
            video.setOrderIndex(i);
        }

        // Save all videos
        List<TrainingVideo> reorderedVideos = trainingVideoRepository.saveAll(videos);
        LOGGER.info("Videos reordered successfully for training {}", trainingId);

        return reorderedVideos.stream()
                .map(TrainingVideoDTO::fromEntity)
                .sorted((v1, v2) -> v1.getOrderIndex().compareTo(v2.getOrderIndex()))
                .collect(Collectors.toList());
    }

    /**
     * Validates that order indexes are unique.
     *
     * @param orderIndexes list of order indexes
     * @throws BadRequestException if duplicates are found
     */
    private void validateUniqueOrderIndexes(List<Integer> orderIndexes) {
        long uniqueCount = orderIndexes.stream().distinct().count();
        if (uniqueCount != orderIndexes.size()) {
            throw new BadRequestException("Order indexes must be unique");
        }
    }
}
