package com.simplifica.application.dto;

import com.simplifica.domain.entity.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new institution.
 *
 * Contains validation constraints to ensure data integrity.
 * Used in POST requests to create new institutions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstitutionDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Acronym is required")
    @Size(max = 50, message = "Acronym must not exceed 50 characters")
    @Pattern(
        regexp = "^[A-Z0-9]+$",
        message = "Acronym must contain only uppercase letters and numbers"
    )
    private String acronym;

    @NotNull(message = "Type is required")
    private InstitutionType type;

    @Size(max = 255, message = "Domain must not exceed 255 characters")
    @Pattern(
        regexp = "^[a-z0-9.-]+\\.[a-z]{2,}$",
        message = "Domain must be a valid format (e.g., example.com, ufms.br)"
    )
    private String domain;

    @Builder.Default
    private Boolean active = true;
}
