package com.simplifica.application.dto;

import com.simplifica.domain.entity.InstitutionRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for assigning a user to an institution.
 *
 * Used when creating a new user-institution relationship.
 * Requires user ID, institution ID, and at least one role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserToInstitutionDTO {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Institution ID is required")
    private UUID institutionId;

    @NotEmpty(message = "At least one role is required")
    private Set<InstitutionRole> roles;
}
