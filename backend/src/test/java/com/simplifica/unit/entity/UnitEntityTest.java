package com.simplifica.unit.entity;

import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Unit entity.
 *
 * Tests entity behavior including acronym normalization,
 * lifecycle callbacks, and business logic methods.
 */
@DisplayName("Unit Entity Tests")
class UnitEntityTest {

    @Test
    @DisplayName("Should normalize acronym to uppercase when set via setter")
    void shouldNormalizeAcronymToUppercaseViaSetter() {
        // Given
        Unit unit = new Unit();

        // When
        unit.setAcronym("dep-01");

        // Then
        assertThat(unit.getAcronym()).isEqualTo("DEP-01");
    }

    @Test
    @DisplayName("Should trim acronym when set via setter")
    void shouldTrimAcronymViaSetter() {
        // Given
        Unit unit = new Unit();

        // When
        unit.setAcronym("  TI  ");

        // Then
        assertThat(unit.getAcronym()).isEqualTo("TI");
    }

    @Test
    @DisplayName("Should handle null acronym gracefully")
    void shouldHandleNullAcronymGracefully() {
        // Given
        Unit unit = new Unit();

        // When
        unit.setAcronym(null);

        // Then
        assertThat(unit.getAcronym()).isNull();
    }

    @Test
    @DisplayName("Should normalize acronym when building with lowercase")
    void shouldNormalizeAcronymWhenBuildingWithLowercase() {
        // Given
        Institution institution = Institution.builder()
            .id(UUID.randomUUID())
            .name("Test Institution")
            .build();

        // When
        Unit unit = Unit.builder()
            .institution(institution)
            .name("Departamento de TI")
            .description("Departamento de Tecnologia da Informação")
            .build();

        unit.setAcronym("dep-ti");  // lowercase

        // Then
        assertThat(unit.getAcronym()).isEqualTo("DEP-TI");
    }

    @Test
    @DisplayName("Should return true when unit is active")
    void shouldReturnTrueWhenUnitIsActive() {
        // Given
        Unit unit = Unit.builder()
            .active(true)
            .build();

        // When & Then
        assertThat(unit.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return false when unit is inactive")
    void shouldReturnFalseWhenUnitIsInactive() {
        // Given
        Unit unit = Unit.builder()
            .active(false)
            .build();

        // When & Then
        assertThat(unit.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return false when active is null")
    void shouldReturnFalseWhenActiveIsNull() {
        // Given
        Unit unit = Unit.builder()
            .active(null)
            .build();

        // When & Then
        assertThat(unit.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should build unit with all required fields via builder")
    void shouldBuildUnitWithAllRequiredFields() {
        // Given
        Institution institution = Institution.builder()
            .id(UUID.randomUUID())
            .name("Test Institution")
            .build();

        // When
        Unit unit = Unit.builder()
            .id(UUID.randomUUID())
            .institution(institution)
            .name("Departamento Financeiro")
            .acronym("DEPTO-FIN")
            .description("Responsável pela gestão financeira")
            .active(true)
            .build();

        // Then
        assertThat(unit.getId()).isNotNull();
        assertThat(unit.getInstitution()).isEqualTo(institution);
        assertThat(unit.getName()).isEqualTo("Departamento Financeiro");
        assertThat(unit.getAcronym()).isEqualTo("DEPTO-FIN");
        assertThat(unit.getDescription()).isEqualTo("Responsável pela gestão financeira");
        assertThat(unit.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should have active as true by default when using builder")
    void shouldHaveActiveTrueByDefaultWhenUsingBuilder() {
        // Given & When
        Unit unit = Unit.builder()
            .name("Test Unit")
            .acronym("TEST")
            .build();

        // Then
        assertThat(unit.getActive()).isTrue();
    }
}
