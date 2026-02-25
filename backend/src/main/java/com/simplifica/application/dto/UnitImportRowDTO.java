package com.simplifica.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a single row from a CSV file
 * for unit import.
 *
 * Contains the unit data extracted from CSV columns plus optional
 * institution identifiers (only used by ADMIN users).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitImportRowDTO {

    /**
     * Name of the unit (required).
     * Maps to 'name' column in CSV.
     */
    private String name;

    /**
     * Unique acronym for the unit within an institution (required).
     * Maps to 'acronym' column in CSV.
     */
    private String acronym;

    /**
     * Optional superior organizational unit name.
     * Maps to 'unidadeSuperior' column in CSV.
     */
    private String parentUnit;

    /**
     * Optional description of the unit.
     * Maps to 'descricao' column in CSV.
     */
    private String description;

    /**
     * Whether the unit is active.
     * Maps to 'active' column in CSV.
     * Defaults to true if not specified.
     */
    private Boolean active;

    /**
     * Optional institution ID (UUID format).
     * Maps to 'instituicaoId' column in CSV.
     * Only used by ADMIN users - ignored for MANAGER.
     */
    private String institutionId;

    /**
     * Optional institution acronym.
     * Maps to 'instituicaoSigla' column in CSV.
     * Only used by ADMIN users - ignored for MANAGER.
     * Alternative to institutionId.
     */
    private String institutionAcronym;
}
