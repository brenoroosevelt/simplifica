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
 * Data Transfer Object for updating user roles within an institution.
 *
 * Used by administrators and managers to modify the roles a user
 * has in a specific institution. At least one role must be provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesRequest {

    @NotNull(message = "Institution ID is required")
    private UUID institutionId;

    @NotEmpty(message = "At least one role is required")
    private Set<InstitutionRole> roles;
}
