package com.simplifica.config;

import com.simplifica.config.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the application.
 *
 * This configuration registers interceptors and configures request handling behavior.
 * Currently registers the TenantInterceptor for tenant context management.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Autowired
    public WebMvcConfig(TenantInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    /**
     * Registers interceptors for request processing.
     *
     * The TenantInterceptor is registered to run on all requests except:
     * - Public endpoints (/public/**)
     * - OAuth2 endpoints (/oauth2/**)
     * - Actuator endpoints (/actuator/**)
     * - Authentication endpoints (/auth/**)
     * - Institution management endpoints (/institutions/**) - admins don't need tenant context
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/public/**",
                        "/oauth2/**",
                        "/actuator/**",
                        "/auth/**",
                        "/institutions/**",
                        "/error"
                );
    }
}
