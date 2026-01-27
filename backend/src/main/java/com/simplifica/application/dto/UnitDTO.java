package com.simplifica.application.dto;

import com.simplifica.domain.entity.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Unit entity.
 *
 * Used to transfer unit data between layers without exposing
 * the entity directly. Includes institution information and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitDTO {

    private UUID id;
    private UUID institutionId;
    private String institutionName;
    private String institutionAcronym;
    private String name;
    private String acronym;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Unit entity to a DTO.
     *
     * @param unit the unit entity
     * @return the unit DTO, or null if the input is null
     */
    public static UnitDTO fromEntity(Unit unit) {
        if (unit == null) {
            return null;
        }

        return UnitDTO.builder()
                .id(unit.getId())
                .institutionId(unit.getInstitution().getId())
                .institutionName(unit.getInstitution().getName())
                .institutionAcronym(unit.getInstitution().getAcronym())
                .name(unit.getName())
                .acronym(unit.getAcronym())
                .description(unit.getDescription())
                .active(unit.getActive())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }
}
