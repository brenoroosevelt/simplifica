package com.simplifica.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing unit.
 *
 * All fields are optional - only provided fields will be updated.
 * Acronym cannot be changed after creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUnitDTO {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean active;

    // Note: acronym is intentionally NOT included (immutable after creation)
}
