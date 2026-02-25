package com.simplifica.presentation.controller;

import com.simplifica.application.dto.CreateNormativeDTO;
import com.simplifica.application.dto.NormativeDTO;
import com.simplifica.application.dto.UpdateNormativeDTO;
import com.simplifica.application.service.NormativeService;
import com.simplifica.domain.entity.Normative;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/normatives")
public class NormativeController {

    @Autowired
    private NormativeService normativeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<NormativeDTO>> listNormatives(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Normative> normatives = normativeService.findAll(search, pageable);
        return ResponseEntity.ok(normatives.map(NormativeDTO::fromEntity));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NormativeDTO> getNormative(@PathVariable UUID id) {
        return ResponseEntity.ok(NormativeDTO.fromEntity(normativeService.findById(id)));
    }

    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NormativeDTO> createNormative(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String externalLink,
            @RequestPart(required = false) MultipartFile file) {

        CreateNormativeDTO dto = CreateNormativeDTO.builder()
                .title(title)
                .description(description)
                .externalLink(externalLink)
                .build();

        Normative normative = normativeService.create(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(NormativeDTO.fromEntity(normative));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data", "application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NormativeDTO> updateNormative(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String externalLink,
            @RequestPart(required = false) MultipartFile file) {

        UpdateNormativeDTO dto = UpdateNormativeDTO.builder()
                .title(title)
                .description(description)
                .externalLink(externalLink)
                .build();

        Normative normative = normativeService.update(id, dto, file);
        return ResponseEntity.ok(NormativeDTO.fromEntity(normative));
    }

    @DeleteMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NormativeDTO> deleteNormativeFile(@PathVariable UUID id) {
        return ResponseEntity.ok(NormativeDTO.fromEntity(normativeService.deleteFile(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteNormative(@PathVariable UUID id) {
        normativeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
