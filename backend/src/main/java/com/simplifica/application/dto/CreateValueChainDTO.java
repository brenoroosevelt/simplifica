package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new value chain.
 *
 * Contains validation constraints to ensure data integrity.
 * Used in POST requests to create new value chains.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateValueChainDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Builder.Default
    private Boolean active = true;
}
