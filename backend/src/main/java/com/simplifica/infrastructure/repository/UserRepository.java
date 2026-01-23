package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 *
 * Provides data access methods for user-related operations including
 * finding users by email, OAuth2 provider credentials, and checking existence.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email address
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their OAuth2 provider and provider ID.
     *
     * @param provider the OAuth2 provider (GOOGLE, MICROSOFT)
     * @param providerId the unique identifier from the OAuth2 provider
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByProviderAndProviderId(OAuth2Provider provider, String providerId);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
}
