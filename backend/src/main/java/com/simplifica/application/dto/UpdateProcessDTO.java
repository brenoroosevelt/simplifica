package com.simplifica.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * Data Transfer Object for updating an existing process.
 *
 * All fields are optional - only provided fields will be updated.
 * Partial updates are supported.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcessDTO {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private UUID valueChainId;

    private UUID responsibleUnitId;

    private UUID directUnitId;

    private Boolean isCritical;

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

    private Boolean active;
}
