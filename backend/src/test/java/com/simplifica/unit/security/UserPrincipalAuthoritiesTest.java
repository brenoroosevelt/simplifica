package com.simplifica.unit.security;

import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.InstitutionType;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for UserPrincipal authority determination logic.
 *
 * Validates that users receive correct Spring Security roles based on their
 * institution roles.
 */
@DisplayName("UserPrincipal - Authority Determination Tests")
class UserPrincipalAuthoritiesTest {

    @Test
    @DisplayName("User with ADMIN in SIMP-ADMIN should get ROLE_ADMIN")
    void shouldGrantRoleAdminForSimpAdminUser() {
        // Arrange
        User user = createUserWithRole("SIMP-ADMIN", InstitutionRole.ADMIN);

        // Act
        UserPrincipal principal = UserPrincipal.create(user);

        // Assert
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(principal.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("User with MANAGER in regular institution should get ROLE_MANAGER")
    void shouldGrantRoleManagerForInstitutionManager() {
        // Arrange
        User user = createUserWithRole("EMPRESA-X", InstitutionRole.MANAGER);

        // Act
        UserPrincipal principal = UserPrincipal.create(user);

        // Assert
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");
        assertThat(principal.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("User with VIEWER only should get ROLE_USER")
    void shouldGrantRoleUserForViewerOnly() {
        // Arrange
        User user = createUserWithRole("EMPRESA-X", InstitutionRole.VIEWER);

        // Act
        UserPrincipal principal = UserPrincipal.create(user);

        // Assert
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(principal.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("User with no institutions should get ROLE_USER")
    void shouldGrantRoleUserForNoInstitutions() {
        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("Test User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-123")
                .status(UserStatus.PENDING)
                .institutions(new HashSet<>())
                .build();

        // Act
        UserPrincipal principal = UserPrincipal.create(user);

        // Assert
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(principal.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("User with MANAGER in multiple institutions should get ROLE_MANAGER")
    void shouldGrantRoleManagerForMultipleInstitutions() {
        // Arrange
        User user = createUserWithMultipleRoles(
                Arrays.asList(
                        new InstitutionRole[]{InstitutionRole.MANAGER},
                        new InstitutionRole[]{InstitutionRole.VIEWER}
                )
        );

        // Act
        UserPrincipal principal = UserPrincipal.create(user);

        // Assert
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");
    }

    @Test
    @DisplayName("ADMIN in SIMP-ADMIN takes precedence over MANAGER in other institutions")
    void shouldPrioritizeSimpAdminRole() {
        // Arrange
        Institution simpAdmin = createInstitution("SIMP-ADMIN");
        Institution regularInst = createInstitution("EMPRESA-X");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@simplifica.com")
                .name("System Admin")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-456")
                .status(UserStatus.ACTIVE)
                .institutions(new HashSet<>())
                .build();

        UserInstitution ui1 = createUserInstitution(user, simpAdmin, InstitutionRole.ADMIN);
        UserInstitution ui2 = createUserInstitution(user, regularInst, InstitutionRole.MANAGER);

        user.getInstitutions().add(ui1);
        user.getInstitutions().add(ui2);

        // Act
        UserPrincipal principal = UserPrincipal.create(user);

        // Assert
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(principal.isAdmin()).isTrue();
    }

    // Helper methods

    private User createUserWithRole(String institutionAcronym, InstitutionRole role) {
        Institution institution = createInstitution(institutionAcronym);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("Test User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-123")
                .status(UserStatus.ACTIVE)
                .institutions(new HashSet<>())
                .build();

        UserInstitution userInstitution = createUserInstitution(user, institution, role);
        user.getInstitutions().add(userInstitution);

        return user;
    }

    private User createUserWithMultipleRoles(List<InstitutionRole[]> rolesList) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("Test User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-123")
                .status(UserStatus.ACTIVE)
                .institutions(new HashSet<>())
                .build();

        int counter = 0;
        for (InstitutionRole[] roles : rolesList) {
            Institution institution = createInstitution("INST-" + counter++);
            for (InstitutionRole role : roles) {
                UserInstitution userInstitution = createUserInstitution(user, institution, role);
                user.getInstitutions().add(userInstitution);
            }
        }

        return user;
    }

    private Institution createInstitution(String acronym) {
        return Institution.builder()
                .id(UUID.randomUUID())
                .name(acronym + " Institution")
                .acronym(acronym)
                .type(InstitutionType.PRIVADA)
                .active(true)
                .build();
    }

    private UserInstitution createUserInstitution(User user, Institution institution, InstitutionRole role) {
        return UserInstitution.builder()
                .id(UUID.randomUUID())
                .user(user)
                .institution(institution)
                .roles(Set.of(role))
                .active(true)
                .build();
    }
}
