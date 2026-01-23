package com.simplifica.config.security.oauth.providers;

import com.simplifica.config.security.oauth.OAuth2UserInfo;

import java.util.Map;

/**
 * OAuth2UserInfo implementation for Google authentication.
 *
 * Extracts user information from Google's OAuth2 response attributes.
 * Google provides user data in the following structure:
 * - sub: unique user ID
 * - name: full name
 * - email: email address
 * - picture: profile picture URL
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
