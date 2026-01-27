package com.simplifica.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifica.application.dto.PagedResponseDTO;
import com.simplifica.application.dto.UserListDTO;
import com.simplifica.config.security.jwt.JwtTokenProvider;
import com.simplifica.config.security.UserPrincipal;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração end-to-end para listagem de usuários por instituição.
 *
 * REGRA CRÍTICA TESTADA:
 * "As permissões de um usuário são determinadas exclusivamente pelo papel que ele possui
 * na instituição ativa, nunca pelo conjunto de vínculos."
 *
 * CENÁRIO:
 * - Instituição SIMP-ADMIN (id=1)
 * - Instituição Normal UFMS (id!=1)
 * - Usuário A: vinculado APENAS à SIMP-ADMIN
 * - Usuário B: vinculado APENAS à UFMS
 * - Usuário C: vinculado APENAS à UFMS
 * - Usuário D: vinculado a SIMP-ADMIN E UFMS
 *
 * EXPECTATIVA:
 * Ao listar usuários com instituição ativa = UFMS, deve retornar:
 * - Usuário B (vinculado apenas à UFMS)
 * - Usuário C (vinculado apenas à UFMS)
 * - Usuário D (vinculado a ambas)
 * TOTAL: 3 usuários
 *
 * NÃO deve retornar:
 * - Usuário A (vinculado apenas à SIMP-ADMIN)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ManagerListUsersIntegrationTest {

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

    // Usuários
    private User userOnlyAdmin;      // Usuário A
    private User userOnlyUfms1;      // Usuário B
    private User userOnlyUfms2;      // Usuário C
    private User userBothInstitutions; // Usuário D

    // Gestor que fará a requisição
    private User managerUser;

    @BeforeEach
    void setUp() {
        // Criar instituições
        simpAdmin = createInstitution("SIMP-ADMIN", "Simplifica Admin", InstitutionType.FEDERAL);
        ufms = createInstitution("UFMS", "Universidade Federal de MS", InstitutionType.FEDERAL);

        // Criar usuários

        // Usuário A: vinculado APENAS à SIMP-ADMIN
        userOnlyAdmin = createUser("admin.only@simplifica.com", "Admin Only User");
        linkUserToInstitution(userOnlyAdmin, simpAdmin, Set.of(InstitutionRole.ADMIN));

        // Usuário B: vinculado APENAS à UFMS
        userOnlyUfms1 = createUser("user1@ufms.br", "UFMS User 1");
        linkUserToInstitution(userOnlyUfms1, ufms, Set.of(InstitutionRole.VIEWER));

        // Usuário C: vinculado APENAS à UFMS
        userOnlyUfms2 = createUser("user2@ufms.br", "UFMS User 2");
        linkUserToInstitution(userOnlyUfms2, ufms, Set.of(InstitutionRole.VIEWER));

        // Usuário D: vinculado a SIMP-ADMIN E UFMS
        userBothInstitutions = createUser("multi@email.com", "Multi Institution User");
        linkUserToInstitution(userBothInstitutions, simpAdmin, Set.of(InstitutionRole.ADMIN));
        linkUserToInstitution(userBothInstitutions, ufms, Set.of(InstitutionRole.MANAGER));

        // Gestor que fará a requisição (com MANAGER na UFMS)
        managerUser = createUser("gestor@ufms.br", "Gestor UFMS");
        linkUserToInstitution(managerUser, ufms, Set.of(InstitutionRole.MANAGER));
    }

    /**
     * Teste principal: Gestor lista usuários da instituição UFMS.
     *
     * Deve retornar apenas usuários vinculados à UFMS:
     * - userOnlyUfms1
     * - userOnlyUfms2
     * - userBothInstitutions
     * - managerUser (o próprio gestor)
     *
     * NÃO deve retornar:
     * - userOnlyAdmin (vinculado apenas à SIMP-ADMIN)
     */
    @Test
    void deveListarApenasUsuariosVinculadosAInstituicaoAtiva() throws Exception {
        // DADO: Token JWT do gestor e instituição ativa = UFMS
        String token = generateToken(managerUser);
        String institutionIdHeader = ufms.getId().toString();

        // QUANDO: GET /admin/users com header X-Institution-Id
        MvcResult result = mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Institution-Id", institutionIdHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse do JSON de resposta
        String jsonResponse = result.getResponse().getContentAsString();
        PagedResponseDTO<UserListDTO> response = objectMapper.readValue(
                jsonResponse,
                new TypeReference<PagedResponseDTO<UserListDTO>>() {}
        );

        // ENTÃO: Deve retornar exatamente 4 usuários (3 setup + 1 gestor)
        List<UserListDTO> users = response.getContent();
        assertThat(users).hasSize(4);

        // Validar que os usuários corretos estão na lista
        List<String> returnedEmails = users.stream()
                .map(UserListDTO::getEmail)
                .toList();

        assertThat(returnedEmails)
                .contains(
                        "user1@ufms.br",       // userOnlyUfms1
                        "user2@ufms.br",       // userOnlyUfms2
                        "multi@email.com",     // userBothInstitutions
                        "gestor@ufms.br"       // managerUser (o próprio)
                )
                .doesNotContain(
                        "admin.only@simplifica.com"  // userOnlyAdmin (NÃO deve aparecer)
                );

        // Validar que o usuário com múltiplos vínculos aparece
        UserListDTO multiUser = users.stream()
                .filter(u -> u.getEmail().equals("multi@email.com"))
                .findFirst()
                .orElseThrow();

        assertThat(multiUser.getName()).isEqualTo("Multi Institution User");
        assertThat(multiUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // Validar paginação
        assertThat(response.getTotalElements()).isEqualTo(4);
        assertThat(response.getCurrentPage()).isEqualTo(0); // primeira página
        assertThat(response.getPageSize()).isGreaterThanOrEqualTo(4);
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
