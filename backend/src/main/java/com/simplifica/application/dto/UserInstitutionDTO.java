package com.simplifica.application.dto;

import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.UserInstitution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for UserInstitution entity.
 *
 * Represents the relationship between a user and an institution,
 * including the user's roles within that institution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInstitutionDTO {

    private UUID id;
    private UUID userId;
    private UUID institutionId;
    private UserDTO user;
    private InstitutionDTO institution;
    private Set<InstitutionRole> roles;
    private Boolean active;
    private LocalDateTime linkedAt;
    private UUID linkedBy;
    private LocalDateTime updatedAt;

    /**
     * Converts a UserInstitution entity to a DTO.
     *
     * @param userInstitution the user-institution entity
     * @return the user-institution DTO, or null if the input is null
     */
    public static UserInstitutionDTO fromEntity(UserInstitution userInstitution) {
        if (userInstitution == null) {
            return null;
        }

        return UserInstitutionDTO.builder()
                .id(userInstitution.getId())
                .userId(userInstitution.getUser() != null ? userInstitution.getUser().getId() : null)
                .institutionId(userInstitution.getInstitution() != null ? userInstitution.getInstitution().getId() : null)
                .user(UserDTO.fromEntity(userInstitution.getUser()))
                .institution(InstitutionDTO.fromEntity(userInstitution.getInstitution()))
                .roles(userInstitution.getRoles())
                .active(userInstitution.getActive())
                .linkedAt(userInstitution.getLinkedAt())
                .linkedBy(userInstitution.getLinkedBy() != null
                        ? userInstitution.getLinkedBy().getId()
                        : null)
                .updatedAt(userInstitution.getUpdatedAt())
                .build();
    }

    /**
     * Converts a UserInstitution entity to a simplified DTO.
     *
     * This version only includes basic institution information without
     * full user details, useful for listing institutions for a user.
     *
     * @param userInstitution the user-institution entity
     * @return the simplified user-institution DTO
     */
    public static UserInstitutionDTO fromEntitySimplified(UserInstitution userInstitution) {
        if (userInstitution == null) {
            return null;
        }

        return UserInstitutionDTO.builder()
                .id(userInstitution.getId())
                .userId(userInstitution.getUser() != null ? userInstitution.getUser().getId() : null)
                .institutionId(userInstitution.getInstitution() != null ? userInstitution.getInstitution().getId() : null)
                .institution(InstitutionDTO.fromEntity(userInstitution.getInstitution()))
                .roles(userInstitution.getRoles())
                .active(userInstitution.getActive())
                .linkedAt(userInstitution.getLinkedAt())
                .build();
    }
}
