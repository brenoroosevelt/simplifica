package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new unit.
 *
 * Contains validation constraints to ensure data integrity.
 * Institution is not included as it's automatically set from the tenant context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUnitDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Acronym is required")
    @Size(max = 50, message = "Acronym must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Acronym must contain only letters, numbers, and hyphens")
    private String acronym;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Builder.Default
    private Boolean active = true;
}
