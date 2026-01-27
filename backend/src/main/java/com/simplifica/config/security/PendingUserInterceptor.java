package com.simplifica.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.UserRepository;
import com.simplifica.presentation.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Interceptor that blocks requests from users with PENDING status.
 *
 * Users in PENDING status are newly registered but not yet linked to
 * any institution. They can only access their profile and authentication
 * endpoints. All other endpoints return 403 Forbidden with a specific
 * message instructing them to wait for approval.
 *
 * Allowed endpoints for PENDING users:
 * - /user/profile (to view their profile)
 * - /auth/** (authentication endpoints)
 * - /public/** (public resources)
 */
@Component
public class PendingUserInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUserInterceptor.class);

    private static final List<String> ALLOWED_PATHS = Arrays.asList(
        "/user/profile",
        "/auth/",
        "/public/",
        "/oauth2/",
        "/actuator/",
        "/error"
    );

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PendingUserInterceptor(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Checks if the authenticated user has PENDING status and blocks access
     * to restricted endpoints.
     *
     * Improvements:
     * - Better null checks for different authentication types
     * - Proper exception handling for response writing
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler (controller method)
     * @return true to continue processing, false to abort
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String requestPath = request.getRequestURI();

        // Skip check for allowed paths
        if (isPathAllowed(requestPath)) {
            LOGGER.debug("Allowed path accessed: {}", requestPath);
            return true;
        }

        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authentication exists and is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            LOGGER.debug("No authentication or not authenticated");
            return true;
        }

        // Check if principal is UserPrincipal (better error handling)
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            LOGGER.debug("Non-UserPrincipal authentication type: {}",
                        authentication.getPrincipal().getClass().getSimpleName());
            return true; // Allow other authentication types to proceed
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Fetch user status from database
            User user = userRepository.findById(userPrincipal.getId()).orElse(null);

            if (user == null) {
                LOGGER.error("User not found in database: {}", userPrincipal.getId());
                // Don't block; let the endpoint handle missing user
                return true;
            }

            // Block PENDING users from accessing restricted endpoints
            if (user.getStatus() == UserStatus.PENDING) {
                LOGGER.warn("PENDING user {} attempted to access restricted endpoint: {}",
                           user.getEmail(), requestPath);
                return writeErrorResponse(response, HttpStatus.FORBIDDEN,
                    "Sua conta está pendente de aprovação. " +
                    "Aguarde até que um administrador vincule você a uma instituição. " +
                    "Você pode visualizar seu perfil em /user/profile.",
                    requestPath);
            }

            // Block INACTIVE users
            if (user.getStatus() == UserStatus.INACTIVE) {
                LOGGER.warn("INACTIVE user {} attempted to access endpoint: {}",
                           user.getEmail(), requestPath);
                return writeErrorResponse(response, HttpStatus.FORBIDDEN,
                    "Sua conta está inativa. " +
                    "Entre em contato com o administrador do sistema para reativar sua conta.",
                    requestPath);
            }

        } catch (Exception e) {
            LOGGER.error("Error in PendingUserInterceptor.preHandle()", e);
            // On unexpected error, allow the request to proceed
            // The endpoint will handle any issues
        }

        return true; // Continue processing
    }

    /**
     * Writes error response with proper exception handling.
     *
     * @param response the HTTP response
     * @param status the HTTP status
     * @param message the error message
     * @param path the request path
     * @return false to block the request
     */
    private boolean writeErrorResponse(HttpServletResponse response, HttpStatus status,
                                       String message, String path) {
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(message)
                    .path(path)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
        } catch (IOException e) {
            LOGGER.error("Error writing error response for path: {}", path, e);
            // Best effort; response may not be writable
        }
        return false; // Block the request
    }

    /**
     * Checks if the request path is allowed for PENDING users.
     *
     * @param path the request path
     * @return true if the path is allowed, false otherwise
     */
    private boolean isPathAllowed(String path) {
        if (path == null) {
            return false;
        }
        return ALLOWED_PATHS.stream().anyMatch(path::startsWith);
    }
}
