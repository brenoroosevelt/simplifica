package com.simplifica.application.dto;

import com.simplifica.domain.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating user information.
 *
 * Used by administrators to update basic user fields such as
 * name and status. Email and provider information cannot be
 * changed as they are tied to OAuth2 authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @NotNull(message = "Status is required")
    private UserStatus status;
}
