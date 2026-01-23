package com.simplifica.config.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler for failed OAuth2 authentication.
 *
 * When OAuth2 authentication fails, this handler redirects to the frontend
 * with an error message as a query parameter. The frontend can then display
 * an appropriate error message to the user.
 */
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    @Value("${app.oauth.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String targetUrl = determineTargetUrl(exception);

        LOGGER.error("OAuth2 authentication failed: {}", exception.getMessage());

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Determines the target URL to redirect to after authentication failure.
     *
     * @param exception the authentication exception
     * @return the target URL with error message
     */
    private String determineTargetUrl(AuthenticationException exception) {
        String errorMessage = exception.getLocalizedMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Authentication failed";
        }

        return UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("error", errorMessage)
                .build()
                .toUriString();
    }
}
