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
 * Data Transfer Object for linking a user to an institution.
 *
 * Used by administrators to create a new user-institution relationship
 * with specific roles. The requesting user is recorded as linkedBy for
 * audit purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkUserInstitutionRequest {

    @NotNull(message = "Institution ID is required")
    private UUID institutionId;

    @NotEmpty(message = "At least one role is required")
    private Set<InstitutionRole> roles;
}
