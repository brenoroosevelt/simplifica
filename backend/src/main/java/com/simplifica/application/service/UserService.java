package com.simplifica.application.service;

import com.simplifica.domain.entity.User;
import com.simplifica.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for user-related business logic.
 *
 * This service handles user operations such as finding users by ID or email.
 * Future enhancements may include user activation, deactivation, role management, etc.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Finds a user by their ID.
     *
     * @param id the user's UUID
     * @return the User entity
     * @throws RuntimeException if the user is not found
     */
    public User findById(UUID id) {
        LOGGER.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email address
     * @return the User entity
     * @throws RuntimeException if the user is not found
     */
    public User findByEmail(String email) {
        LOGGER.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email address to check
     * @return true if a user exists with this email, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
