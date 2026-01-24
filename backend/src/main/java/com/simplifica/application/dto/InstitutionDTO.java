package com.simplifica.application.dto;

import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Institution entity.
 *
 * Used to transfer institution data between layers without exposing
 * the entity directly. Contains all institution information including
 * logo URLs and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDTO {

    private UUID id;
    private String name;
    private String acronym;
    private String logoUrl;
    private String logoThumbnailUrl;
    private LocalDateTime logoUploadedAt;
    private InstitutionType type;
    private String domain;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts an Institution entity to a DTO.
     *
     * @param institution the institution entity
     * @return the institution DTO, or null if the input is null
     */
    public static InstitutionDTO fromEntity(Institution institution) {
        if (institution == null) {
            return null;
        }

        return InstitutionDTO.builder()
                .id(institution.getId())
                .name(institution.getName())
                .acronym(institution.getAcronym())
                .logoUrl(institution.getLogoUrl())
                .logoThumbnailUrl(institution.getLogoThumbnailUrl())
                .logoUploadedAt(institution.getLogoUploadedAt())
                .type(institution.getType())
                .domain(institution.getDomain())
                .active(institution.getActive())
                .createdAt(institution.getCreatedAt())
                .updatedAt(institution.getUpdatedAt())
                .build();
    }
}
