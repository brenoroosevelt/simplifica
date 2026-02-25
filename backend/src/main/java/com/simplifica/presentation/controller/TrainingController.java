package com.simplifica.presentation.controller;

import com.simplifica.application.dto.CreateTrainingDTO;
import com.simplifica.application.dto.CreateTrainingVideoDTO;
import com.simplifica.application.dto.ReorderVideosDTO;
import com.simplifica.application.dto.TrainingDTO;
import com.simplifica.application.dto.TrainingVideoDTO;
import com.simplifica.application.dto.UpdateTrainingDTO;
import com.simplifica.application.dto.UpdateTrainingVideoDTO;
import com.simplifica.application.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing trainings (institutional training courses).
 *
 * Provides endpoints for CRUD operations on trainings with multi-tenant support.
 * All operations are automatically scoped to the current institution from the
 * X-Institution-Id header via TenantInterceptor.
 *
 * Access Control:
 * - ROLE_MANAGER (GESTOR): Can manage trainings in their institution
 * - ROLE_ADMIN: Can manage trainings in any institution (when context is set)
 *
 * Key Features:
 * - Training CRUD operations
 * - Cover image upload/delete
 * - Video playlist management (add, update, delete, reorder)
 * - Filtering and pagination
 * - Soft delete functionality
 */
@RestController
@RequestMapping("/trainings")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    /**
     * Lists all trainings with optional filters and pagination.
     * Automatically filtered by the current institution from TenantContext.
     *
     * Query Parameters:
     * - active: Filter by active status (optional)
     * - search: Search term for title and description (optional, case-insensitive)
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort field (default: createdAt)
     * - direction: Sort direction (default: DESC)
     *
     * @param active filter by active status (optional)
     * @param search search term for title and description (optional)
     * @param pageable pagination and sorting parameters
     * @return paginated list of trainings
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<TrainingDTO>> listTrainings(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TrainingDTO> trainings = trainingService.findAll(active, search, pageable);
        return ResponseEntity.ok(trainings);
    }

    /**
     * Gets a single training by ID.
     *
     * @param id the training UUID
     * @return the training DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingDTO> getTraining(@PathVariable UUID id) {
        TrainingDTO training = trainingService.findById(id);
        return ResponseEntity.ok(training);
    }

    /**
     * Creates a new training.
     * Automatically assigned to the current institution from TenantContext.
     *
     * Request Body:
     * - title: Training title (required, max 255 chars)
     * - description: Training description (optional, max 5000 chars)
     * - videos: List of videos (required, at least 1)
     *   - title: Video title (required, max 255 chars)
     *   - youtubeUrl: YouTube URL (required, max 512 chars, valid format)
     *   - durationMinutes: Duration in minutes (optional, non-negative)
     *   - orderIndex: Order position (required, non-negative, unique)
     * - active: Active flag (optional, default: true)
     *
     * @param dto the training data
     * @return the created training DTO with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingDTO> createTraining(@Valid @RequestBody CreateTrainingDTO dto) {
        TrainingDTO training = trainingService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(training);
    }

    /**
     * Updates an existing training.
     *
     * Request Body:
     * - title: Training title (required, max 255 chars)
     * - description: Training description (optional, max 5000 chars)
     * - active: Active flag (optional)
     *
     * @param id the training UUID
     * @param dto the updated training data
     * @return the updated training DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingDTO> updateTraining(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTrainingDTO dto) {
        TrainingDTO training = trainingService.update(id, dto);
        return ResponseEntity.ok(training);
    }

    /**
     * Deletes a training (soft delete by setting active = false).
     *
     * @param id the training UUID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteTraining(@PathVariable UUID id) {
        trainingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads a cover image for a training.
     *
     * Form Data:
     * - file: Image file (required, max 5MB, jpg/jpeg/png/gif/webp)
     *
     * @param id the training UUID
     * @param file the image file
     * @return the updated training DTO
     */
    @PostMapping(value = "/{id}/cover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingDTO> uploadCoverImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        TrainingDTO training = trainingService.uploadCoverImage(id, file);
        return ResponseEntity.ok(training);
    }

    /**
     * Deletes the cover image of a training.
     *
     * @param id the training UUID
     * @return the updated training DTO
     */
    @DeleteMapping("/{id}/cover-image")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingDTO> deleteCoverImage(@PathVariable UUID id) {
        TrainingDTO training = trainingService.deleteCoverImage(id);
        return ResponseEntity.ok(training);
    }

    /**
     * Adds a video to a training.
     *
     * Request Body:
     * - title: Video title (required, max 255 chars)
     * - youtubeUrl: YouTube URL (required, max 512 chars, valid format)
     * - durationMinutes: Duration in minutes (optional, non-negative)
     * - orderIndex: Order position (required, non-negative, unique)
     *
     * @param trainingId the training UUID
     * @param dto the video data
     * @return the created video DTO with HTTP 201 status
     */
    @PostMapping("/{trainingId}/videos")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingVideoDTO> addVideo(
            @PathVariable UUID trainingId,
            @Valid @RequestBody CreateTrainingVideoDTO dto) {
        TrainingVideoDTO video = trainingService.addVideo(trainingId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(video);
    }

    /**
     * Updates a video.
     *
     * Request Body:
     * - title: Video title (required, max 255 chars)
     * - youtubeUrl: YouTube URL (required, max 512 chars, valid format)
     * - durationMinutes: Duration in minutes (optional, non-negative)
     *
     * @param trainingId the training UUID
     * @param videoId the video UUID
     * @param dto the updated video data
     * @return the updated video DTO
     */
    @PutMapping("/{trainingId}/videos/{videoId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TrainingVideoDTO> updateVideo(
            @PathVariable UUID trainingId,
            @PathVariable UUID videoId,
            @Valid @RequestBody UpdateTrainingVideoDTO dto) {
        TrainingVideoDTO video = trainingService.updateVideo(trainingId, videoId, dto);
        return ResponseEntity.ok(video);
    }

    /**
     * Deletes a video from a training.
     *
     * @param trainingId the training UUID
     * @param videoId the video UUID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{trainingId}/videos/{videoId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteVideo(
            @PathVariable UUID trainingId,
            @PathVariable UUID videoId) {
        trainingService.deleteVideo(trainingId, videoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorders videos in a training.
     *
     * Request Body:
     * - videoIds: List of video UUIDs in the desired order (required, all videos must be included)
     *
     * @param trainingId the training UUID
     * @param dto the reorder data
     * @return list of reordered video DTOs
     */
    @PutMapping("/{trainingId}/videos/reorder")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<TrainingVideoDTO>> reorderVideos(
            @PathVariable UUID trainingId,
            @Valid @RequestBody ReorderVideosDTO dto) {
        List<TrainingVideoDTO> videos = trainingService.reorderVideos(trainingId, dto);
        return ResponseEntity.ok(videos);
    }
}
