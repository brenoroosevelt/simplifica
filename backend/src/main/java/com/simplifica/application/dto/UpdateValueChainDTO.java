package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing value chain.
 *
 * All fields are optional to support partial updates.
 * Only non-null fields will be applied to the value chain.
 * Used in PUT/PATCH requests to update value chains.
 *
 * Validation Rules:
 * - name: If provided, must be non-blank and 1-255 characters
 * - description: If provided, must not exceed 5000 characters
 * - active: If provided, must be boolean
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateValueChainDTO {

    @NotBlank(message = "Name cannot be blank if provided")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean active;
}
