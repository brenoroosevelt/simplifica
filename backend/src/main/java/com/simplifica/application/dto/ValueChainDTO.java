package com.simplifica.application.dto;

import com.simplifica.domain.entity.ValueChain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for ValueChain entity.
 *
 * Used to transfer value chain data between layers without exposing
 * the entity directly. Contains all value chain information including
 * institution details and image URLs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueChainDTO {

    private UUID id;
    private UUID institutionId;
    private String institutionName;
    private String institutionAcronym;
    private String name;
    private String description;
    private String imageUrl;
    private String imageThumbnailUrl;
    private LocalDateTime imageUploadedAt;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a ValueChain entity to a DTO.
     *
     * @param valueChain the value chain entity
     * @return the value chain DTO, or null if the input is null
     */
    public static ValueChainDTO fromEntity(ValueChain valueChain) {
        if (valueChain == null) {
            return null;
        }

        return ValueChainDTO.builder()
                .id(valueChain.getId())
                .institutionId(valueChain.getInstitution().getId())
                .institutionName(valueChain.getInstitution().getName())
                .institutionAcronym(valueChain.getInstitution().getAcronym())
                .name(valueChain.getName())
                .description(valueChain.getDescription())
                .imageUrl(valueChain.getImageUrl())
                .imageThumbnailUrl(valueChain.getImageThumbnailUrl())
                .imageUploadedAt(valueChain.getImageUploadedAt())
                .active(valueChain.getActive())
                .createdAt(valueChain.getCreatedAt())
                .updatedAt(valueChain.getUpdatedAt())
                .build();
    }
}
