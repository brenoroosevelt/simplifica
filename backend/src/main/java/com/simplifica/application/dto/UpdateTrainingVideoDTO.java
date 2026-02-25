package com.simplifica.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing training video.
 *
 * Contains validation constraints to ensure data integrity.
 * Order index is updated through a separate reorder endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrainingVideoDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "YouTube URL is required")
    @Size(max = 512, message = "YouTube URL must not exceed 512 characters")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)([a-zA-Z0-9_-]{11}).*$",
            message = "Invalid YouTube URL format"
    )
    private String youtubeUrl;

    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @Min(value = 0, message = "Duration must be non-negative")
    private Integer durationMinutes;
}
