package com.simplifica.unit.service;

import com.simplifica.application.dto.LinkUserInstitutionRequest;
import com.simplifica.application.dto.UpdateUserRolesRequest;
import com.simplifica.application.service.AuditService;
import com.simplifica.application.service.InstitutionService;
import com.simplifica.application.service.UserAdminService;
import com.simplifica.application.service.UserService;
import com.simplifica.domain.constants.InstitutionConstants;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.InstitutionType;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import com.simplifica.infrastructure.repository.UserRepository;
import com.simplifica.presentation.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Security tests for UserAdminService.
 *
 * Validates that ADMIN role can only be assigned to users in the SIMP-ADMIN institution.
 */
@ExtendWith(MockitoExtension.class)
class UserAdminServiceSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInstitutionRepository userInstitutionRepository;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserAdminService userAdminService;

    private UUID userId;
    private UUID adminInstitutionId;
    private UUID regularInstitutionId;
    private UUID requestingUserId;
    private User testUser;
    private User requestingUser;
    private Institution adminInstitution;
    private Institution regularInstitution;
    private UserInstitution userInstitution;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminInstitutionId = UUID.randomUUID();
        regularInstitutionId = UUID.randomUUID();
        requestingUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .name("Test User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-123")
                .status(UserStatus.ACTIVE)
                .build();

        requestingUser = User.builder()
                .id(requestingUserId)
                .email("admin@example.com")
                .name("Admin User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-456")
                .status(UserStatus.ACTIVE)
                .build();

        adminInstitution = Institution.builder()
                .id(adminInstitutionId)
                .name("Simplifica Admin")
                .acronym(InstitutionConstants.ADMIN_INSTITUTION_ACRONYM)
                .type(InstitutionType.FEDERAL)
                .active(true)
                .build();

        regularInstitution = Institution.builder()
                .id(regularInstitutionId)
                .name("Regular University")
                .acronym("REG-UNIV")
                .type(InstitutionType.ESTADUAL)
                .active(true)
                .build();

        userInstitution = UserInstitution.builder()
                .user(testUser)
                .institution(regularInstitution)
                .roles(Set.of(InstitutionRole.MANAGER))
                .active(true)
                .build();
    }

    @Test
    void shouldAllowAdminRoleInAdminInstitution_WhenUpdatingRoles() {
        // Arrange
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(adminInstitutionId);
        request.setRoles(Set.of(InstitutionRole.ADMIN));

        userInstitution.setInstitution(adminInstitution);

        when(institutionService.findById(adminInstitutionId)).thenReturn(adminInstitution);
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, adminInstitutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(requestingUserId)).thenReturn(requestingUser);
        when(userService.findById(userId)).thenReturn(testUser);

        // Act & Assert - should not throw exception
        userAdminService.updateUserRoles(userId, request, requestingUserId, true, null);

        // Verify audit was called
        verify(auditService).logUserRolesUpdate(
                any(User.class), any(User.class), any(UUID.class), anyString(), anySet());
    }

    @Test
    void shouldRejectAdminRoleInRegularInstitution_WhenUpdatingRoles() {
        // Arrange
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(regularInstitutionId);
        request.setRoles(Set.of(InstitutionRole.ADMIN, InstitutionRole.MANAGER));

        when(institutionService.findById(regularInstitutionId)).thenReturn(regularInstitution);

        // Act & Assert
        assertThatThrownBy(() ->
                userAdminService.updateUserRoles(userId, request, requestingUserId, true, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN role can only be assigned to users in the SIMP-ADMIN institution");

        // Verify no changes were persisted
        verify(userInstitutionRepository, never()).save(any());
        verify(auditService, never()).logUserRolesUpdate(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAllowAdminRoleInAdminInstitution_WhenLinkingUser() {
        // Arrange
        LinkUserInstitutionRequest request = new LinkUserInstitutionRequest();
        request.setInstitutionId(adminInstitutionId);
        request.setRoles(Set.of(InstitutionRole.ADMIN));

        when(userService.findById(userId)).thenReturn(testUser);
        when(institutionService.findById(adminInstitutionId)).thenReturn(adminInstitution);
        when(userService.findById(requestingUserId)).thenReturn(requestingUser);
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, adminInstitutionId))
                .thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        userAdminService.linkUserToInstitution(userId, request, requestingUserId, true);

        // Verify link was created
        verify(userInstitutionRepository).save(any(UserInstitution.class));
        verify(auditService).logUserLinkedToInstitution(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectAdminRoleInRegularInstitution_WhenLinkingUser() {
        // Arrange
        LinkUserInstitutionRequest request = new LinkUserInstitutionRequest();
        request.setInstitutionId(regularInstitutionId);
        request.setRoles(Set.of(InstitutionRole.ADMIN, InstitutionRole.VIEWER));

        when(userService.findById(userId)).thenReturn(testUser);
        when(institutionService.findById(regularInstitutionId)).thenReturn(regularInstitution);
        when(userService.findById(requestingUserId)).thenReturn(requestingUser);

        // Act & Assert
        assertThatThrownBy(() ->
                userAdminService.linkUserToInstitution(userId, request, requestingUserId, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN role can only be assigned to users in the SIMP-ADMIN institution");

        // Verify no link was created
        verify(userInstitutionRepository, never()).save(any());
        verify(auditService, never()).logUserLinkedToInstitution(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAllowManagerAndViewerRolesInRegularInstitution() {
        // Arrange
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(regularInstitutionId);
        request.setRoles(Set.of(InstitutionRole.MANAGER, InstitutionRole.VIEWER));

        when(institutionService.findById(regularInstitutionId)).thenReturn(regularInstitution);
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, regularInstitutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(requestingUserId)).thenReturn(requestingUser);
        when(userService.findById(userId)).thenReturn(testUser);

        // Act & Assert - should not throw exception
        userAdminService.updateUserRoles(userId, request, requestingUserId, true, null);

        // Verify changes were persisted
        verify(userInstitutionRepository).save(any(UserInstitution.class));
        verify(auditService).logUserRolesUpdate(any(), any(), any(), any(), any());
    }
}
