package com.simplifica.config.security.oauth;

import com.simplifica.config.security.oauth.providers.GoogleOAuth2UserInfo;
import com.simplifica.config.security.oauth.providers.MicrosoftOAuth2UserInfo;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

/**
 * Factory for creating OAuth2UserInfo instances based on the provider.
 *
 * This factory implements the Strategy pattern, making it easy to add
 * new OAuth2 providers without modifying existing code. Simply add a
 * new case to the switch statement and create the corresponding
 * OAuth2UserInfo implementation.
 */
public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
        // Private constructor to hide the implicit public one
    }

    /**
     * Creates an OAuth2UserInfo instance for the given provider.
     *
     * @param registrationId the OAuth2 provider registration ID (google, microsoft, etc.)
     * @param attributes the user attributes returned by the OAuth2 provider
     * @return an OAuth2UserInfo implementation for the specified provider
     * @throws OAuth2AuthenticationException if the provider is not supported
     */
    public static OAuth2UserInfo getOAuth2UserInfo(
            String registrationId,
            Map<String, Object> attributes
    ) {
        if (registrationId == null) {
            throw new OAuth2AuthenticationException("Provider registration ID cannot be null");
        }

        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "microsoft" -> new MicrosoftOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(
                    "Unsupported OAuth2 provider: " + registrationId
                    + ". Supported providers: google, microsoft"
            );
        };
    }
}
