package com.simplifica.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifica.application.dto.UpdateUserRolesRequest;
import com.simplifica.config.security.jwt.JwtTokenProvider;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.InstitutionType;
import com.simplifica.domain.entity.OAuth2Provider;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import com.simplifica.infrastructure.repository.InstitutionRepository;
import com.simplifica.infrastructure.repository.UserInstitutionRepository;
import com.simplifica.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração end-to-end para alteração de papéis de usuários.
 *
 * REGRA CRÍTICA TESTADA:
 * "Gestores podem gerenciar os papéis dos usuários da instituição selecionada apenas.
 * Não podem alterar papéis de usuários em outras instituições."
 *
 * CENÁRIO:
 * - Instituição SIMP-ADMIN (id=1)
 * - Instituição UFMS
 * - Instituição UFPR
 *
 * - Gestor da UFMS (managerUser)
 * - Usuário A: vinculado à UFMS e UFPR (multiInstitutionUser)
 * - Usuário B: vinculado APENAS à UFPR (userOnlyUfpr)
 *
 * TESTES:
 * 1. ✅ GESTOR da UFMS PODE alterar papéis do Usuário A na UFMS
 * 2. ❌ GESTOR da UFMS NÃO PODE alterar papéis do Usuário A na UFPR
 * 3. ❌ GESTOR da UFMS NÃO PODE alterar papéis do Usuário B (apenas UFPR)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ManagerUpdateRolesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserInstitutionRepository userInstitutionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    // Instituições
    private Institution simpAdmin;
    private Institution ufms;
    private Institution ufpr;

    // Usuários
    private User adminUser;                // Admin da SIMP-ADMIN
    private User managerUser;              // Gestor da UFMS
    private User multiInstitutionUser;     // Vinculado à UFMS e UFPR
    private User userOnlyUfpr;             // Vinculado APENAS à UFPR

    @BeforeEach
    void setUp() {
        // Criar instituições
        simpAdmin = createInstitution("SIMP-ADMIN", "Simplifica Admin", InstitutionType.FEDERAL);
        ufms = createInstitution("UFMS", "Universidade Federal de MS", InstitutionType.FEDERAL);
        ufpr = createInstitution("UFPR", "Universidade Federal do PR", InstitutionType.FEDERAL);

        // Admin da SIMP-ADMIN (NÃO vinculado a UFMS nem UFPR)
        adminUser = createUser("admin@simplifica.com", "Admin User");
        linkUserToInstitution(adminUser, simpAdmin, Set.of(InstitutionRole.ADMIN));

        // Gestor da UFMS
        managerUser = createUser("gestor@ufms.br", "Gestor UFMS");
        linkUserToInstitution(managerUser, ufms, Set.of(InstitutionRole.MANAGER));

        // Usuário vinculado à UFMS e UFPR
        multiInstitutionUser = createUser("multi@email.com", "Multi Institution User");
        linkUserToInstitution(multiInstitutionUser, ufms, Set.of(InstitutionRole.VIEWER));
        linkUserToInstitution(multiInstitutionUser, ufpr, Set.of(InstitutionRole.MANAGER));

        // Usuário vinculado APENAS à UFPR
        userOnlyUfpr = createUser("user@ufpr.br", "UFPR Only User");
        linkUserToInstitution(userOnlyUfpr, ufpr, Set.of(InstitutionRole.VIEWER));
    }

    /**
     * TESTE 1: GESTOR da UFMS PODE alterar papéis de usuário vinculado à UFMS.
     *
     * Cenário:
     * - multiInstitutionUser está vinculado à UFMS com VIEWER
     * - GESTOR da UFMS altera para MANAGER
     * - Operação deve SUCEDER
     */
    @Test
    void gestorDevePoderAlterarPapeisNaPropriaInstituicao() throws Exception {
        // DADO: Token do gestor e instituição ativa = UFMS
        String token = generateToken(managerUser);
        String institutionIdHeader = ufms.getId().toString();

        // Request para alterar papéis do multiInstitutionUser na UFMS
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufms.getId());
        request.setRoles(Set.of(InstitutionRole.MANAGER)); // mudando de VIEWER para MANAGER

        // QUANDO: PUT /admin/users/{userId}/roles
        mockMvc.perform(put("/admin/users/" + multiInstitutionUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // ENTÃO: Verificar que o papel foi alterado no banco
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(multiInstitutionUser.getId(), ufms.getId())
                .orElseThrow();

        assertThat(link.getRoles()).containsExactly(InstitutionRole.MANAGER);
        assertThat(link.isActive()).isTrue();
    }

    /**
     * TESTE 2: GESTOR da UFMS NÃO PODE alterar papéis do mesmo usuário em OUTRA instituição (UFPR).
     *
     * Cenário:
     * - multiInstitutionUser está vinculado à UFPR com MANAGER
     * - GESTOR da UFMS tenta alterar para VIEWER na UFPR
     * - Operação deve FALHAR com 403 Forbidden
     */
    @Test
    void gestorNaoDevePoderAlterarPapeisEmOutraInstituicao() throws Exception {
        // DADO: Token do gestor e instituição ativa = UFMS
        String token = generateToken(managerUser);
        String institutionIdHeader = ufms.getId().toString();

        // Request para alterar papéis do multiInstitutionUser na UFPR (outra instituição!)
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufpr.getId()); // ← Tentando alterar em UFPR
        request.setRoles(Set.of(InstitutionRole.VIEWER));

        // QUANDO: PUT /admin/users/{userId}/roles para outra instituição
        mockMvc.perform(put("/admin/users/" + multiInstitutionUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // ← Deve retornar 403

        // ENTÃO: Verificar que o papel NÃO foi alterado no banco
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(multiInstitutionUser.getId(), ufpr.getId())
                .orElseThrow();

        // Papel deve permanecer MANAGER (não mudou para VIEWER)
        assertThat(link.getRoles()).containsExactly(InstitutionRole.MANAGER);
    }

    /**
     * TESTE 3: GESTOR da UFMS NÃO PODE alterar papéis de usuário que não pertence à sua instituição.
     *
     * Cenário:
     * - userOnlyUfpr está vinculado APENAS à UFPR
     * - GESTOR da UFMS tenta alterar papéis desse usuário na UFPR
     * - Operação deve FALHAR com 403 Forbidden
     */
    @Test
    void gestorNaoDevePoderAlterarPapeisDeUsuarioDeOutraInstituicao() throws Exception {
        // DADO: Token do gestor e instituição ativa = UFMS
        String token = generateToken(managerUser);
        String institutionIdHeader = ufms.getId().toString();

        // Request para alterar papéis do userOnlyUfpr na UFPR
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufpr.getId());
        request.setRoles(Set.of(InstitutionRole.MANAGER));

        // QUANDO: PUT /admin/users/{userId}/roles
        mockMvc.perform(put("/admin/users/" + userOnlyUfpr.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // ← Deve retornar 403

        // ENTÃO: Verificar que o papel NÃO foi alterado
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(userOnlyUfpr.getId(), ufpr.getId())
                .orElseThrow();

        // Papel deve permanecer VIEWER (não mudou para MANAGER)
        assertThat(link.getRoles()).containsExactly(InstitutionRole.VIEWER);
    }

    /**
     * TESTE 4: GESTOR não pode atribuir role ADMIN.
     *
     * Cenário:
     * - GESTOR da UFMS tenta atribuir role ADMIN a um usuário
     * - Operação deve FALHAR com 400 Bad Request
     * - Apenas SIMP-ADMIN pode ter role ADMIN
     */
    @Test
    void gestorNaoDevePoderAtribuirRoleAdmin() throws Exception {
        // DADO: Token do gestor e instituição ativa = UFMS
        String token = generateToken(managerUser);
        String institutionIdHeader = ufms.getId().toString();

        // Request para atribuir role ADMIN (não permitido fora de SIMP-ADMIN)
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufms.getId());
        request.setRoles(Set.of(InstitutionRole.ADMIN)); // ← Role ADMIN não permitido

        // QUANDO: PUT /admin/users/{userId}/roles
        mockMvc.perform(put("/admin/users/" + multiInstitutionUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // ← Deve retornar 400

        // ENTÃO: Verificar que o papel NÃO foi alterado
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(multiInstitutionUser.getId(), ufms.getId())
                .orElseThrow();

        // Papel deve permanecer VIEWER (não mudou para ADMIN)
        assertThat(link.getRoles()).containsExactly(InstitutionRole.VIEWER);
    }

    /**
     * TESTE 5: GESTOR pode atribuir múltiplos papéis (MANAGER + VIEWER) na sua instituição.
     *
     * Cenário:
     * - GESTOR da UFMS atribui MANAGER e VIEWER ao mesmo tempo
     * - Operação deve SUCEDER
     */
    @Test
    void gestorDevePoderAtribuirMultiplosPapeis() throws Exception {
        // DADO: Token do gestor e instituição ativa = UFMS
        String token = generateToken(managerUser);
        String institutionIdHeader = ufms.getId().toString();

        // Request para atribuir múltiplos papéis
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufms.getId());
        request.setRoles(Set.of(InstitutionRole.MANAGER, InstitutionRole.VIEWER));

        // QUANDO: PUT /admin/users/{userId}/roles
        mockMvc.perform(put("/admin/users/" + multiInstitutionUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // ENTÃO: Verificar que ambos papéis foram atribuídos
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(multiInstitutionUser.getId(), ufms.getId())
                .orElseThrow();

        assertThat(link.getRoles())
                .containsExactlyInAnyOrder(InstitutionRole.MANAGER, InstitutionRole.VIEWER);
    }

    /**
     * TESTE 6: ADMIN da SIMP-ADMIN PODE alterar papéis em qualquer instituição,
     * mesmo sem estar vinculado a ela.
     *
     * Cenário:
     * - adminUser está vinculado APENAS à SIMP-ADMIN
     * - adminUser NÃO está vinculado à UFMS
     * - ADMIN altera papéis de multiInstitutionUser na UFMS
     * - Operação deve SUCEDER (ADMIN tem acesso global)
     */
    @Test
    void adminDevePoderAlterarPapeisEmQualquerInstituicaoMesmoSemVinculo() throws Exception {
        // DADO: Token do ADMIN e instituição ativa = SIMP-ADMIN
        String token = generateToken(adminUser);
        String institutionIdHeader = simpAdmin.getId().toString();

        // Request para alterar papéis do multiInstitutionUser na UFMS
        // (adminUser NÃO está vinculado à UFMS, mas é ADMIN do sistema)
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufms.getId()); // ← Instituição que ADMIN não pertence
        request.setRoles(Set.of(InstitutionRole.MANAGER));

        // QUANDO: PUT /admin/users/{userId}/roles
        mockMvc.perform(put("/admin/users/" + multiInstitutionUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent()); // ← Deve SUCEDER

        // ENTÃO: Verificar que o papel foi alterado
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(multiInstitutionUser.getId(), ufms.getId())
                .orElseThrow();

        assertThat(link.getRoles()).containsExactly(InstitutionRole.MANAGER);
        assertThat(link.isActive()).isTrue();
    }

    /**
     * TESTE 7: ADMIN da SIMP-ADMIN PODE alterar papéis na UFPR também,
     * mesmo sem estar vinculado.
     *
     * Cenário:
     * - ADMIN altera papéis de userOnlyUfpr na UFPR
     * - Operação deve SUCEDER (ADMIN tem acesso global)
     */
    @Test
    void adminDevePoderAlterarPapeisDeUsuarioEmOutraInstituicaoSemVinculo() throws Exception {
        // DADO: Token do ADMIN e instituição ativa = SIMP-ADMIN
        String token = generateToken(adminUser);
        String institutionIdHeader = simpAdmin.getId().toString();

        // Request para alterar papéis do userOnlyUfpr na UFPR
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setInstitutionId(ufpr.getId()); // ← ADMIN não está vinculado à UFPR
        request.setRoles(Set.of(InstitutionRole.MANAGER));

        // QUANDO: PUT /admin/users/{userId}/roles
        mockMvc.perform(put("/admin/users/" + userOnlyUfpr.getId() + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent()); // ← Deve SUCEDER

        // ENTÃO: Verificar que o papel foi alterado
        UserInstitution link = userInstitutionRepository
                .findByUserIdAndInstitutionId(userOnlyUfpr.getId(), ufpr.getId())
                .orElseThrow();

        assertThat(link.getRoles()).containsExactly(InstitutionRole.MANAGER);
    }

    // ==================== Métodos Auxiliares ====================

    private Institution createInstitution(String acronym, String name, InstitutionType type) {
        Institution institution = Institution.builder()
                .acronym(acronym)
                .name(name)
                .type(type)
                .active(true)
                .build();
        return institutionRepository.save(institution);
    }

    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .provider(OAuth2Provider.GOOGLE)
                .providerId(UUID.randomUUID().toString())
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private void linkUserToInstitution(User user, Institution institution, Set<InstitutionRole> roles) {
        UserInstitution link = UserInstitution.builder()
                .user(user)
                .institution(institution)
                .roles(roles)
                .linkedBy(user) // simplificação para teste
                .active(true)
                .build();
        userInstitutionRepository.save(link);
    }

    private String generateToken(User user) {
        return jwtTokenProvider.generateTokenFromUserId(user.getId());
    }
}
