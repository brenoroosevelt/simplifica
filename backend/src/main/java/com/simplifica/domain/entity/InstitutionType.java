package com.simplifica.domain.entity;

/**
 * Enum representing the type of institution in the system.
 *
 * Defines the classification of educational/governmental institutions:
 * - FEDERAL: Federal government institution
 * - ESTADUAL: State government institution
 * - MUNICIPAL: Municipal/city government institution
 * - PRIVADA: Private institution
 */
public enum InstitutionType {
    FEDERAL,
    ESTADUAL,
    MUNICIPAL,
    PRIVADA;

    /**
     * Converts a string representation to an InstitutionType enum value.
     *
     * @param type the string representation of the institution type
     * @return the corresponding InstitutionType enum value
     * @throws IllegalArgumentException if the type string is invalid
     */
    public static InstitutionType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Institution type cannot be null");
        }

        try {
            return InstitutionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid institution type: " + type
                + ". Supported types: FEDERAL, ESTADUAL, MUNICIPAL, PRIVADA"
            );
        }
    }
}
