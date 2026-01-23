package com.simplifica.domain.entity;

/**
 * Enum representing supported OAuth2 authentication providers.
 *
 * This enum defines the OAuth2 providers that users can authenticate with.
 * The architecture is designed to be extensible for adding new providers.
 */
public enum OAuth2Provider {
    GOOGLE,
    MICROSOFT;

    /**
     * Converts a string representation to an OAuth2Provider enum value.
     *
     * @param provider the string representation of the provider
     * @return the corresponding OAuth2Provider enum value
     * @throws IllegalArgumentException if the provider string is invalid
     */
    public static OAuth2Provider fromString(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        try {
            return OAuth2Provider.valueOf(provider.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid OAuth2 provider: " + provider + ". Supported providers: GOOGLE, MICROSOFT"
            );
        }
    }
}
