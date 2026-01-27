package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * Data Transfer Object for creating a new process.
 *
 * Contains validation constraints to ensure data integrity.
 * Institution is not included as it's automatically set from the tenant context.
 * All status and URL fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProcessDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private UUID valueChainId;

    private UUID responsibleUnitId;

    private UUID directUnitId;

    @Builder.Default
    private Boolean isCritical = false;

    // Documentation fields
    private String documentationStatus;

    @Size(max = 1024, message = "Documentation URL must not exceed 1024 characters")
    @URL(message = "Documentation URL must be a valid URL")
    private String documentationUrl;

    // External guidance fields
    private String externalGuidanceStatus;

    @Size(max = 1024, message = "External guidance URL must not exceed 1024 characters")
    @URL(message = "External guidance URL must be a valid URL")
    private String externalGuidanceUrl;

    // Risk management fields
    private String riskManagementStatus;

    @Size(max = 1024, message = "Risk management URL must not exceed 1024 characters")
    @URL(message = "Risk management URL must be a valid URL")
    private String riskManagementUrl;

    // Mapping status
    private String mappingStatus;

    @Builder.Default
    private Boolean active = true;
}
