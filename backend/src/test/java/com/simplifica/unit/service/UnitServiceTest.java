package com.simplifica.unit.service;

import com.simplifica.application.dto.CreateUnitDTO;
import com.simplifica.application.dto.UpdateUnitDTO;
import com.simplifica.application.service.InstitutionService;
import com.simplifica.application.service.UnitService;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionType;
import com.simplifica.domain.entity.Unit;
import com.simplifica.infrastructure.repository.UnitRepository;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ResourceAlreadyExistsException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UnitService.
 *
 * Tests business logic, multi-tenant isolation, and validation rules.
 */
@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private InstitutionService institutionService;

    @InjectMocks
    private UnitService unitService;

    private UUID institutionId;
    private Institution institution;
    private Unit unit;

    @BeforeEach
    void setUp() {
        institutionId = UUID.randomUUID();

        institution = Institution.builder()
                .id(institutionId)
                .name("Test Institution")
                .acronym("TEST")
                .type(InstitutionType.FEDERAL)
                .active(true)
                .build();

        unit = Unit.builder()
                .id(UUID.randomUUID())
                .institution(institution)
                .name("Technology Department")
                .acronym("TI")
                .description("IT Department")
                .active(true)
                .build();

        // Set tenant context
        TenantContext.setCurrentInstitution(institutionId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findById_shouldReturnUnit_whenUnitExistsAndBelongsToCurrentInstitution() {
        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));

        Unit result = unitService.findById(unit.getId());

        assertNotNull(result);
        assertEquals(unit.getId(), result.getId());
        assertEquals(unit.getName(), result.getName());
        verify(unitRepository).findById(unit.getId());
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenUnitDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(unitRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> unitService.findById(nonExistentId));
        verify(unitRepository).findById(nonExistentId);
    }

    @Test
    void findById_shouldThrowUnauthorizedAccessException_whenUnitBelongsToDifferentInstitution() {
        UUID differentInstitutionId = UUID.randomUUID();
        Institution differentInstitution = Institution.builder()
                .id(differentInstitutionId)
                .name("Different Institution")
                .acronym("DIFF")
                .type(InstitutionType.FEDERAL)
                .active(true)
                .build();

        Unit unitFromDifferentInstitution = Unit.builder()
                .id(UUID.randomUUID())
                .institution(differentInstitution)
                .name("Other Department")
                .acronym("OTH")
                .active(true)
                .build();

        when(unitRepository.findById(unitFromDifferentInstitution.getId()))
                .thenReturn(Optional.of(unitFromDifferentInstitution));

        assertThrows(UnauthorizedAccessException.class,
                () -> unitService.findById(unitFromDifferentInstitution.getId()));
    }

    @Test
    void findAll_shouldReturnPagedUnits_whenFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Unit> expectedPage = new PageImpl<>(List.of(unit));

        when(unitRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Unit> result = unitService.findAll(true, "Tech", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(unit.getId(), result.getContent().get(0).getId());
        verify(unitRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_shouldThrowBadRequestException_whenNoInstitutionContext() {
        TenantContext.clear();
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(BadRequestException.class,
                () -> unitService.findAll(null, null, pageable));
    }

    @Test
    void create_shouldCreateUnit_whenDataIsValid() {
        CreateUnitDTO dto = CreateUnitDTO.builder()
                .name("Technology Department")
                .acronym("TI")
                .description("IT Department")
                .active(true)
                .build();

        when(unitRepository.existsByAcronymAndInstitutionId("TI", institutionId))
                .thenReturn(false);
        when(institutionService.findById(institutionId)).thenReturn(institution);
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        Unit result = unitService.create(dto);

        assertNotNull(result);
        assertEquals(unit.getId(), result.getId());
        assertEquals("TI", result.getAcronym());
        verify(unitRepository).existsByAcronymAndInstitutionId("TI", institutionId);
        verify(institutionService).findById(institutionId);
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    void create_shouldNormalizeAcronymToUppercase() {
        CreateUnitDTO dto = CreateUnitDTO.builder()
                .name("Technology Department")
                .acronym("ti")
                .active(true)
                .build();

        when(unitRepository.existsByAcronymAndInstitutionId("TI", institutionId))
                .thenReturn(false);
        when(institutionService.findById(institutionId)).thenReturn(institution);
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        unitService.create(dto);

        verify(unitRepository).existsByAcronymAndInstitutionId("TI", institutionId);
    }

    @Test
    void create_shouldThrowResourceAlreadyExistsException_whenAcronymAlreadyExists() {
        CreateUnitDTO dto = CreateUnitDTO.builder()
                .name("Technology Department")
                .acronym("TI")
                .active(true)
                .build();

        when(unitRepository.existsByAcronymAndInstitutionId("TI", institutionId))
                .thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> unitService.create(dto));
        verify(unitRepository).existsByAcronymAndInstitutionId("TI", institutionId);
        verify(unitRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateUnit_whenDataIsValid() {
        UpdateUnitDTO dto = UpdateUnitDTO.builder()
                .name("Updated Name")
                .description("Updated Description")
                .active(false)
                .build();

        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        Unit result = unitService.update(unit.getId(), dto);

        assertNotNull(result);
        verify(unitRepository).findById(unit.getId());
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    void delete_shouldSoftDeleteUnit_whenUnitExists() {
        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        unitService.delete(unit.getId());

        verify(unitRepository).findById(unit.getId());
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenUnitDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(unitRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> unitService.delete(nonExistentId));
        verify(unitRepository).findById(nonExistentId);
        verify(unitRepository, never()).save(any());
    }
}
