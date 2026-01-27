package com.simplifica.application.service;

import com.simplifica.application.dto.CreateValueChainDTO;
import com.simplifica.application.dto.UpdateValueChainDTO;
import com.simplifica.application.dto.ValueChainDTO;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.ValueChain;
import com.simplifica.infrastructure.repository.ValueChainRepository;
import com.simplifica.infrastructure.repository.ValueChainSpecifications;
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

import java.util.UUID;

/**
 * Service for managing value chains (cadeias de valor).
 *
 * This service provides business logic for CRUD operations on value chains,
 * with strict multi-tenant isolation. All operations are scoped to the current
 * institution from TenantContext.
 *
 * CRITICAL SECURITY:
 * - All queries MUST filter by institution_id from TenantContext
 * - validateTenantAccess MUST be called before any modification
 * - Never expose value chains from other institutions
 */
@Service
@Transactional(readOnly = true)
public class ValueChainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueChainService.class);

    @Autowired
    private ValueChainRepository valueChainRepository;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Gets the current institution ID from TenantContext.
     * CRITICAL: This should always be set by the request interceptor.
     *
     * @return the current institution ID
     * @throws UnauthorizedAccessException if no institution context is set
     */
    private UUID getCurrentInstitutionId() {
        UUID institutionId = TenantContext.getCurrentInstitution();
        if (institutionId == null) {
            // Log como erro crítico - isso indica falha no interceptor
            LOGGER.error("SECURITY ALERT: TenantContext is null. Interceptor may have failed to set institution context.");
            throw new UnauthorizedAccessException("Access denied: No institution context found");
        }
        return institutionId;
    }

    /**
     * Validates that the value chain belongs to the current institution.
     * CRITICAL: Must be called before any modification operation.
     *
     * @param valueChain the value chain to validate
     * @throws UnauthorizedAccessException if the value chain belongs to a different institution
     */
    private void validateTenantAccess(ValueChain valueChain) {
        UUID currentInstitutionId = getCurrentInstitutionId();
        if (!valueChain.getInstitution().getId().equals(currentInstitutionId)) {
            LOGGER.warn("Unauthorized access attempt to value chain {} from institution {}",
                    valueChain.getId(), currentInstitutionId);
            throw new UnauthorizedAccessException(
                    "You do not have access to this value chain");
        }
    }

    /**
     * Finds all value chains for the current institution with optional filtering and pagination.
     *
     * @param active filter by active status (null for all)
     * @param search search term for name (null for no search)
     * @param pageable pagination and sorting parameters
     * @return paginated list of value chain DTOs
     */
    public Page<ValueChainDTO> findAll(Boolean active, String search, Pageable pageable) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.debug("Finding value chains for institution {} with filters - active: {}, search: {}",
                institutionId, active, search);

        // Build specification with MANDATORY institution filter
        Specification<ValueChain> spec = Specification.where(
                ValueChainSpecifications.belongsToInstitution(institutionId));

        if (active != null) {
            spec = spec.and(ValueChainSpecifications.hasActive(active));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(ValueChainSpecifications.searchByName(search));
        }

        Page<ValueChain> valueChains = valueChainRepository.findAll(spec, pageable);
        return valueChains.map(ValueChainDTO::fromEntity);
    }

    /**
     * Finds a value chain by ID, ensuring it belongs to the current institution.
     *
     * @param id the value chain's UUID
     * @return the value chain DTO
     * @throws ResourceNotFoundException if the value chain is not found
     * @throws UnauthorizedAccessException if the value chain belongs to another institution
     */
    public ValueChainDTO findById(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.debug("Finding value chain {} for institution {}", id, institutionId);

        ValueChain valueChain = valueChainRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ValueChain", id.toString()));

        return ValueChainDTO.fromEntity(valueChain);
    }

    /**
     * Creates a new value chain for the current institution.
     *
     * @param dto the value chain data
     * @return the created value chain DTO
     * @throws ResourceAlreadyExistsException if a value chain with the same name already exists
     */
    @Transactional
    public ValueChainDTO create(CreateValueChainDTO dto) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Creating new value chain '{}' for institution {}", dto.getName(), institutionId);

        // Validate uniqueness of name within institution
        if (valueChainRepository.existsByNameAndInstitutionId(dto.getName(), institutionId)) {
            throw new ResourceAlreadyExistsException("ValueChain", "name", dto.getName());
        }

        // Get institution entity
        Institution institution = institutionService.findById(institutionId);

        // Build and save value chain
        ValueChain valueChain = ValueChain.builder()
                .institution(institution)
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        ValueChain saved = valueChainRepository.save(valueChain);
        LOGGER.info("Created value chain with ID: {}", saved.getId());
        return ValueChainDTO.fromEntity(saved);
    }

    /**
     * Updates an existing value chain.
     *
     * @param id the value chain's UUID
     * @param dto the updated data (only non-null fields are updated)
     * @return the updated value chain DTO
     * @throws ResourceNotFoundException if the value chain is not found
     * @throws UnauthorizedAccessException if the value chain belongs to another institution
     * @throws ResourceAlreadyExistsException if name already exists for this institution
     */
    @Transactional
    public ValueChainDTO update(UUID id, UpdateValueChainDTO dto) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Updating value chain {} for institution {}", id, institutionId);

        ValueChain valueChain = valueChainRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ValueChain", id.toString()));

        // Validate tenant access
        validateTenantAccess(valueChain);

        // Update name if provided and check uniqueness
        if (dto.getName() != null && !dto.getName().equals(valueChain.getName())) {
            if (valueChainRepository.existsByNameAndInstitutionId(dto.getName(), institutionId)) {
                throw new ResourceAlreadyExistsException("ValueChain", "name", dto.getName());
            }
            valueChain.setName(dto.getName());
        }

        // Update other fields if provided
        if (dto.getDescription() != null) {
            valueChain.setDescription(dto.getDescription());
        }
        if (dto.getActive() != null) {
            valueChain.setActive(dto.getActive());
        }

        ValueChain saved = valueChainRepository.save(valueChain);
        LOGGER.info("Updated value chain: {}", id);
        return ValueChainDTO.fromEntity(saved);
    }

    /**
     * Uploads an image for a value chain, replacing any existing image.
     *
     * @param id the value chain's UUID
     * @param file the image file to upload
     * @return the updated value chain DTO
     * @throws ResourceNotFoundException if the value chain is not found
     * @throws UnauthorizedAccessException if the value chain belongs to another institution
     */
    @Transactional
    public ValueChainDTO uploadImage(UUID id, MultipartFile file) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Uploading image for value chain {} in institution {}", id, institutionId);

        ValueChain valueChain = valueChainRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ValueChain", id.toString()));

        // Validate tenant access
        validateTenantAccess(valueChain);

        // Delete old image if exists
        if (valueChain.getImageUrl() != null) {
            LOGGER.debug("Deleting old image for value chain {}", id);
            fileStorageService.deleteFile(valueChain.getImageUrl());
        }

        // Upload new image
        FileStorageService.FileUploadResult uploadResult =
                fileStorageService.storeImage(file, "value-chains");
        valueChain.setImageUrls(uploadResult.getFileUrl(), uploadResult.getThumbnailUrl());

        ValueChain saved = valueChainRepository.save(valueChain);
        LOGGER.info("Image uploaded successfully for value chain {}", id);
        return ValueChainDTO.fromEntity(saved);
    }

    /**
     * Deletes the image of a value chain (keeps the value chain itself).
     *
     * @param id the value chain's UUID
     * @throws ResourceNotFoundException if the value chain is not found
     * @throws UnauthorizedAccessException if the value chain belongs to another institution
     */
    @Transactional
    public void deleteImage(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Deleting image for value chain {} in institution {}", id, institutionId);

        ValueChain valueChain = valueChainRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ValueChain", id.toString()));

        // Validate tenant access
        validateTenantAccess(valueChain);

        // Delete image file if exists
        if (valueChain.getImageUrl() != null) {
            fileStorageService.deleteFile(valueChain.getImageUrl());
            valueChain.clearImage();
            valueChainRepository.save(valueChain);
            LOGGER.info("Image deleted successfully for value chain {}", id);
        } else {
            LOGGER.debug("No image to delete for value chain {}", id);
        }
    }

    /**
     * Soft deletes a value chain by setting its active status to false.
     *
     * @param id the value chain's UUID
     * @throws ResourceNotFoundException if the value chain is not found
     * @throws UnauthorizedAccessException if the value chain belongs to another institution
     */
    @Transactional
    public void delete(UUID id) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Soft deleting value chain {} for institution {}", id, institutionId);

        ValueChain valueChain = valueChainRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("ValueChain", id.toString()));

        // Validate tenant access
        validateTenantAccess(valueChain);

        valueChain.setActive(false);
        valueChainRepository.save(valueChain);
        LOGGER.info("Value chain {} marked as inactive", id);
    }
}
