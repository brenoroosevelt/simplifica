package com.simplifica.application.dto;

import com.simplifica.domain.entity.Process;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Process entity.
 *
 * Used to transfer process data between layers without exposing
 * the entity directly. Includes all process information with related
 * entity names and status as strings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDTO {

    private UUID id;
    private UUID institutionId;
    private String institutionName;
    private String institutionAcronym;
    private String name;
    private String description;
    private Boolean isCritical;

    // Value chain relationship
    private UUID valueChainId;
    private String valueChainName;

    // Responsible unit relationship
    private UUID responsibleUnitId;
    private String responsibleUnitName;
    private String responsibleUnitAcronym;

    // Direct unit relationship
    private UUID directUnitId;
    private String directUnitName;
    private String directUnitAcronym;

    // Documentation status
    private String documentationStatus;
    private String documentationUrl;

    // External guidance status
    private String externalGuidanceStatus;
    private String externalGuidanceUrl;

    // Risk management status
    private String riskManagementStatus;
    private String riskManagementUrl;

    // Mapping status
    private String mappingStatus;

    // Process mapping files
    private List<ProcessMappingDTO> mappings;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Process entity to a DTO.
     *
     * @param process the process entity
     * @return the process DTO, or null if the input is null
     */
    public static ProcessDTO fromEntity(Process process) {
        if (process == null) {
            return null;
        }

        ProcessDTOBuilder builder = ProcessDTO.builder()
                .id(process.getId())
                .institutionId(process.getInstitution().getId())
                .institutionName(process.getInstitution().getName())
                .institutionAcronym(process.getInstitution().getAcronym())
                .name(process.getName())
                .description(process.getDescription())
                .isCritical(process.isCritical())
                .documentationStatus(process.getDocumentationStatus() != null
                        ? process.getDocumentationStatus().name()
                        : null)
                .documentationUrl(process.getDocumentationUrl())
                .externalGuidanceStatus(process.getExternalGuidanceStatus() != null
                        ? process.getExternalGuidanceStatus().name()
                        : null)
                .externalGuidanceUrl(process.getExternalGuidanceUrl())
                .riskManagementStatus(process.getRiskManagementStatus() != null
                        ? process.getRiskManagementStatus().name()
                        : null)
                .riskManagementUrl(process.getRiskManagementUrl())
                .mappingStatus(process.getMappingStatus() != null
                        ? process.getMappingStatus().name()
                        : null)
                .active(process.isActive())
                .createdAt(process.getCreatedAt())
                .updatedAt(process.getUpdatedAt());

        // Add value chain info if present
        if (process.getValueChain() != null) {
            builder.valueChainId(process.getValueChain().getId())
                    .valueChainName(process.getValueChain().getName());
        }

        // Add responsible unit info if present
        if (process.getResponsibleUnit() != null) {
            builder.responsibleUnitId(process.getResponsibleUnit().getId())
                    .responsibleUnitName(process.getResponsibleUnit().getName())
                    .responsibleUnitAcronym(process.getResponsibleUnit().getAcronym());
        }

        // Add direct unit info if present
        if (process.getDirectUnit() != null) {
            builder.directUnitId(process.getDirectUnit().getId())
                    .directUnitName(process.getDirectUnit().getName())
                    .directUnitAcronym(process.getDirectUnit().getAcronym());
        }

        // Convert mappings to DTOs
        if (process.getMappings() != null) {
            List<ProcessMappingDTO> mappingDTOs = process.getMappings().stream()
                    .map(ProcessMappingDTO::fromEntity)
                    .collect(Collectors.toList());
            builder.mappings(mappingDTOs);
        }

        return builder.build();
    }
}
