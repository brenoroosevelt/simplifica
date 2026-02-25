package com.simplifica.application.dto;

import com.simplifica.domain.entity.Training;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Training entity.
 *
 * Used to transfer training data between layers without exposing
 * the entity directly. Includes all training information with videos
 * and computed statistics like video count and total duration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {

    private UUID id;
    private UUID institutionId;
    private String institutionName;
    private String institutionAcronym;
    private String title;
    private String description;
    private String content;
    private String coverImageUrl;
    private String coverImageThumbnailUrl;
    private List<TrainingVideoDTO> videos;
    private Integer videoCount;
    private Integer totalDurationMinutes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Training entity to a DTO.
     *
     * @param training the training entity
     * @return the training DTO, or null if the input is null
     */
    public static TrainingDTO fromEntity(Training training) {
        if (training == null) {
            return null;
        }

        // Calculate thumbnail URL from cover image URL
        String coverImageUrl = training.getCoverImageUrl();
        String thumbnailUrl = null;
        if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
            // Insert "/thumbnails/" before the filename
            // Example: /uploads/trainings/file.jpg -> /uploads/trainings/thumbnails/file.jpg
            int lastSlashIndex = coverImageUrl.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                String basePath = coverImageUrl.substring(0, lastSlashIndex);
                String filename = coverImageUrl.substring(lastSlashIndex + 1);
                thumbnailUrl = basePath + "/thumbnails/" + filename;
            }
        }

        TrainingDTOBuilder builder = TrainingDTO.builder()
                .id(training.getId())
                .institutionId(training.getInstitution().getId())
                .institutionName(training.getInstitution().getName())
                .institutionAcronym(training.getInstitution().getAcronym())
                .title(training.getTitle())
                .description(training.getDescription())
                .content(training.getContent())
                .coverImageUrl(coverImageUrl)
                .coverImageThumbnailUrl(thumbnailUrl)
                .videoCount(training.getVideoCount())
                .totalDurationMinutes(training.getTotalDurationMinutes())
                .active(training.isActive())
                .createdAt(training.getCreatedAt())
                .updatedAt(training.getUpdatedAt());

        // Convert videos to DTOs
        if (training.getVideos() != null) {
            List<TrainingVideoDTO> videoDTOs = training.getVideos().stream()
                    .map(TrainingVideoDTO::fromEntity)
                    .sorted((v1, v2) -> v1.getOrderIndex().compareTo(v2.getOrderIndex()))
                    .collect(Collectors.toList());
            builder.videos(videoDTOs);
        }

        return builder.build();
    }
}
