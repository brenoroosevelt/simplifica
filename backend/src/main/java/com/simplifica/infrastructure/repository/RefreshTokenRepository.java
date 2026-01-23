package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.RefreshToken;
import com.simplifica.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RefreshToken entity.
 *
 * Provides data access methods for refresh token operations including
 * finding tokens, deleting expired tokens, and managing user tokens.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a refresh token by its token value.
     *
     * @param token the token string to search for
     * @return an Optional containing the RefreshToken if found, empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with a specific user.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);

    /**
     * Deletes all refresh tokens that have expired before the given timestamp.
     *
     * @param now the current timestamp to compare against
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
}
