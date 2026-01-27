package com.simplifica.unit.service;

import com.simplifica.application.service.InstitutionService;
import com.simplifica.application.service.UserAdminService;
import com.simplifica.application.service.UserService;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import com.simplifica.infrastructure.repository.UserRepository;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for UserAdminService.unlinkUserFromInstitution method.
 *
 * Tests verify that unlinking works correctly and that the link is properly deactivated.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAdminService - Unlink User from Institution Tests")
class UserAdminServiceUnlinkTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInstitutionRepository userInstitutionRepository;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private UserService userService;

    @Mock
    private com.simplifica.application.service.AuditService auditService;

    @InjectMocks
    private UserAdminService userAdminService;

    private UUID userId;
    private UUID institutionId;
    private User user;
    private Institution institution;
    private UserInstitution userInstitution;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        institutionId = UUID.randomUUID();

        // Create user
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .status(UserStatus.ACTIVE)
                .institutions(new HashSet<>())
                .build();

        // Create institution
        institution = Institution.builder()
                .id(institutionId)
                .name("Test Institution")
                .acronym("TEST")
                .active(true)
                .build();

        // Create ACTIVE user-institution link
        userInstitution = UserInstitution.builder()
                .id(UUID.randomUUID())
                .user(user)
                .institution(institution)
                .roles(Set.of(InstitutionRole.VIEWER))
                .active(true)  // ACTIVE
                .build();

        // Add to user's institutions
        user.getInstitutions().add(userInstitution);
    }

    @Test
    @DisplayName("Should successfully unlink user from institution")
    void shouldSuccessfullyUnlinkUserFromInstitution() {
        // Arrange
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, institutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(userId)).thenReturn(user);
        when(institutionService.findById(institutionId)).thenReturn(institution);
        when(userInstitutionRepository.save(any(UserInstitution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Verify initial state
        assertThat(userInstitution.getActive()).isTrue();
        assertThat(userInstitution.isActive()).isTrue();

        // Act
        userAdminService.unlinkUserFromInstitution(userId, institutionId, UUID.randomUUID(), true);

        // Assert - Verify the link was deactivated
        ArgumentCaptor<UserInstitution> captor = ArgumentCaptor.forClass(UserInstitution.class);
        verify(userInstitutionRepository, times(1)).save(captor.capture());

        UserInstitution savedLink = captor.getValue();
        assertThat(savedLink.getActive()).isFalse();
        assertThat(savedLink.isActive()).isFalse();
        assertThat(savedLink.getId()).isEqualTo(userInstitution.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when link does not exist")
    void shouldThrowExceptionWhenLinkNotFound() {
        // Arrange
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, institutionId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                userAdminService.unlinkUserFromInstitution(userId, institutionId, UUID.randomUUID(), true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User-Institution link not found");

        // Verify save was never called
        verify(userInstitutionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set user status to PENDING when last institution is removed")
    void shouldSetUserToPendingWhenLastInstitutionRemoved() {
        // Arrange - User has only one institution
        user.setStatus(UserStatus.ACTIVE);
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, institutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(userId)).thenReturn(user);
        when(institutionService.findById(institutionId)).thenReturn(institution);
        when(userInstitutionRepository.save(any(UserInstitution.class)))
                .thenAnswer(invocation -> {
                    UserInstitution ui = invocation.getArgument(0);
                    ui.setActive(false); // Simulate the deactivation
                    return ui;
                });
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userAdminService.unlinkUserFromInstitution(userId, institutionId, UUID.randomUUID(), true);

        // Assert - Verify user status was changed to PENDING
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("Should NOT change user status when other active institutions remain")
    void shouldNotChangeUserStatusWhenOtherInstitutionsRemain() {
        // Arrange - User has TWO institutions
        Institution otherInstitution = Institution.builder()
                .id(UUID.randomUUID())
                .name("Other Institution")
                .acronym("OTHER")
                .active(true)
                .build();

        UserInstitution otherLink = UserInstitution.builder()
                .id(UUID.randomUUID())
                .user(user)
                .institution(otherInstitution)
                .roles(Set.of(InstitutionRole.VIEWER))
                .active(true)  // Still ACTIVE
                .build();

        user.getInstitutions().add(otherLink);
        user.setStatus(UserStatus.ACTIVE);

        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, institutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(userId)).thenReturn(user);
        when(institutionService.findById(institutionId)).thenReturn(institution);
        when(userInstitutionRepository.save(any(UserInstitution.class)))
                .thenAnswer(invocation -> {
                    UserInstitution ui = invocation.getArgument(0);
                    ui.setActive(false); // Simulate the deactivation
                    return ui;
                });

        // Act
        userAdminService.unlinkUserFromInstitution(userId, institutionId, UUID.randomUUID(), true);

        // Assert - Verify user status was NOT changed (still ACTIVE)
        // Since there's still one active institution, status should remain ACTIVE
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle unlinking already inactive link")
    void shouldHandleUnlinkingAlreadyInactiveLink() {
        // Arrange - Link is already inactive
        userInstitution.setActive(false);

        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, institutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(userId)).thenReturn(user);
        when(institutionService.findById(institutionId)).thenReturn(institution);
        when(userInstitutionRepository.save(any(UserInstitution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Should not throw exception
        userAdminService.unlinkUserFromInstitution(userId, institutionId, UUID.randomUUID(), true);

        // Assert - Should still save (idempotent operation)
        ArgumentCaptor<UserInstitution> captor = ArgumentCaptor.forClass(UserInstitution.class);
        verify(userInstitutionRepository, times(1)).save(captor.capture());

        UserInstitution savedLink = captor.getValue();
        assertThat(savedLink.getActive()).isFalse();
    }

    @Test
    @DisplayName("Should verify active flag is persisted correctly")
    void shouldVerifyActiveFlagIsPersisted() {
        // Arrange
        when(userInstitutionRepository.findByUserIdAndInstitutionId(userId, institutionId))
                .thenReturn(Optional.of(userInstitution));
        when(userService.findById(userId)).thenReturn(user);
        when(institutionService.findById(institutionId)).thenReturn(institution);

        // Track what gets saved
        ArgumentCaptor<UserInstitution> captor = ArgumentCaptor.forClass(UserInstitution.class);
        when(userInstitutionRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userAdminService.unlinkUserFromInstitution(userId, institutionId, UUID.randomUUID(), true);

        // Assert - Verify the EXACT state being persisted
        UserInstitution savedEntity = captor.getValue();
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isEqualTo(userInstitution.getId());
        assertThat(savedEntity.getActive()).isFalse();
        assertThat(savedEntity.isActive()).isFalse();

        // Verify all repository interactions
        verify(userInstitutionRepository, times(1))
                .findByUserIdAndInstitutionId(userId, institutionId);
        verify(userInstitutionRepository, times(1))
                .save(any(UserInstitution.class));
    }
}
