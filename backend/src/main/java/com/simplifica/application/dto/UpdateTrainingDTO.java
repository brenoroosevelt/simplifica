package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing training.
 *
 * Contains validation constraints to ensure data integrity.
 * Videos are updated through separate endpoints for better control.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrainingDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    private String trainingType;

    @Size(max = 1024, message = "External link must not exceed 1024 characters")
    private String externalLink;

    private Boolean active;
}
