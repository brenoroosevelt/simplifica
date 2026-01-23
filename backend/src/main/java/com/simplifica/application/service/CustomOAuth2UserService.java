package com.simplifica.application.service;

import com.simplifica.config.security.UserPrincipal;
import com.simplifica.config.security.oauth.OAuth2UserInfo;
import com.simplifica.config.security.oauth.OAuth2UserInfoFactory;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserStatus;
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

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    /**
     * Creates a new user from OAuth2 provider information.
     *
     * @param provider the OAuth2 provider
     * @param userInfo the user information from the provider
     * @return the newly created user
     */
    private User createNewUser(OAuth2Provider provider, OAuth2UserInfo userInfo) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .pictureUrl(userInfo.getImageUrl())
                .provider(provider)
                .providerId(userInfo.getId())
                .status(UserStatus.PENDING) // New users start as PENDING
                .build();

        User savedUser = userRepository.save(user);
        LOGGER.info("Created new user: {} from provider: {}", savedUser.getEmail(), provider);
        return savedUser;
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
