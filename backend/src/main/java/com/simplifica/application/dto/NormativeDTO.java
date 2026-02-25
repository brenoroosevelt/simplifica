package com.simplifica.application.dto;

import com.simplifica.domain.entity.Normative;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormativeDTO {

    private UUID id;
    private UUID institutionId;
    private String institutionName;
    private String institutionAcronym;
    private String title;
    private String description;
    private String fileUrl;
    private String fileOriginalName;
    private String externalLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NormativeDTO fromEntity(Normative normative) {
        if (normative == null) return null;
        return NormativeDTO.builder()
                .id(normative.getId())
                .institutionId(normative.getInstitution().getId())
                .institutionName(normative.getInstitution().getName())
                .institutionAcronym(normative.getInstitution().getAcronym())
                .title(normative.getTitle())
                .description(normative.getDescription())
                .fileUrl(normative.getFileUrl())
                .fileOriginalName(normative.getFileOriginalName())
                .externalLink(normative.getExternalLink())
                .createdAt(normative.getCreatedAt())
                .updatedAt(normative.getUpdatedAt())
                .build();
    }
}
