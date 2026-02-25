package com.simplifica.application.service;

import com.simplifica.application.dto.CreateNormativeDTO;
import com.simplifica.application.dto.UpdateNormativeDTO;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.Normative;
import com.simplifica.infrastructure.repository.NormativeRepository;
import com.simplifica.infrastructure.repository.NormativeSpecifications;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import com.simplifica.storage.domain.FileCategory;
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

@Service
@Transactional(readOnly = true)
public class NormativeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormativeService.class);

    @Autowired
    private NormativeRepository normativeRepository;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private StorageService storageService;

    public Normative findById(UUID id) {
        Specification<Normative> spec = Specification
                .where(NormativeSpecifications.withInstitution())
                .and((root, query, cb) -> cb.equal(root.get("id"), id));

        Normative normative = normativeRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Normative", id.toString()));

        validateTenantAccess(normative);
        return normative;
    }

    public Page<Normative> findAll(String search, Pageable pageable) {
        UUID institutionId = getCurrentInstitutionId();

        Specification<Normative> spec = Specification
                .where(NormativeSpecifications.withInstitution())
                .and(NormativeSpecifications.belongsToInstitution(institutionId));

        if (search != null && !search.isBlank()) {
            spec = spec.and(NormativeSpecifications.searchByTitle(search));
        }

        return normativeRepository.findAll(spec, pageable);
    }

    @Transactional
    public Normative create(CreateNormativeDTO dto, MultipartFile file) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Creating normative '{}' for institution: {}", dto.getTitle(), institutionId);

        if ((file == null || file.isEmpty()) && (dto.getExternalLink() == null || dto.getExternalLink().isBlank())) {
            throw new BadRequestException("A normative must have either a file or an external link");
        }

        Institution institution = institutionService.findById(institutionId);

        Normative normative = Normative.builder()
                .institution(institution)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .externalLink(dto.getExternalLink())
                .build();

        Normative saved = normativeRepository.save(normative);

        if (file != null && !file.isEmpty()) {
            StorageService.FileUploadResult result = storageService.storeFile(
                    file, "Normative", saved.getId(), FileCategory.NORMATIVE_FILE, false);
            saved.setFileUrl(result.fileUrl());
            saved.setFileOriginalName(file.getOriginalFilename());
            saved = normativeRepository.save(saved);
        }

        LOGGER.info("Created normative with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Normative update(UUID id, UpdateNormativeDTO dto, MultipartFile file) {
        LOGGER.info("Updating normative: {}", id);
        Normative normative = findById(id);

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            normative.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            normative.setDescription(dto.getDescription());
        }
        if (dto.getExternalLink() != null) {
            normative.setExternalLink(dto.getExternalLink().isBlank() ? null : dto.getExternalLink());
        }

        if (file != null && !file.isEmpty()) {
            // Delete old file if exists
            if (normative.getFileUrl() != null) {
                storageService.deleteByEntityAndCategory("Normative", id, FileCategory.NORMATIVE_FILE);
            }
            StorageService.FileUploadResult result = storageService.storeFile(
                    file, "Normative", id, FileCategory.NORMATIVE_FILE, false);
            normative.setFileUrl(result.fileUrl());
            normative.setFileOriginalName(file.getOriginalFilename());
        }

        Normative saved = normativeRepository.save(normative);
        LOGGER.info("Updated normative: {}", id);
        return saved;
    }

    @Transactional
    public Normative deleteFile(UUID id) {
        Normative normative = findById(id);
        storageService.deleteByEntityAndCategory("Normative", id, FileCategory.NORMATIVE_FILE);
        normative.clearFile();
        return normativeRepository.save(normative);
    }

    @Transactional
    public void delete(UUID id) {
        LOGGER.info("Deleting normative: {}", id);
        Normative normative = findById(id);
        storageService.deleteByEntity("Normative", id);
        normativeRepository.delete(normative);
        LOGGER.info("Normative {} deleted successfully", id);
    }

    private UUID getCurrentInstitutionId() {
        UUID institutionId = TenantContext.getCurrentInstitution();
        if (institutionId == null) {
            throw new BadRequestException("No institution context set. Please select an institution.");
        }
        return institutionId;
    }

    private void validateTenantAccess(Normative normative) {
        UUID currentInstitutionId = getCurrentInstitutionId();
        if (!normative.getInstitution().getId().equals(currentInstitutionId)) {
            throw new UnauthorizedAccessException("You do not have permission to access this normative");
        }
    }
}
