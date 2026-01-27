package com.simplifica.application.dto;

import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for detailed user information.
 *
 * Contains complete user data including all institution relationships
 * and roles. Used for user detail views and administrative operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

    private UUID id;
    private String email;
    private String name;
    private String pictureUrl;
    private OAuth2Provider provider;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserInstitutionDetailDTO> institutions;

    /**
     * Nested DTO representing a user's relationship with an institution.
     * Contains both the complete institution object and individual fields
     * for backward compatibility.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInstitutionDetailDTO {
        private UUID id;
        private UUID userId;
        private UUID institutionId;
        private String institutionName;
        private String institutionAcronym;
        private InstitutionDTO institution;
        private Set<InstitutionRole> roles;
        private Boolean active;
        private LocalDateTime linkedAt;
        private String linkedByEmail;
    }

    /**
     * Creates a UserDetailDTO from a User entity.
     *
     * @param user the User entity with institutions loaded
     * @return a UserDetailDTO instance
     */
    public static UserDetailDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        List<UserInstitutionDetailDTO> institutions = user.getInstitutions().stream()
                .filter(UserInstitution::isActive)  // BUGFIX: Filter only ACTIVE institutions
                .map(UserDetailDTO::mapUserInstitution)
                .collect(Collectors.toList());

        return UserDetailDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .institutions(institutions)
                .build();
    }

    /**
     * Maps a UserInstitution entity to a UserInstitutionDetailDTO.
     * Populates both the complete institution object and individual fields
     * for backward compatibility with existing frontend code.
     *
     * @param ui the UserInstitution entity
     * @return a UserInstitutionDetailDTO instance
     */
    private static UserInstitutionDetailDTO mapUserInstitution(UserInstitution ui) {
        return UserInstitutionDetailDTO.builder()
                .id(ui.getId())
                .userId(ui.getUser().getId())
                .institutionId(ui.getInstitution().getId())
                .institutionName(ui.getInstitution().getName())
                .institutionAcronym(ui.getInstitution().getAcronym())
                .institution(InstitutionDTO.fromEntity(ui.getInstitution()))
                .roles(ui.getRoles())
                .active(ui.getActive())
                .linkedAt(ui.getLinkedAt())
                .linkedByEmail(ui.getLinkedBy() != null ? ui.getLinkedBy().getEmail() : null)
                .build();
    }
}
