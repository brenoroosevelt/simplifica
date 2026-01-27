package com.simplifica.application.service;

import com.simplifica.config.security.UserPrincipal;
import com.simplifica.config.security.oauth.OAuth2UserInfo;
import com.simplifica.config.security.oauth.OAuth2UserInfoFactory;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.InstitutionRepository;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import com.simplifica.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * Custom OAuth2 User Service for processing OAuth2 authentication.
 *
 * This service extends the default OAuth2 user service to handle the complete
 * OAuth2 authentication flow:
 * 1. Receives user data from the OAuth2 provider
 * 2. Extracts user information using the appropriate provider strategy
 * 3. Creates a new user or updates existing user in the database
 * 4. Returns a UserPrincipal for Spring Security
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserInstitutionRepository userInstitutionRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            LOGGER.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + ex.getMessage());
        }
    }

    /**
     * Processes the OAuth2 user data and creates/updates the user in the database.
     *
     * @param userRequest the OAuth2 user request
     * @param oauth2User the OAuth2 user data from the provider
     * @return a UserPrincipal representing the authenticated user
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oauth2User.getAttributes()
        );

        if (!StringUtils.hasText(oauth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        OAuth2Provider provider = OAuth2Provider.fromString(registrationId);
        String providerId = oauth2UserInfo.getId();

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user = updateExistingUser(user, oauth2UserInfo);
        } else {
            user = createNewUser(provider, oauth2UserInfo);
        }

        // Reload user with institutions to determine authorities
        user = userRepository.findByIdWithInstitutions(user.getId())
                .orElse(user);

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    /**
     * Creates a new user from OAuth2 provider information.
     * Attempts to auto-link user to institution by email domain.
     *
     * @param provider the OAuth2 provider
     * @param userInfo the user information from the provider
     * @return the newly created user
     */
    private User createNewUser(OAuth2Provider provider, OAuth2UserInfo userInfo) {
        String email = userInfo.getEmail();
        String domain = extractDomain(email);

        User user = User.builder()
                .email(email)
                .name(userInfo.getName())
                .pictureUrl(userInfo.getImageUrl())
                .provider(provider)
                .providerId(userInfo.getId())
                .status(UserStatus.PENDING) // Start as PENDING, may change to ACTIVE if auto-linked
                .build();

        User savedUser = userRepository.save(user);
        LOGGER.info("Created new user: {} from provider: {}", savedUser.getEmail(), provider);

        // Attempt auto-link by email domain
        autoLinkUserByDomain(savedUser, domain);

        return savedUser;
    }

    /**
     * Extracts the domain from an email address.
     *
     * @param email the email address
     * @return the domain (e.g., "ufms.br" from "usuario@ufms.br")
     */
    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    /**
     * Attempts to automatically link a user to an institution by email domain.
     * If a matching institution is found:
     * - Creates user-institution link with VIEWER role
     * - Changes user status from PENDING to ACTIVE
     * - Logs audit trail
     *
     * @param user the user to link
     * @param domain the email domain
     */
    private void autoLinkUserByDomain(User user, String domain) {
        if (domain == null) {
            LOGGER.debug("No domain extracted for user: {}", user.getEmail());
            return;
        }

        Optional<Institution> institutionOpt = institutionRepository.findByDomain(domain);

        if (institutionOpt.isEmpty()) {
            LOGGER.info("No institution found for domain: {}. User {} remains PENDING.",
                       domain, user.getEmail());
            return;
        }

        Institution institution = institutionOpt.get();

        if (!institution.isActive()) {
            LOGGER.warn("Institution {} is inactive. User {} remains PENDING.",
                       institution.getName(), user.getEmail());
            return;
        }

        try {
            // Create user-institution link with VIEWER role
            UserInstitution userInstitution = UserInstitution.builder()
                    .user(user)
                    .institution(institution)
                    .roles(Set.of(InstitutionRole.VIEWER))
                    .active(true)
                    .build();

            userInstitutionRepository.save(userInstitution);

            // Change user status to ACTIVE
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);

            LOGGER.info("User {} auto-linked to institution {} via domain {} with VIEWER role",
                       user.getEmail(), institution.getName(), domain);

            // Log audit trail
            auditService.logUserAutoLinkedByDomain(
                    user,
                    institution.getId(),
                    institution.getName(),
                    domain,
                    Set.of(InstitutionRole.VIEWER)
            );

        } catch (Exception e) {
            LOGGER.error("Failed to auto-link user {} to institution {} via domain {}",
                        user.getEmail(), institution.getName(), domain, e);
            // Keep user as PENDING if auto-link fails
        }
    }

    /**
     * Updates an existing user with fresh data from the OAuth2 provider.
     *
     * @param existingUser the existing user entity
     * @param userInfo the updated user information from the provider
     * @return the updated user
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo) {
        boolean updated = false;

        // Update name if changed
        if (!existingUser.getName().equals(userInfo.getName())) {
            existingUser.setName(userInfo.getName());
            updated = true;
        }

        // Update picture URL if changed
        String newPictureUrl = userInfo.getImageUrl();
        if (newPictureUrl != null && !newPictureUrl.equals(existingUser.getPictureUrl())) {
            existingUser.setPictureUrl(newPictureUrl);
            updated = true;
        }

        if (updated) {
            User savedUser = userRepository.save(existingUser);
            LOGGER.info("Updated user: {}", savedUser.getEmail());
            return savedUser;
        }

        return existingUser;
    }
}
