package com.simplifica.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication response.
 *
 * This DTO is returned after successful authentication,
 * containing the JWT access token and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserDTO user;
}
