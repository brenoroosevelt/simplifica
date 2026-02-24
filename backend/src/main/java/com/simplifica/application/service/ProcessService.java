package com.simplifica.application.service;

import com.simplifica.application.dto.CreateProcessDTO;
import com.simplifica.application.dto.ProcessDTO;
import com.simplifica.application.dto.UpdateProcessDTO;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.Process;
import com.simplifica.domain.entity.ProcessDocumentationStatus;
import com.simplifica.domain.entity.ProcessExternalGuidanceStatus;
import com.simplifica.domain.entity.ProcessMapping;
import com.simplifica.domain.entity.ProcessMappingStatus;
import com.simplifica.domain.entity.ProcessRiskManagementStatus;
import com.simplifica.domain.entity.Unit;
import com.simplifica.domain.entity.ValueChain;
import com.simplifica.infrastructure.repository.ProcessMappingRepository;
import com.simplifica.infrastructure.repository.ProcessRepository;
import com.simplifica.infrastructure.repository.ProcessSpecifications;
import com.simplifica.infrastructure.repository.UnitRepository;
import com.simplifica.infrastructure.repository.ValueChainRepository;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ResourceAlreadyExistsException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing processes (business processes within institutions).
 *
 * Provides business logic for CRUD operations on processes with strict multi-tenant
 * isolation. All operations are scoped to the current institution from TenantContext.
 *
 * CRITICAL SECURITY:
 * - All queries MUST filter by institution_id from TenantContext
 * - validateTenantAccess MUST be called before any modification
 * - Foreign keys (ValueChain, Units) MUST belong to the same institution
 * - Never expose processes from other institutions
 *
 * Key Features:
 * - Multi-tenant isolation with institution-scoped queries
 * - Comprehensive filtering (active, search, valueChain, isCritical)
 * - Foreign key validation (ensure related entities belong to same institution)
 * - HTML file upload support for process mappings
 * - Soft delete (active flag)
 */
@Service
@Transactional(readOnly = true)
public class ProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessMappingRepository processMappingRepository;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private ValueChainRepository valueChainRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Gets the current institution ID from TenantContext.
     * CRITICAL: This should always be set by the request interceptor.
     *
     * @return the current institution ID
     * @throws BadRequestException if no institution context is set
     */
    private UUID getCurrentInstitutionId() {
        UUID institutionId = TenantContext.getCurrentInstitution();
        if (institutionId == null) {
            LOGGER.error("SECURITY ALERT: TenantContext is null. Interceptor may have failed to set institution context.");
            throw new BadRequestException("No institution context set. Please select an institution.");
        }
        return institutionId;
    }

    /**
     * Validates that the process belongs to the current institution.
     * CRITICAL: Must be called before any modification operation.
     *
     * @param process the process to validate
     * @throws UnauthorizedAccessException if the process belongs to a different institution
     */
    private void validateTenantAccess(Process process) {
        UUID currentInstitutionId = getCurrentInstitutionId();
        if (!process.getInstitution().getId().equals(currentInstitutionId)) {
            LOGGER.warn("Unauthorized access attempt to process {} from institution {}",
                    process.getId(), currentInstitutionId);
            throw new UnauthorizedAccessException(
                    "You do not have permission to access this process"
            );
        }
    }

    /**
     * Finds all processes for the current institution with optional filtering and pagination.
     *
     * @param active filter by active status (null for all)
     * @param search search term for name and description (null for no search)
     * @param valueChainId filter by value chain (null for all)
     * @param isCritical filter by critical status (null for all)
     * @param documentationStatus filter by documentation status (null for all)
     * @param externalGuidanceStatus filter by external guidance status (null for all)
     * @param riskManagementStatus filter by risk management status (null for all)
     * @param mappingStatus filter by mapping status (null for all)
     * @param responsibleUnitId filter by responsible unit (null for all)
     * @param directUnitId filter by direct unit (null for all)
     * @param pageable pagination and sorting parameters
     * @return paginated list of process DTOs
     */
    public Page<ProcessDTO> findAll(Boolean active, String search, UUID valueChainId,
                                    Boolean isCritical, ProcessDocumentationStatus documentationStatus,
                                    ProcessExternalGuidanceStatus externalGuidanceStatus,
                                    ProcessRiskManagementStatus riskManagementStatus,
                                    ProcessMappingStatus mappingStatus, UUID responsibleUnitId,
                                    UUID directUnitId, Pageable pageable) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.debug("Finding processes for institution {} with filters - active: {}, search: {}, " +
                     "valueChainId: {}, isCritical: {}, documentationStatus: {}, externalGuidanceStatus: {}, " +
                     "riskManagementStatus: {}, mappingStatus: {}, responsibleUnitId: {}, directUnitId: {}",
                institutionId, active, search, valueChainId, isCritical, documentationStatus,
                externalGuidanceStatus, riskManagementStatus, mappingStatus, responsibleUnitId, directUnitId);

        // Build specification with MANDATORY institution filter
        Specification<Process> spec = Specification
                .where(ProcessSpecifications.withRelations())
                .and(ProcessSpecifications.belongsToInstitution(institutionId));

        if (active != null) {
            spec = spec.and(ProcessSpecifications.hasActive(active));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(ProcessSpecifications.searchByMultipleFields(search));
        }
        if (valueChainId != null) {
            spec = spec.and(ProcessSpecifications.hasValueChain(valueChainId));
        }
        if (isCritical != null) {
            spec = spec.and(ProcessSpecifications.hasCritical(isCritical));
        }
        if (documentationStatus != null) {
            spec = spec.and(ProcessSpecifications.hasDocumentationStatus(documentationStatus));
        }
        if (externalGuidanceStatus != null) {
            spec = spec.and(ProcessSpecifications.hasExternalGuidanceStatus(externalGuidanceStatus));
        }
        if (riskManagementStatus != null) {
            spec = spec.and(ProcessSpecifications.hasRiskManagementStatus(riskManagementStatus));
        }
        if (mappingStatus != null) {
            spec = spec.and(ProcessSpecifications.hasMappingStatus(mappingStatus));
        }
        if (responsibleUnitId != null) {
            spec = spec.and(ProcessSpecifications.hasResponsibleUnit(responsibleUnitId));
        }
        if (directUnitId != null) {
            spec = spec.and(ProcessSpecifications.hasDirectUnit(directUnitId));
        }

        Page<Process> processes = processRepository.findAll(spec, pageable);
        return processes.map(ProcessDTO::fromEntity);
    }

    /**
     * Finds a process by ID, ensuring it belongs to the current institution.
     *
     * @param id the process UUID
     * @return the process DTO
     * @throws ResourceNotFoundException if the process is not found
     * @throws UnauthorizedAccessException if the process belongs to another institution
     */
    public ProcessDTO findById(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.debug("Finding process {} for institution {}", id, institutionId);

        // Use specification to eagerly fetch relationships
        Specification<Process> spec = Specification
                .where(ProcessSpecifications.withRelations())
                .and((root, query, cb) -> cb.equal(root.get("id"), id));

        Process process = processRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Process", id.toString()));

        // Validate tenant access
        validateTenantAccess(process);

        return ProcessDTO.fromEntity(process);
    }

    /**
     * Creates a new process for the current institution.
     *
     * Validates:
     * - Name uniqueness (optional - can be disabled if not required)
     * - Foreign keys (ValueChain, Units) belong to the same institution
     *
     * @param dto the process data
     * @return the created process DTO
     * @throws ResourceAlreadyExistsException if a process with the same name already exists (if validation enabled)
     * @throws BadRequestException if foreign key validation fails
     */
    @Transactional
    public ProcessDTO create(CreateProcessDTO dto) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Creating new process '{}' for institution {}", dto.getName(), institutionId);

        // Note: Name uniqueness validation is optional - uncomment if needed
        // if (processRepository.existsByNameAndInstitutionId(dto.getName(), institutionId)) {
        //     throw new ResourceAlreadyExistsException("Process", "name", dto.getName());
        // }

        // Get institution entity
        Institution institution = institutionService.findById(institutionId);

        // Build process entity
        Process.ProcessBuilder builder = Process.builder()
                .institution(institution)
                .name(dto.getName())
                .description(dto.getDescription())
                .isCritical(dto.getIsCritical() != null ? dto.getIsCritical() : false)
                .active(dto.getActive() != null ? dto.getActive() : true);

        // Validate and set value chain if provided
        if (dto.getValueChainId() != null) {
            ValueChain valueChain = validateAndGetValueChain(dto.getValueChainId(), institutionId);
            builder.valueChain(valueChain);
        }

        // Validate and set responsible unit if provided
        if (dto.getResponsibleUnitId() != null) {
            Unit responsibleUnit = validateAndGetUnit(dto.getResponsibleUnitId(), institutionId);
            builder.responsibleUnit(responsibleUnit);
        }

        // Validate and set direct unit if provided
        if (dto.getDirectUnitId() != null) {
            Unit directUnit = validateAndGetUnit(dto.getDirectUnitId(), institutionId);
            builder.directUnit(directUnit);
        }

        // Set documentation status and URL
        if (dto.getDocumentationStatus() != null) {
            try {
                ProcessDocumentationStatus status = ProcessDocumentationStatus.valueOf(dto.getDocumentationStatus());
                builder.documentationStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid documentation status: " + dto.getDocumentationStatus());
            }
        }
        builder.documentationUrl(dto.getDocumentationUrl());

        // Set external guidance status and URL
        if (dto.getExternalGuidanceStatus() != null) {
            try {
                ProcessExternalGuidanceStatus status = ProcessExternalGuidanceStatus.valueOf(dto.getExternalGuidanceStatus());
                builder.externalGuidanceStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid external guidance status: " + dto.getExternalGuidanceStatus());
            }
        }
        builder.externalGuidanceUrl(dto.getExternalGuidanceUrl());

        // Set risk management status and URL
        if (dto.getRiskManagementStatus() != null) {
            try {
                ProcessRiskManagementStatus status = ProcessRiskManagementStatus.valueOf(dto.getRiskManagementStatus());
                builder.riskManagementStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid risk management status: " + dto.getRiskManagementStatus());
            }
        }
        builder.riskManagementUrl(dto.getRiskManagementUrl());

        // Set mapping status
        if (dto.getMappingStatus() != null) {
            try {
                ProcessMappingStatus status = ProcessMappingStatus.valueOf(dto.getMappingStatus());
                builder.mappingStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid mapping status: " + dto.getMappingStatus());
            }
        }

        Process process = builder.build();
        Process saved = processRepository.save(process);
        LOGGER.info("Created process with ID: {}", saved.getId());

        return ProcessDTO.fromEntity(saved);
    }

    /**
     * Updates an existing process.
     * Only non-null fields in the DTO are updated.
     *
     * @param id the process UUID
     * @param dto the updated data (partial updates supported)
     * @return the updated process DTO
     * @throws ResourceNotFoundException if the process is not found
     * @throws UnauthorizedAccessException if the process belongs to another institution
     * @throws BadRequestException if foreign key validation fails
     */
    @Transactional
    public ProcessDTO update(UUID id, UpdateProcessDTO dto) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Updating process {} for institution {}", id, institutionId);

        Process process = processRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Process", id.toString()));

        // Validate tenant access
        validateTenantAccess(process);

        // Update name if provided
        if (dto.getName() != null && !dto.getName().isBlank()) {
            // Note: Name uniqueness validation is optional - uncomment if needed
            // if (!dto.getName().equals(process.getName()) &&
            //     processRepository.existsByNameAndInstitutionIdAndIdNot(dto.getName(), institutionId, id)) {
            //     throw new ResourceAlreadyExistsException("Process", "name", dto.getName());
            // }
            process.setName(dto.getName());
        }

        // Update description if provided
        if (dto.getDescription() != null) {
            process.setDescription(dto.getDescription());
        }

        // Update isCritical if provided
        if (dto.getIsCritical() != null) {
            process.setCritical(dto.getIsCritical());
        }

        // Update value chain if provided (null clears the relationship)
        if (dto.getValueChainId() != null) {
            ValueChain valueChain = validateAndGetValueChain(dto.getValueChainId(), institutionId);
            process.setValueChain(valueChain);
        }

        // Update responsible unit if provided (null clears the relationship)
        if (dto.getResponsibleUnitId() != null) {
            Unit responsibleUnit = validateAndGetUnit(dto.getResponsibleUnitId(), institutionId);
            process.setResponsibleUnit(responsibleUnit);
        }

        // Update direct unit if provided (null clears the relationship)
        if (dto.getDirectUnitId() != null) {
            Unit directUnit = validateAndGetUnit(dto.getDirectUnitId(), institutionId);
            process.setDirectUnit(directUnit);
        }

        // Update documentation status and URL if provided
        if (dto.getDocumentationStatus() != null) {
            try {
                ProcessDocumentationStatus status = ProcessDocumentationStatus.valueOf(dto.getDocumentationStatus());
                process.setDocumentationStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid documentation status: " + dto.getDocumentationStatus());
            }
        }
        if (dto.getDocumentationUrl() != null) {
            process.setDocumentationUrl(dto.getDocumentationUrl());
        }

        // Update external guidance status and URL if provided
        if (dto.getExternalGuidanceStatus() != null) {
            try {
                ProcessExternalGuidanceStatus status = ProcessExternalGuidanceStatus.valueOf(dto.getExternalGuidanceStatus());
                process.setExternalGuidanceStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid external guidance status: " + dto.getExternalGuidanceStatus());
            }
        }
        if (dto.getExternalGuidanceUrl() != null) {
            process.setExternalGuidanceUrl(dto.getExternalGuidanceUrl());
        }

        // Update risk management status and URL if provided
        if (dto.getRiskManagementStatus() != null) {
            try {
                ProcessRiskManagementStatus status = ProcessRiskManagementStatus.valueOf(dto.getRiskManagementStatus());
                process.setRiskManagementStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid risk management status: " + dto.getRiskManagementStatus());
            }
        }
        if (dto.getRiskManagementUrl() != null) {
            process.setRiskManagementUrl(dto.getRiskManagementUrl());
        }

        // Update mapping status if provided
        if (dto.getMappingStatus() != null) {
            try {
                ProcessMappingStatus status = ProcessMappingStatus.valueOf(dto.getMappingStatus());
                process.setMappingStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid mapping status: " + dto.getMappingStatus());
            }
        }

        // Update active status if provided
        if (dto.getActive() != null) {
            process.setActive(dto.getActive());
        }

        Process saved = processRepository.save(process);
        LOGGER.info("Updated process: {}", id);

        return ProcessDTO.fromEntity(saved);
    }

    /**
     * Soft deletes a process by setting its active status to false.
     * The process and its related data remain in the database for audit purposes.
     *
     * @param id the process UUID
     * @throws ResourceNotFoundException if the process is not found
     * @throws UnauthorizedAccessException if the process belongs to another institution
     */
    @Transactional
    public void delete(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Soft deleting process {} for institution {}", id, institutionId);

        Process process = processRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Process", id.toString()));

        // Validate tenant access
        validateTenantAccess(process);

        process.setActive(false);
        processRepository.save(process);
        LOGGER.info("Process {} marked as inactive", id);
    }

    /**
     * Uploads multiple HTML mapping files for a process.
     * Each file is validated and stored using FileStorageService.
     *
     * @param processId the process UUID
     * @param files list of HTML files to upload
     * @return the updated process DTO with new mappings
     * @throws ResourceNotFoundException if the process is not found
     * @throws UnauthorizedAccessException if the process belongs to another institution
     * @throws BadRequestException if file validation fails
     */
    @Transactional
    public ProcessDTO uploadMappings(UUID processId, List<MultipartFile> files) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Uploading {} HTML mapping file(s) for process {} in institution {}",
                files.size(), processId, institutionId);

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one file is required");
        }

        Process process = processRepository.findByIdAndInstitutionId(processId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Process", processId.toString()));

        // Validate tenant access
        validateTenantAccess(process);

        List<ProcessMapping> newMappings = new ArrayList<>();

        for (MultipartFile file : files) {
            // Upload HTML file using FileStorageService
            FileStorageService.FileUploadResult uploadResult =
                    fileStorageService.storeHtmlFile(file, "processes");

            // Create ProcessMapping entity
            ProcessMapping mapping = ProcessMapping.builder()
                    .process(process)
                    .fileUrl(uploadResult.getFileUrl())
                    .filename(uploadResult.getFilename())
                    .fileSize(file.getSize())
                    .build();

            newMappings.add(mapping);
            process.addMapping(mapping);

            LOGGER.debug("Created mapping for file: {} ({})", uploadResult.getFilename(), file.getOriginalFilename());
        }

        // Save all mappings
        processMappingRepository.saveAll(newMappings);

        Process saved = processRepository.save(process);
        LOGGER.info("Uploaded {} HTML mapping file(s) successfully for process {}", files.size(), processId);

        return ProcessDTO.fromEntity(saved);
    }

    /**
     * Deletes a specific mapping file from a process.
     * The file is removed from storage and the database.
     *
     * @param processId the process UUID
     * @param mappingId the mapping UUID to delete
     * @throws ResourceNotFoundException if the process or mapping is not found
     * @throws UnauthorizedAccessException if the process belongs to another institution
     * @throws BadRequestException if the mapping does not belong to the process
     */
    @Transactional
    public void deleteMapping(UUID processId, UUID mappingId) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Deleting mapping {} from process {} in institution {}", mappingId, processId, institutionId);

        Process process = processRepository.findByIdAndInstitutionId(processId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Process", processId.toString()));

        // Validate tenant access
        validateTenantAccess(process);

        ProcessMapping mapping = processMappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProcessMapping", mappingId.toString()));

        // Verify that the mapping belongs to the specified process
        if (!mapping.getProcess().getId().equals(processId)) {
            LOGGER.warn("Mapping {} does not belong to process {}", mappingId, processId);
            throw new BadRequestException("The specified mapping does not belong to this process");
        }

        // Delete file from storage
        fileStorageService.deleteFile(mapping.getFileUrl());

        // Remove mapping from process and delete from database
        process.removeMapping(mapping);
        processMappingRepository.delete(mapping);

        LOGGER.info("Mapping {} deleted successfully from process {}", mappingId, processId);
    }

    /**
     * Validates that a value chain exists and belongs to the specified institution.
     *
     * @param valueChainId the value chain UUID
     * @param institutionId the institution UUID
     * @return the validated ValueChain entity
     * @throws ResourceNotFoundException if the value chain is not found
     * @throws BadRequestException if the value chain belongs to a different institution
     */
    private ValueChain validateAndGetValueChain(UUID valueChainId, UUID institutionId) {
        ValueChain valueChain = valueChainRepository.findById(valueChainId)
                .orElseThrow(() -> new ResourceNotFoundException("ValueChain", valueChainId.toString()));

        if (!valueChain.getInstitution().getId().equals(institutionId)) {
            LOGGER.warn("Value chain {} does not belong to institution {}", valueChainId, institutionId);
            throw new BadRequestException(
                    "The specified value chain does not belong to your institution"
            );
        }

        return valueChain;
    }

    /**
     * Validates that a unit exists and belongs to the specified institution.
     *
     * @param unitId the unit UUID
     * @param institutionId the institution UUID
     * @return the validated Unit entity
     * @throws ResourceNotFoundException if the unit is not found
     * @throws BadRequestException if the unit belongs to a different institution
     */
    private Unit validateAndGetUnit(UUID unitId, UUID institutionId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", unitId.toString()));

        if (!unit.getInstitution().getId().equals(institutionId)) {
            LOGGER.warn("Unit {} does not belong to institution {}", unitId, institutionId);
            throw new BadRequestException(
                    "The specified unit does not belong to your institution"
            );
        }

        return unit;
    }
}
