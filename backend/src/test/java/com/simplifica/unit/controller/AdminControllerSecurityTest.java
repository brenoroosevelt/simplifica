package com.simplifica.unit.controller;

import com.simplifica.application.dto.LinkUserInstitutionRequest;
import com.simplifica.application.service.UserAdminService;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.presentation.controller.AdminController;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Security tests for AdminController.
 *
 * Validates that only SIMP-ADMIN administrators can link/unlink users from institutions.
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerSecurityTest {

    @Mock
    private UserAdminService userAdminService;

    @InjectMocks
    private AdminController adminController;

    private UUID userId;
    private UUID institutionId;
    private UserPrincipal simpAdminPrincipal;
    private UserPrincipal managerPrincipal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        institutionId = UUID.randomUUID();

        // SIMP-ADMIN administrator (has ROLE_ADMIN)
        simpAdminPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "admin@simplifica.com",
                "System Admin",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")),
                Collections.emptyMap(),
                institutionId
        );

        // Institution manager (has ROLE_MANAGER, not ROLE_ADMIN)
        managerPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "manager@institution.com",
                "Institution Manager",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER")),
                Collections.emptyMap(),
                institutionId
        );
    }

    @Test
    void shouldAllowSimpAdminToLinkUserToInstitution() {
        // Arrange
        LinkUserInstitutionRequest request = new LinkUserInstitutionRequest();
        request.setInstitutionId(institutionId);
        request.setRoles(Set.of(InstitutionRole.VIEWER));

        // Act
        adminController.linkUserToInstitution(userId, request, simpAdminPrincipal);

        // Assert
        verify(userAdminService, times(1)).linkUserToInstitution(
                userId,
                request,
                simpAdminPrincipal.getId(),
                true // isAdmin = true
        );
    }

    @Test
    void shouldRejectManagerFromLinkingUserToInstitution() {
        // Arrange
        LinkUserInstitutionRequest request = new LinkUserInstitutionRequest();
        request.setInstitutionId(institutionId);
        request.setRoles(Set.of(InstitutionRole.VIEWER));

        // Act & Assert
        assertThatThrownBy(() ->
                adminController.linkUserToInstitution(userId, request, managerPrincipal))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Only SIMP-ADMIN administrators can link users to institutions");

        // Verify service was never called
        verify(userAdminService, never()).linkUserToInstitution(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldAllowSimpAdminToUnlinkUserFromInstitution() {
        // Act
        adminController.unlinkUserFromInstitution(userId, institutionId, simpAdminPrincipal);

        // Assert
        verify(userAdminService, times(1)).unlinkUserFromInstitution(
                userId,
                institutionId,
                simpAdminPrincipal.getId(),
                true // isAdmin = true
        );
    }

    @Test
    void shouldRejectManagerFromUnlinkingUserFromInstitution() {
        // Act & Assert
        assertThatThrownBy(() ->
                adminController.unlinkUserFromInstitution(userId, institutionId, managerPrincipal))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Only SIMP-ADMIN administrators can unlink users from institutions");

        // Verify service was never called
        verify(userAdminService, never()).unlinkUserFromInstitution(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldRejectUserWithoutAdminRole() {
        // Arrange - user with no admin role at all
        UserPrincipal regularUserPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "user@institution.com",
                "Regular User",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                Collections.emptyMap(),
                institutionId
        );

        LinkUserInstitutionRequest request = new LinkUserInstitutionRequest();
        request.setInstitutionId(institutionId);
        request.setRoles(Set.of(InstitutionRole.VIEWER));

        // Act & Assert - link
        assertThatThrownBy(() ->
                adminController.linkUserToInstitution(userId, request, regularUserPrincipal))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Only SIMP-ADMIN administrators can link users to institutions");

        // Act & Assert - unlink
        assertThatThrownBy(() ->
                adminController.unlinkUserFromInstitution(userId, institutionId, regularUserPrincipal))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Only SIMP-ADMIN administrators can unlink users from institutions");

        // Verify service was never called
        verify(userAdminService, never()).linkUserToInstitution(any(), any(), any(), anyBoolean());
        verify(userAdminService, never()).unlinkUserFromInstitution(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldAllowSimpAdminToLinkUserWithMultipleRoles() {
        // Arrange
        LinkUserInstitutionRequest request = new LinkUserInstitutionRequest();
        request.setInstitutionId(institutionId);
        request.setRoles(Set.of(InstitutionRole.MANAGER, InstitutionRole.VIEWER));

        // Act
        adminController.linkUserToInstitution(userId, request, simpAdminPrincipal);

        // Assert
        verify(userAdminService, times(1)).linkUserToInstitution(
                userId,
                request,
                simpAdminPrincipal.getId(),
                true // isAdmin = true
        );
    }
}
