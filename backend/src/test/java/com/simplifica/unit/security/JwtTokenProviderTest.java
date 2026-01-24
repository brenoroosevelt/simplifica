package com.simplifica.unit.security;

import com.simplifica.config.security.UserPrincipal;
import com.simplifica.config.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        String jwtSecret = "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hs256-algorithm";
        long expirationMs = 3600000L; // 1 hour

        tokenProvider = new JwtTokenProvider(jwtSecret, expirationMs);
        testUserId = UUID.randomUUID();
    }

    @Test
    void shouldGenerateTokenFromAuthentication() {
        UserPrincipal userPrincipal = new UserPrincipal(
                testUserId,
                "test@example.com",
                "Test User",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                Collections.emptyMap(),
                null
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );

        String token = tokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldGenerateTokenFromUserId() {
        String token = tokenProvider.generateTokenFromUserId(testUserId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldValidateValidToken() {
        String token = tokenProvider.generateTokenFromUserId(testUserId);

        boolean isValid = tokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldNotValidateInvalidToken() {
        String invalidToken = "invalid.token.value";

        boolean isValid = tokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = tokenProvider.generateTokenFromUserId(testUserId);

        UUID extractedUserId = tokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(testUserId);
    }

    @Test
    void shouldThrowExceptionForInvalidTokenFormat() {
        String invalidToken = "invalid.token.value";

        assertThatThrownBy(() -> tokenProvider.getUserIdFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }
}
