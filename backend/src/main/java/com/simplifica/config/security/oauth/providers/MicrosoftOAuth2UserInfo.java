package com.simplifica.config.security.oauth.providers;

import com.simplifica.config.security.oauth.OAuth2UserInfo;

import java.util.Map;

/**
 * OAuth2UserInfo implementation for Microsoft authentication.
 *
 * Extracts user information from Microsoft's OAuth2 response attributes.
 * Microsoft provides user data in the following structure:
 * - sub or id: unique user ID
 * - name or displayName: full name
 * - email or userPrincipalName: email address
 * - picture: profile picture URL (optional)
 */
public class MicrosoftOAuth2UserInfo extends OAuth2UserInfo {

    public MicrosoftOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        // Microsoft can use either 'sub' (OIDC standard) or 'id'
        String id = (String) attributes.get("sub");
        if (id == null) {
            id = (String) attributes.get("id");
        }
        return id;
    }

    @Override
    public String getName() {
        // Microsoft can use either 'name' or 'displayName'
        String name = (String) attributes.get("name");
        if (name == null) {
            name = (String) attributes.get("displayName");
        }
        return name;
    }

    @Override
    public String getEmail() {
        // Microsoft can use either 'email' or 'userPrincipalName'
        String email = (String) attributes.get("email");
        if (email == null) {
            email = (String) attributes.get("userPrincipalName");
        }
        // Sometimes email comes from preferred_username
        if (email == null) {
            email = (String) attributes.get("preferred_username");
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
