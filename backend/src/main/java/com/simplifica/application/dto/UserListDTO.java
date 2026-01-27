package com.simplifica.application.dto;

import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for user list items.
 *
 * Contains a simplified view of user data for listing purposes,
 * including institution count and institution summaries for admin view.
 * Optimized for performance in paginated list views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {

    private UUID id;
    private String email;
    private String name;
    private String pictureUrl;
    private OAuth2Provider provider;
    private UserStatus status;
    private LocalDateTime createdAt;
    private Long institutionCount;

    /**
     * Lista simplificada de instituições vinculadas ao usuário.
     * Visível apenas para admins.
     */
    @Builder.Default
    private List<UserInstitutionSummaryDTO> institutions = new ArrayList<>();

    /**
     * Creates a UserListDTO from a User entity.
     *
     * DEPRECATED: Use UserAdminService.listUsers instead.
     * This method loads all institutions which causes N+1 problems.
     *
     * @param user the User entity
     * @return a UserListDTO instance
     */
    @Deprecated(since = "1.1", forRemoval = false)
    public static UserListDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .institutionCount((long) user.getActiveInstitutions().size())
                .build();
    }

    /**
     * Creates a UserListDTO with explicit institution count.
     * Preferred method to avoid N+1 queries.
     *
     * @param user the User entity
     * @param institutionCount pre-calculated institution count
     * @return a UserListDTO instance
     */
    public static UserListDTO fromEntity(User user, Long institutionCount) {
        if (user == null) {
            return null;
        }

        return UserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .institutionCount(institutionCount != null ? institutionCount : 0L)
                .build();
    }
}
