package com.simplifica.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for creating a new training.
 *
 * Contains validation constraints to ensure data integrity.
 * Institution is not included as it's automatically set from the tenant context.
 * At least one video is required for a valid training.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    private String trainingType; // "VIDEO_SEQUENCE" or "LINK"

    @Size(max = 1024, message = "External link must not exceed 1024 characters")
    private String externalLink;

    @Valid
    private List<CreateTrainingVideoDTO> videos;

    @Builder.Default
    private Boolean active = true;
}
