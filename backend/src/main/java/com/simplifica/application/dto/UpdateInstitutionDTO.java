package com.simplifica.application.dto;

import com.simplifica.domain.entity.InstitutionType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing institution.
 *
 * All fields are optional to support partial updates.
 * Only non-null fields will be applied to the institution.
 * Used in PUT/PATCH requests to update institutions.
 *
 * Validation notes:
 * - Name must be between 1 and 255 characters if provided
 * - Domain must be valid format if provided
 * - Type and active can be updated freely
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInstitutionDTO {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    private InstitutionType type;

    @Size(min = 1, max = 255, message = "Domain must be between 1 and 255 characters")
    @Pattern(
        regexp = "^[a-z0-9.-]+\\.[a-z]{2,}$",
        message = "Domain must be a valid format (e.g., example.com, ufms.br)"
    )
    private String domain;

    private Boolean active;
}
