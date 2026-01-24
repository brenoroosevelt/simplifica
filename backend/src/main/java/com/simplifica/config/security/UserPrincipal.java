package com.simplifica.config.security;

import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * UserPrincipal is an adapter that implements both UserDetails and OAuth2User.
 *
 * This class bridges the gap between our domain User entity and Spring Security's
 * authentication interfaces. It provides user information to the security context
 * during both OAuth2 and JWT authentication flows.
 *
 * The currentInstitutionId field is mutable and set by the TenantInterceptor based
 * on the X-Institution-Id header in each request.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {

    private UUID id;
    private String email;
    private String name;
    private String pictureUrl;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Setter
    private UUID currentInstitutionId;

    /**
     * Creates a UserPrincipal from a User entity (for JWT authentication).
     *
     * @param user the User entity
     * @return a UserPrincipal instance
     */
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPictureUrl(),
                authorities,
                Collections.emptyMap(),
                null // currentInstitutionId set later by interceptor
        );
    }

    /**
     * Creates a UserPrincipal from a User entity with OAuth2 attributes.
     *
     * @param user the User entity
     * @param attributes the OAuth2 user attributes from the provider
     * @return a UserPrincipal instance
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = create(user);
        return new UserPrincipal(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getName(),
                userPrincipal.getPictureUrl(),
                userPrincipal.getAuthorities(),
                attributes,
                null // currentInstitutionId set later by interceptor
        );
    }

    // UserDetails interface methods

    @Override
    public String getPassword() {
        return null; // OAuth2 users don't have passwords
    }

    @Override
    public String getUsername() {
        return email; // Email is used as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // All authenticated users are enabled
    }

    // OAuth2User interface methods

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Checks if the user has admin role.
     *
     * @return true if the user has ROLE_ADMIN authority
     */
    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + UserRole.ADMIN.name()));
    }
}
