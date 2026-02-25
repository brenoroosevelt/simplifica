package com.simplifica.application.dto;

import com.simplifica.domain.entity.TrainingVideo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for TrainingVideo entity.
 *
 * Used to transfer video data between layers. Includes
 * the extracted YouTube video ID for embed usage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingVideoDTO {

    private UUID id;
    private UUID trainingId;
    private String title;
    private String youtubeUrl;
    private String videoId;
    private String content;
    private Integer durationMinutes;
    private Integer orderIndex;
    private LocalDateTime createdAt;

    /**
     * Converts a TrainingVideo entity to a DTO.
     *
     * @param video the video entity
     * @return the video DTO, or null if the input is null
     */
    public static TrainingVideoDTO fromEntity(TrainingVideo video) {
        if (video == null) {
            return null;
        }

        return TrainingVideoDTO.builder()
                .id(video.getId())
                .trainingId(video.getTraining().getId())
                .title(video.getTitle())
                .youtubeUrl(video.getYoutubeUrl())
                .videoId(video.extractVideoId())
                .content(video.getContent())
                .durationMinutes(video.getDurationMinutes())
                .orderIndex(video.getOrderIndex())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
