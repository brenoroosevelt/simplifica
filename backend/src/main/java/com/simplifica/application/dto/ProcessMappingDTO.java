package com.simplifica.application.dto;

import com.simplifica.domain.entity.ProcessMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for ProcessMapping entity.
 *
 * Used to transfer process mapping file data between layers without exposing
 * the entity directly. Represents an uploaded HTML file for process visualization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMappingDTO {

    private UUID id;
    private UUID processId;
    private String fileUrl;
    private String filename;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    /**
     * Converts a ProcessMapping entity to a DTO.
     *
     * @param mapping the process mapping entity
     * @return the process mapping DTO, or null if the input is null
     */
    public static ProcessMappingDTO fromEntity(ProcessMapping mapping) {
        if (mapping == null) {
            return null;
        }

        return ProcessMappingDTO.builder()
                .id(mapping.getId())
                .processId(mapping.getProcess().getId())
                .fileUrl(mapping.getFileUrl())
                .filename(mapping.getFilename())
                .fileSize(mapping.getFileSize())
                .uploadedAt(mapping.getUploadedAt())
                .build();
    }
}
