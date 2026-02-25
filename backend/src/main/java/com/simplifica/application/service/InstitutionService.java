package com.simplifica.application.service;

import com.simplifica.application.dto.CreateInstitutionDTO;
import com.simplifica.application.dto.UpdateInstitutionDTO;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionType;
import com.simplifica.infrastructure.repository.InstitutionRepository;
import com.simplifica.infrastructure.repository.InstitutionSpecifications;
import com.simplifica.presentation.exception.ResourceAlreadyExistsException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.storage.service.StorageService;
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
 * Service for managing institutions (tenants).
 *
 * This service provides business logic for CRUD operations on institutions,
 * including validation of unique constraints (acronym, domain) and filtering.
 */
@Service
@Transactional(readOnly = true)
public class InstitutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionService.class);

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private StorageService storageService;

    /**
     * Finds an institution by its ID.
     *
     * @param id the institution's UUID
     * @return the Institution entity
     * @throws ResourceNotFoundException if the institution is not found
     */
    public Institution findById(UUID id) {
        LOGGER.debug("Finding institution by ID: {}", id);
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", id.toString()));
    }

    /**
     * Finds an institution by its acronym.
     *
     * @param acronym the institution's unique acronym
     * @return the Institution entity
     * @throws ResourceNotFoundException if the institution is not found
     */
    public Institution findByAcronym(String acronym) {
        LOGGER.debug("Finding institution by acronym: {}", acronym);
        return institutionRepository.findByAcronym(acronym)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", acronym));
    }

    /**
     * Finds all institutions with optional filtering and pagination.
     *
     * @param active filter by active status (null for all)
     * @param type filter by institution type (null for all)
     * @param search search term for name or acronym (null for no search)
     * @param pageable pagination and sorting parameters
     * @return paginated list of institutions
     */
    public Page<Institution> findAll(Boolean active, InstitutionType type,
                                     String search, Pageable pageable) {
        LOGGER.debug("Finding institutions with filters - active: {}, type: {}, search: {}",
                     active, type, search);

        Specification<Institution> spec = Specification.where(null);

        if (active != null) {
            spec = spec.and(InstitutionSpecifications.hasActive(active));
        }
        if (type != null) {
            spec = spec.and(InstitutionSpecifications.hasType(type));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(InstitutionSpecifications.searchByNameOrAcronym(search));
        }

        return institutionRepository.findAll(spec, pageable);
    }

    /**
     * Creates a new institution.
     *
     * @param dto the institution data
     * @return the created Institution entity
     * @throws ResourceAlreadyExistsException if acronym or domain already exists
     */
    @Transactional
    public Institution create(CreateInstitutionDTO dto) {
        LOGGER.info("Creating new institution: {}", dto.getAcronym());

        // Validate uniqueness of acronym
        if (institutionRepository.existsByAcronym(dto.getAcronym())) {
            throw new ResourceAlreadyExistsException("Institution", "acronym", dto.getAcronym());
        }

        // Validate uniqueness of domain if provided
        if (dto.getDomain() != null && institutionRepository.existsByDomain(dto.getDomain())) {
            throw new ResourceAlreadyExistsException("Institution", "domain", dto.getDomain());
        }

        Institution institution = Institution.builder()
                .name(dto.getName())
                .acronym(dto.getAcronym().toUpperCase())
                .type(dto.getType())
                .domain(dto.getDomain() != null ? dto.getDomain().toLowerCase() : null)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Institution saved = institutionRepository.save(institution);
        LOGGER.info("Created institution with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Creates a new institution with logo upload.
     *
     * @param dto the institution data
     * @param logo the logo file to upload (optional)
     * @return the created Institution entity
     * @throws ResourceAlreadyExistsException if acronym or domain already exists
     */
    @Transactional
    public Institution create(CreateInstitutionDTO dto, MultipartFile logo) {
        Institution institution = create(dto);

        if (logo != null && !logo.isEmpty()) {
            LOGGER.info("Uploading logo for institution: {}", institution.getId());
            StorageService.FileUploadResult uploadResult =
                    storageService.storeFile(logo, "Institution", institution.getId(),
                            com.simplifica.storage.domain.FileCategory.INSTITUTION_LOGO, true);
            institution.setLogoUrls(uploadResult.fileUrl(), uploadResult.thumbnailUrl());
            institution = institutionRepository.save(institution);
            LOGGER.info("Logo uploaded successfully for institution: {}", institution.getId());
        }

        return institution;
    }

    /**
     * Updates an existing institution.
     *
     * @param id the institution's UUID
     * @param dto the updated data (only non-null fields are updated)
     * @return the updated Institution entity
     * @throws ResourceNotFoundException if the institution is not found
     * @throws ResourceAlreadyExistsException if domain already exists
     */
    @Transactional
    public Institution update(UUID id, UpdateInstitutionDTO dto) {
        LOGGER.info("Updating institution: {}", id);

        Institution institution = findById(id);

        if (dto.getName() != null) {
            institution.setName(dto.getName());
        }
        if (dto.getType() != null) {
            institution.setType(dto.getType());
        }
        if (dto.getDomain() != null) {
            if (!dto.getDomain().equals(institution.getDomain())
                    && institutionRepository.existsByDomain(dto.getDomain())) {
                throw new ResourceAlreadyExistsException("Institution", "domain", dto.getDomain());
            }
            institution.setDomain(dto.getDomain().toLowerCase());
        }
        if (dto.getActive() != null) {
            institution.setActive(dto.getActive());
        }

        Institution saved = institutionRepository.save(institution);
        LOGGER.info("Updated institution: {}", id);
        return saved;
    }

    /**
     * Updates an existing institution with optional logo upload.
     *
     * @param id the institution's UUID
     * @param dto the updated data (only non-null fields are updated)
     * @param logo the new logo file (optional, replaces existing if provided)
     * @return the updated Institution entity
     * @throws ResourceNotFoundException if the institution is not found
     * @throws ResourceAlreadyExistsException if domain already exists
     */
    @Transactional
    public Institution update(UUID id, UpdateInstitutionDTO dto, MultipartFile logo) {
        Institution institution = update(id, dto);

        if (logo != null && !logo.isEmpty()) {
            LOGGER.info("Replacing logo for institution: {}", id);

            // Delete old logo if exists
            storageService.deleteByEntityAndCategory("Institution", id,
                    com.simplifica.storage.domain.FileCategory.INSTITUTION_LOGO);

            // Upload new logo
            StorageService.FileUploadResult uploadResult =
                    storageService.storeFile(logo, "Institution", id,
                            com.simplifica.storage.domain.FileCategory.INSTITUTION_LOGO, true);
            institution.setLogoUrls(uploadResult.fileUrl(), uploadResult.thumbnailUrl());
            institution = institutionRepository.save(institution);
            LOGGER.info("Logo replaced successfully for institution: {}", id);
        }

        return institution;
    }

    /**
     * Deletes the logo of an institution.
     *
     * @param id the institution's UUID
     * @return the updated Institution entity
     * @throws ResourceNotFoundException if the institution is not found
     */
    @Transactional
    public Institution deleteLogo(UUID id) {
        LOGGER.info("Deleting logo for institution: {}", id);
        Institution institution = findById(id);
        storageService.deleteByEntityAndCategory("Institution", id,
                com.simplifica.storage.domain.FileCategory.INSTITUTION_LOGO);
        institution.clearLogo();
        Institution saved = institutionRepository.save(institution);
        LOGGER.info("Logo deleted for institution: {}", id);
        return saved;
    }

    /**
     * Soft deletes an institution by setting its active status to false.
     *
     * @param id the institution's UUID
     * @throws ResourceNotFoundException if the institution is not found
     */
    @Transactional
    public void delete(UUID id) {
        LOGGER.info("Soft deleting institution: {}", id);
        Institution institution = findById(id);
        institution.setActive(false);
        institutionRepository.save(institution);
        LOGGER.info("Institution {} marked as inactive", id);
    }
}
