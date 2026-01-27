package com.simplifica.application.dto;

import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for User entity.
 *
 * This DTO represents user data sent to the frontend API.
 * It excludes sensitive information and provides a clean interface
 * for client applications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private UUID id;
    private String email;
    private String name;
    private String pictureUrl;
    private OAuth2Provider provider;
    private UserStatus status;
    private LocalDateTime createdAt;

    /**
     * Creates a UserDTO from a User entity.
     *
     * @param user the User entity
     * @return a UserDTO instance
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
