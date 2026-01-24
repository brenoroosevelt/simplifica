package com.simplifica.config.tenant;

import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.Institution;
import com.simplifica.infrastructure.repository.InstitutionRepository;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor that extracts the institution ID from the request header and sets the tenant context.
 *
 * This interceptor runs before each request and:
 * 1. Extracts the X-Institution-Id header
 * 2. Validates the UUID format
 * 3. Validates institution exists and is active
 * 4. For non-admin users, validates they belong to the institution
 * 5. Sets the TenantContext for the current thread
 * 6. Clears the context after request completion
 *
 * The institution ID is used for tenant-aware data filtering throughout the application.
 *
 * Security validations:
 * - Admin users can access any active institution
 * - Non-admin users can only access institutions they are linked to
 * - Inactive institutions are blocked for all users
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantInterceptor.class);
    private static final String INSTITUTION_HEADER = "X-Institution-Id";

    private final InstitutionRepository institutionRepository;
    private final UserInstitutionRepository userInstitutionRepository;

    public TenantInterceptor(
            InstitutionRepository institutionRepository,
            UserInstitutionRepository userInstitutionRepository
    ) {
        this.institutionRepository = institutionRepository;
        this.userInstitutionRepository = userInstitutionRepository;
    }

    /**
     * Pre-handle method that runs before the request is processed.
     *
     * Extracts and validates the institution ID from the request header,
     * then sets it in the TenantContext for the current thread.
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
        String institutionIdHeader = request.getHeader(INSTITUTION_HEADER);

        if (institutionIdHeader != null && !institutionIdHeader.isEmpty()) {
            try {
                UUID institutionId = UUID.fromString(institutionIdHeader);

                // Validate that institution exists and is active
                Institution institution = institutionRepository.findById(institutionId).orElse(null);
                if (institution == null) {
                    LOGGER.warn("Attempted access to non-existent institution: {}", institutionId);
                    // Don't set context - let endpoint return 403/404
                    return true;
                }

                if (!institution.isActive()) {
                    LOGGER.warn("Attempted access to inactive institution: {} ({})",
                            institutionId, institution.getName());
                    // Don't set context - let endpoint return 403
                    return true;
                }

                // Get the authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                    // Admin users can access any active institution
                    if (userPrincipal.isAdmin()) {
                        TenantContext.setCurrentInstitution(institutionId);
                        LOGGER.debug("Admin user {} set tenant context to institution: {}",
                                userPrincipal.getEmail(), institutionId);
                    } else {
                        // For non-admin users, validate they actually belong to this institution
                        boolean belongsToInstitution = userInstitutionRepository
                                .findActiveByUserAndInstitution(userPrincipal.getId(), institutionId)
                                .isPresent();

                        if (belongsToInstitution) {
                            TenantContext.setCurrentInstitution(institutionId);
                            LOGGER.debug("User {} set tenant context to institution: {}",
                                    userPrincipal.getEmail(), institutionId);
                        } else {
                            LOGGER.warn("User {} attempted access to institution {} without permissions",
                                    userPrincipal.getId(), institutionId);
                            // Don't set context - let endpoint return 403
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid institution ID format in header: {}", institutionIdHeader);
                // Don't fail the request, just don't set the context
            }
        } else {
            LOGGER.trace("No institution ID header found in request to: {}", request.getRequestURI());
        }

        return true;
    }

    /**
     * After-completion method that runs after the request is processed.
     *
     * Clears the TenantContext to prevent memory leaks in thread-pooled environments.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler (controller method)
     * @param ex any exception thrown during request processing
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        TenantContext.clear();
        LOGGER.trace("Cleared tenant context after request to: {}", request.getRequestURI());
    }
}
