package com.simplifica.unit.service;

import com.simplifica.application.service.UserService;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .name("Test User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-123")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void shouldFindUserById() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findById(testUserId);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(testUserId);
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    void shouldFindUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = userService.findByEmail("test@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail(nonExistentEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    void shouldReturnTrueWhenUserExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = userService.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByEmail() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean exists = userService.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }
}
