package com.simplifica.config.security.oauth;

import java.util.Map;

/**
 * Abstract class for extracting user information from OAuth2 providers.
 *
 * This class provides a common interface for accessing user data from different
 * OAuth2 providers (Google, Microsoft, etc.). Each provider implementation
 * knows how to extract the relevant fields from the provider's specific
 * attribute structure.
 */
public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets the provider-specific user ID.
     *
     * @return the unique identifier for the user from the OAuth2 provider
     */
    public abstract String getId();

    /**
     * Gets the user's full name.
     *
     * @return the user's name
     */
    public abstract String getName();

    /**
     * Gets the user's email address.
     *
     * @return the user's email
     */
    public abstract String getEmail();

    /**
     * Gets the URL of the user's profile image.
     *
     * @return the profile image URL, or null if not available
     */
    public abstract String getImageUrl();
}
