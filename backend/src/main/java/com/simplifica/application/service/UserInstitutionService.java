package com.simplifica.application.service;

import com.simplifica.application.dto.AssignUserToInstitutionDTO;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import com.simplifica.presentation.exception.ResourceAlreadyExistsException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing user-institution relationships.
 *
 * This service handles linking/unlinking users to institutions, managing roles,
 * and sending email notifications when configured.
 */
@Service
@Transactional(readOnly = true)
public class UserInstitutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInstitutionService.class);

    @Autowired
    private UserInstitutionRepository userInstitutionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Value("${app.features.email-notifications:false}")
    private boolean emailNotificationsEnabled;

    /**
     * Assigns a user to an institution with specific roles.
     *
     * @param dto the assignment data containing userId, institutionId and roles
     * @return the created UserInstitution entity
     * @throws ResourceNotFoundException if user or institution not found
     * @throws ResourceAlreadyExistsException if link already exists
     */
    @Transactional
    public UserInstitution assignUserToInstitution(AssignUserToInstitutionDTO dto) {
        LOGGER.info("Assigning user {} to institution {}", dto.getUserId(), dto.getInstitutionId());

        User user = userService.findById(dto.getUserId());
        Institution institution = institutionService.findById(dto.getInstitutionId());

        if (userInstitutionRepository.existsByUserIdAndInstitutionId(
                dto.getUserId(), dto.getInstitutionId())) {
            throw new ResourceAlreadyExistsException(
                    "User-Institution link already exists");
        }

        UserInstitution userInstitution = UserInstitution.builder()
                .user(user)
                .institution(institution)
                .roles(dto.getRoles())
                .active(true)
                .build();

        UserInstitution saved = userInstitutionRepository.save(userInstitution);
        LOGGER.info("User {} assigned to institution {} with roles: {}",
                    user.getId(), institution.getId(), dto.getRoles());

        if (emailNotificationsEnabled) {
            emailNotificationService.sendUserAssignedToInstitutionEmail(
                    user, institution, dto.getRoles());
        }

        return saved;
    }

    /**
     * Updates the roles of a user in an institution.
     *
     * @param userId the user's UUID
     * @param institutionId the institution's UUID
     * @param roles the new set of roles
     * @return the updated UserInstitution entity
     * @throws ResourceNotFoundException if the link does not exist
     */
    @Transactional
    public UserInstitution updateRoles(UUID userId, UUID institutionId, Set<InstitutionRole> roles) {
        LOGGER.info("Updating roles for user {} in institution {}", userId, institutionId);

        UserInstitution userInstitution = userInstitutionRepository
                .findByUserIdAndInstitutionId(userId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

        userInstitution.setRoles(roles);
        UserInstitution saved = userInstitutionRepository.save(userInstitution);
        LOGGER.info("Roles updated for user {} in institution {}: {}",
                    userId, institutionId, roles);
        return saved;
    }

    /**
     * Removes a user from an institution by soft deleting the link.
     *
     * @param userId the user's UUID
     * @param institutionId the institution's UUID
     * @throws ResourceNotFoundException if the link does not exist
     */
    @Transactional
    public void removeUserFromInstitution(UUID userId, UUID institutionId) {
        LOGGER.info("Removing user {} from institution {}", userId, institutionId);

        UserInstitution userInstitution = userInstitutionRepository
                .findByUserIdAndInstitutionId(userId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

        User user = userInstitution.getUser();
        Institution institution = userInstitution.getInstitution();

        userInstitution.setActive(false);
        userInstitutionRepository.save(userInstitution);
        LOGGER.info("User {} removed from institution {}", userId, institutionId);

        if (emailNotificationsEnabled) {
            emailNotificationService.sendUserRemovedFromInstitutionEmail(user, institution);
        }
    }

    /**
     * Gets all active institutions for a user.
     *
     * @param userId the user's UUID
     * @return list of active UserInstitution entities with eagerly loaded institutions
     */
    public List<UserInstitution> getUserInstitutions(UUID userId) {
        LOGGER.debug("Fetching institutions for user: {}", userId);
        return userInstitutionRepository.findActiveInstitutionsWithUser(userId);
    }

    /**
     * Gets all active users for an institution.
     *
     * @param institutionId the institution's UUID
     * @return list of active UserInstitution entities with user and institution eagerly loaded
     */
    public List<UserInstitution> getInstitutionUsers(UUID institutionId) {
        LOGGER.debug("Fetching users for institution: {}", institutionId);
        return userInstitutionRepository.findActiveUsersWithInstitution(institutionId);
    }

    /**
     * Checks if a user belongs to an institution (active link).
     *
     * @param userId the user's UUID
     * @param institutionId the institution's UUID
     * @return true if the user has an active link to the institution
     */
    public boolean userBelongsToInstitution(UUID userId, UUID institutionId) {
        LOGGER.debug("Checking if user {} belongs to institution {}", userId, institutionId);
        return userInstitutionRepository
                .findActiveByUserAndInstitution(userId, institutionId)
                .isPresent();
    }
}
