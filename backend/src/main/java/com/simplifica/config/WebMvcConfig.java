package com.simplifica.config;

import com.simplifica.config.security.PendingUserInterceptor;
import com.simplifica.config.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the application.
 *
 * This configuration registers interceptors and configures request handling behavior.
 * Registers both the TenantInterceptor for tenant context management and the
 * PendingUserInterceptor to block PENDING users from accessing restricted endpoints.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final PendingUserInterceptor pendingUserInterceptor;

    @Autowired
    public WebMvcConfig(TenantInterceptor tenantInterceptor,
                        PendingUserInterceptor pendingUserInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
        this.pendingUserInterceptor = pendingUserInterceptor;
    }

    /**
     * Registers interceptors for request processing.
     *
     * Order matters:
     * 1. PendingUserInterceptor runs first to block PENDING users (order = 1)
     * 2. TenantInterceptor runs second to set tenant context (order = 2)
     *
     * The TenantInterceptor is registered to run on all requests except:
     * - Public endpoints (/public/**)
     * - OAuth2 endpoints (/oauth2/**)
     * - Actuator endpoints (/actuator/**)
     * - Authentication endpoints (/auth/**)
     * - Institution management endpoints (/institutions/**) - admins don't need tenant context
     *
     * The PendingUserInterceptor runs on all authenticated endpoints except:
     * - /user/profile (PENDING users can view their profile)
     * - /auth/** (authentication endpoints)
     * - /public/** (public resources)
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register PendingUserInterceptor first
        registry.addInterceptor(pendingUserInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // Register TenantInterceptor second
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/public/**",
                        "/oauth2/**",
                        "/actuator/**",
                        "/auth/**",
                        "/institutions/**",
                        "/error"
                )
                .order(2);
    }
}
