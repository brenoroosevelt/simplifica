package com.simplifica.unit.dto;

import com.simplifica.application.dto.UserDetailDTO;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for UserDetailDTO mapping from User entity.
 *
 * CRITICAL: Tests that only ACTIVE institutions are included in the DTO.
 * This is a regression test for the bug where inactive institutions
 * were being returned after unlinking.
 */
@DisplayName("UserDetailDTO - Active Institutions Filter Tests")
class UserDetailDTOTest {

    @Test
    @DisplayName("Should return only ACTIVE institutions")
    void shouldReturnOnlyActiveInstitutions() {
        // Arrange - Create user with 2 ACTIVE and 1 INACTIVE institution
        User user = createUser();

        Institution activeInst1 = createInstitution("Active 1", "ACT1");
        Institution activeInst2 = createInstitution("Active 2", "ACT2");
        Institution inactiveInst = createInstitution("Inactive", "INACT");

        UserInstitution activeLink1 = createUserInstitution(user, activeInst1, true);
        UserInstitution activeLink2 = createUserInstitution(user, activeInst2, true);
        UserInstitution inactiveLink = createUserInstitution(user, inactiveInst, false); // INACTIVE

        user.getInstitutions().add(activeLink1);
        user.getInstitutions().add(activeLink2);
        user.getInstitutions().add(inactiveLink);

        // Act - Convert to DTO
        UserDetailDTO dto = UserDetailDTO.fromEntity(user);

        // Assert - Should have only 2 ACTIVE institutions
        assertThat(dto).isNotNull();
        assertThat(dto.getInstitutions()).hasSize(2);
        assertThat(dto.getInstitutions())
                .extracting(UserDetailDTO.UserInstitutionDetailDTO::getInstitutionName)
                .containsExactlyInAnyOrder("Active 1", "Active 2")
                .doesNotContain("Inactive");

        // Verify all returned institutions are active
        assertThat(dto.getInstitutions())
                .allMatch(UserDetailDTO.UserInstitutionDetailDTO::getActive);
    }

    @Test
    @DisplayName("Should return empty list when all institutions are INACTIVE")
    void shouldReturnEmptyListWhenAllInstitutionsInactive() {
        // Arrange - User with only INACTIVE institutions
        User user = createUser();

        Institution inactiveInst1 = createInstitution("Inactive 1", "INACT1");
        Institution inactiveInst2 = createInstitution("Inactive 2", "INACT2");

        UserInstitution inactiveLink1 = createUserInstitution(user, inactiveInst1, false);
        UserInstitution inactiveLink2 = createUserInstitution(user, inactiveInst2, false);

        user.getInstitutions().add(inactiveLink1);
        user.getInstitutions().add(inactiveLink2);

        // Act - Convert to DTO
        UserDetailDTO dto = UserDetailDTO.fromEntity(user);

        // Assert - Should have NO institutions
        assertThat(dto).isNotNull();
        assertThat(dto.getInstitutions()).isEmpty();
    }

    @Test
    @DisplayName("Should handle user with no institutions")
    void shouldHandleUserWithNoInstitutions() {
        // Arrange - User with no institutions
        User user = createUser();

        // Act - Convert to DTO
        UserDetailDTO dto = UserDetailDTO.fromEntity(user);

        // Assert - Should have empty institutions list
        assertThat(dto).isNotNull();
        assertThat(dto.getInstitutions()).isEmpty();
    }

    @Test
    @DisplayName("Should correctly map ACTIVE institution details")
    void shouldCorrectlyMapActiveInstitutionDetails() {
        // Arrange - User with one ACTIVE institution
        User user = createUser();
        Institution institution = createInstitution("Test Institution", "TEST");

        UserInstitution link = UserInstitution.builder()
                .id(UUID.randomUUID())
                .user(user)
                .institution(institution)
                .roles(Set.of(InstitutionRole.MANAGER, InstitutionRole.VIEWER))
                .active(true)
                .linkedBy(user)
                .build();

        user.getInstitutions().add(link);

        // Act - Convert to DTO
        UserDetailDTO dto = UserDetailDTO.fromEntity(user);

        // Assert - Should have correct institution details
        assertThat(dto).isNotNull();
        assertThat(dto.getInstitutions()).hasSize(1);

        UserDetailDTO.UserInstitutionDetailDTO instDto = dto.getInstitutions().get(0);
        assertThat(instDto.getInstitutionId()).isEqualTo(institution.getId());
        assertThat(instDto.getInstitutionName()).isEqualTo("Test Institution");
        assertThat(instDto.getInstitutionAcronym()).isEqualTo("TEST");
        assertThat(instDto.getRoles()).containsExactlyInAnyOrder(
                InstitutionRole.MANAGER,
                InstitutionRole.VIEWER
        );
        assertThat(instDto.getActive()).isTrue();
    }

    @Test
    @DisplayName("REGRESSION TEST: Should not return unlinked institution after deactivation")
    void regressionTest_shouldNotReturnUnlinkedInstitution() {
        // Arrange - Simulate unlinking scenario
        User user = createUser();
        Institution institution = createInstitution("Institution to Unlink", "UNLINK");

        UserInstitution link = createUserInstitution(user, institution, true);
        user.getInstitutions().add(link);

        // Verify initial state - institution is active
        UserDetailDTO dtoBeforeUnlink = UserDetailDTO.fromEntity(user);
        assertThat(dtoBeforeUnlink.getInstitutions()).hasSize(1);

        // Act - Simulate unlinking by deactivating
        link.setActive(false);

        // Convert to DTO again
        UserDetailDTO dtoAfterUnlink = UserDetailDTO.fromEntity(user);

        // Assert - Institution should NOT be in the list anymore
        assertThat(dtoAfterUnlink.getInstitutions()).isEmpty();
    }

    // Helper methods

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-123")
                .status(UserStatus.ACTIVE)
                .institutions(new HashSet<>())
                .build();
    }

    private Institution createInstitution(String name, String acronym) {
        return Institution.builder()
                .id(UUID.randomUUID())
                .name(name)
                .acronym(acronym)
                .active(true)
                .build();
    }

    private UserInstitution createUserInstitution(User user, Institution institution, boolean active) {
        return UserInstitution.builder()
                .id(UUID.randomUUID())
                .user(user)
                .institution(institution)
                .roles(Set.of(InstitutionRole.VIEWER))
                .active(active)
                .build();
    }
}
