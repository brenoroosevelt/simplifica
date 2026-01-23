# Feature 001 - Multi-Tenant

## Contexto
Usuários precisam autenticar no sistema e ter acesso apenas aos dados referente a sua instituição. O sistema deve suportar múltiplas instituições (tenants) com isolamento completo de dados e gerenciamento granular de permissões por instituição.

## Objetivo
Permitir que várias instituições usem o sistema e que seus usuários tenham acesso apenas ao contexto de seus dados, com suporte a:
- Vinculação de usuários a múltiplas instituições
- Seleção de instituição ativa no contexto da sessão
- Gerenciamento de papéis (roles) por instituição
- Isolamento automático de dados via tenant context

## Usuários impactados
- Usuários finais do sistema web (múltiplas instituições)
- Administradores de sistema (gerenciamento global)
- Administradores de instituição (gerenciamento local)

## Escopo

### Inclui:
- **Backend:**
  - Schema de banco de dados multi-tenant (institutions, user_institutions)
  - Entidades JPA com relacionamentos
  - CRUD completo de instituições (admin only)
  - Vinculação de usuários a instituições com roles
  - Sistema de tenant context (ThreadLocal + Interceptors)
  - Isolamento de dados via Hibernate Filters
  - Segurança com custom annotations (@RequireInstitutionAccess)
  - Auditoria de acesso a recursos de instituições
  - Documentação OpenAPI/Swagger completa

- **Frontend:**
  - Types TypeScript para instituições
  - Service layer para APIs de instituições
  - Pinia store para gerenciamento de instituição ativa
  - Componentes Vue para CRUD de instituições
  - Modal de seleção de instituição (login)
  - Switcher de instituição no header
  - Integração com router guards
  - Persistência de instituição ativa em localStorage

- **Infraestrutura:**
  - Serviço de email (notificações de vínculo/desvínculo)
  - Sistema de armazenamento de imagens (logos)
  - Templates HTML responsivos para emails
  - Configurações .env para email e storage

- **Campos da Instituição:**
  - Logo (imagem upload com processamento)
  - Nome completo
  - Sigla (unique)
  - Tipo (FEDERAL, ESTADUAL, MUNICIPAL, PRIVADA)
  - Domínio (ex: ufms.br) para vinculação automática futura
  - Status (ativo/inativo)

### Não Inclui (Futuro):
- Vinculação automática de usuários por domínio de email
- Dashboard de métricas por instituição
- Personalização visual por instituição (tema, cores)
- Multi-idioma por instituição

---

## Fluxo do usuário

### Fluxo Administrativo (Admin Global)
1. Admin entra no sistema
2. Acessa menu "Instituições"
3. Visualiza listagem com filtros (tipo, status, busca), paginação e ordenação
4. Pode criar nova instituição (formulário com upload de logo)
5. Pode editar instituição existente
6. Pode ativar/desativar instituição
7. Pode vincular/desvincular usuários com seleção de roles
8. Sistema envia email de notificação ao usuário

### Fluxo do Usuário (Login)
1. Usuário faz login via OAuth2
2. Sistema verifica quantidade de instituições vinculadas:
   - **1 instituição**: seleciona automaticamente e vai para dashboard
   - **Múltiplas instituições**: exibe modal de seleção
3. Usuário seleciona instituição desejada
4. Sistema persiste seleção em localStorage
5. Todas as requisições incluem header `X-Institution-Id`
6. Backend valida acesso e filtra dados automaticamente

### Fluxo do Usuário (Troca de Instituição)
1. Usuário clica no switcher de instituição no header
2. Visualiza dropdown com instituições disponíveis
3. Seleciona outra instituição
4. Página recarrega com novo contexto
5. Dados exibidos são da nova instituição ativa

---

## Regras de negócio

### Instituições
- Uma instituição possui vários usuários vinculados
- Um usuário pode estar vinculado a mais de uma instituição
- Sigla da instituição deve ser única no sistema
- Domínio (se informado) deve ser único no sistema
- Logo é opcional, mas recomendada (processada: WebP, thumbnails)
- Instituição inativa não permite novos vínculos de usuários

### Vinculação Usuário-Instituição
- Um usuário pode ter diferentes roles em cada instituição
- Roles disponíveis por instituição: ADMIN, MANAGER, VIEWER
- Um usuário pode ter múltiplos roles na mesma instituição
- Vinculação é soft delete (campo active em user_institutions)
- O usuário deve ser informado por email sobre vínculo/desvínculo (configurável via .env)

### Isolamento de Dados
- Usuário comum (não admin global) só acessa dados da instituição ativa
- Admin global pode acessar dados de todas as instituições
- Todas as entidades tenant-aware são filtradas automaticamente por instituição
- Tentativa de acesso a recurso de outra instituição resulta em 403 Forbidden

### Contexto de Sessão
- Usuário com múltiplas instituições deve selecionar uma ao fazer login
- Seleção é persistida em localStorage (frontend) e enviada via header em cada request
- Backend valida que o usuário pertence à instituição informada no header
- Admin global não precisa de instituição ativa (bypass de filtros)

---

## Critérios de aceite

### Backend
- [ ] Migration SQL cria tabelas: institutions, user_institutions, user_institution_roles
- [ ] Entidades JPA mapeadas corretamente com relacionamentos
- [ ] CRUD de instituições funciona (create, read, update, delete/soft delete)
- [ ] Vinculação/desvínculo de usuários funciona
- [ ] Endpoint GET /institutions lista com filtros (tipo, status, busca) e paginação
- [ ] Endpoint GET /user/institutions retorna instituições do usuário logado
- [ ] Tenant context (ThreadLocal) é setado via interceptor do header X-Institution-Id
- [ ] Hibernate filters aplicados automaticamente em queries
- [ ] Segurança: admin global acessa tudo, usuário comum apenas sua instituição
- [ ] Exceções customizadas (ResourceNotFoundException, UnauthorizedAccessException)
- [ ] Documentação Swagger completa com exemplos
- [ ] Testes unitários com cobertura 90%+ em services
- [ ] Testes de integração com Testcontainers

### Frontend
- [ ] Types TypeScript criados (Institution, UserInstitution, enums)
- [ ] Service de API implementado (InstitutionService)
- [ ] Pinia store gerencia instituição ativa
- [ ] Modal de seleção de instituição exibido ao login (se múltiplas)
- [ ] Switcher de instituição funciona no header (dropdown)
- [ ] Página de listagem de instituições (admin) com filtros e paginação
- [ ] Formulário de criação/edição de instituição
- [ ] Upload de logo com preview e validação
- [ ] Guards de rota verificam acesso admin
- [ ] Header X-Institution-Id enviado em todas as requisições
- [ ] localStorage persiste instituição ativa

### Infraestrutura
- [ ] Variáveis de ambiente configuradas (.env.example atualizado)
- [ ] Serviço de email implementado (mesmo que stub inicial)
- [ ] Templates HTML de email criados (vínculo, desvínculo)
- [ ] Sistema de storage de imagens (local ou MinIO)
- [ ] Processamento de imagem (conversão WebP, thumbnails)
- [ ] Tabela de auditoria (security_audit_log) criada e funcional

### Qualidade
- [ ] Código segue padrões (Checkstyle backend, ESLint frontend)
- [ ] Cobertura de testes: 85%+ global
- [ ] Documentação técnica atualizada
- [ ] README com instruções de configuração

---

## Tarefas

### FASE 1: Banco de Dados e Entidades

#### 1.1 - Criar Schema Multi-Tenant
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Criar estrutura de banco de dados completa para multi-tenancy.

**Arquivo**: `/backend/src/main/resources/db/migration/V2__create_multi_tenant_schema.sql`

**Tabelas a criar**:

1. **institutions**
   ```sql
   - id (UUID, PK)
   - name (VARCHAR 255, NOT NULL)
   - acronym (VARCHAR 50, NOT NULL, UNIQUE)
   - logo_url (VARCHAR 1024)
   - logo_thumbnail_url (VARCHAR 1024)
   - logo_uploaded_at (TIMESTAMP)
   - type (VARCHAR 50, NOT NULL) -- CHECK: FEDERAL, ESTADUAL, MUNICIPAL, PRIVADA
   - domain (VARCHAR 255, UNIQUE)
   - active (BOOLEAN, NOT NULL, DEFAULT true)
   - created_at (TIMESTAMP, NOT NULL)
   - updated_at (TIMESTAMP, NOT NULL)
   ```

2. **user_institutions**
   ```sql
   - id (UUID, PK)
   - user_id (UUID, NOT NULL, FK -> users.id)
   - institution_id (UUID, NOT NULL, FK -> institutions.id)
   - active (BOOLEAN, NOT NULL, DEFAULT true)
   - linked_at (TIMESTAMP, NOT NULL)
   - linked_by (UUID, FK -> users.id) -- quem criou o vínculo
   - updated_at (TIMESTAMP, NOT NULL)
   - UNIQUE (user_id, institution_id)
   ```

3. **user_institution_roles**
   ```sql
   - user_institution_id (UUID, NOT NULL, FK -> user_institutions.id)
   - role (VARCHAR 50, NOT NULL) -- CHECK: ADMIN, MANAGER, VIEWER
   ```

4. **security_audit_log**
   ```sql
   - id (UUID, PK)
   - user_id (UUID, NOT NULL)
   - institution_id (UUID)
   - action (VARCHAR 100, NOT NULL)
   - resource (VARCHAR 100, NOT NULL)
   - resource_id (UUID)
   - details (TEXT)
   - ip_address (VARCHAR 45)
   - user_agent (VARCHAR 500)
   - timestamp (TIMESTAMP, NOT NULL)
   - result (VARCHAR 50, NOT NULL) -- CHECK: SUCCESS, FAILURE, DENIED
   ```

**Índices essenciais**:
- institutions: (acronym), (domain), (active), (type), (created_at DESC)
- user_institutions: (user_id), (institution_id), (linked_at DESC)
- user_institution_roles: (user_institution_id)
- security_audit_log: (user_id), (institution_id), (timestamp DESC), (action)

**Triggers**:
- Trigger `update_updated_at_column()` em institutions e user_institutions

**Validação**:
- `mvn flyway:migrate` executa sem erros
- Todas as tabelas criadas no PostgreSQL
- Constraints e índices funcionando

---

#### 1.2 - Criar Entidades JPA
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Mapear tabelas do banco para entidades Java com JPA.

**Arquivos a criar** (package `com.simplifica.domain.entity`):

1. **InstitutionType.java** (enum)
   ```java
   public enum InstitutionType {
       FEDERAL, ESTADUAL, MUNICIPAL, PRIVADA;

       public static InstitutionType fromString(String type) {
           // Conversão case-insensitive com validação
       }
   }
   ```

2. **Institution.java**
   ```java
   @Entity
   @Table(name = "institutions")
   @Getter @Setter @Builder
   @NoArgsConstructor @AllArgsConstructor
   public class Institution {
       @Id @GeneratedValue(strategy = UUID)
       private UUID id;

       @Column(nullable = false, length = 255)
       private String name;

       @Column(nullable = false, unique = true, length = 50)
       private String acronym;

       @Column(name = "logo_url", length = 1024)
       private String logoUrl;

       @Column(name = "logo_thumbnail_url", length = 1024)
       private String logoThumbnailUrl;

       @Column(name = "logo_uploaded_at")
       private LocalDateTime logoUploadedAt;

       @Enumerated(EnumType.STRING)
       @Column(nullable = false, length = 50)
       private InstitutionType type;

       @Column(unique = true, length = 255)
       private String domain;

       @Column(nullable = false)
       private Boolean active = true;

       @Column(name = "created_at", nullable = false, updatable = false)
       private LocalDateTime createdAt;

       @Column(name = "updated_at", nullable = false)
       private LocalDateTime updatedAt;

       @PrePersist
       protected void onCreate() {
           createdAt = LocalDateTime.now();
           updatedAt = LocalDateTime.now();
       }

       @PreUpdate
       protected void onUpdate() {
           updatedAt = LocalDateTime.now();
       }

       public boolean isActive() {
           return Boolean.TRUE.equals(active);
       }
   }
   ```

3. **UserInstitution.java**
   ```java
   @Entity
   @Table(
       name = "user_institutions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "institution_id"})
   )
   @Getter @Setter @Builder
   public class UserInstitution {
       @Id @GeneratedValue(strategy = UUID)
       private UUID id;

       @ManyToOne(fetch = LAZY, optional = false)
       @JoinColumn(name = "user_id", nullable = false)
       private User user;

       @ManyToOne(fetch = LAZY, optional = false)
       @JoinColumn(name = "institution_id", nullable = false)
       private Institution institution;

       @ElementCollection(fetch = EAGER)
       @CollectionTable(
           name = "user_institution_roles",
           joinColumns = @JoinColumn(name = "user_institution_id")
       )
       @Column(name = "role")
       @Enumerated(EnumType.STRING)
       private Set<UserRole> roles = new HashSet<>();

       @Column(nullable = false)
       private Boolean active = true;

       @Column(name = "linked_at", nullable = false, updatable = false)
       private LocalDateTime linkedAt;

       @ManyToOne(fetch = LAZY)
       @JoinColumn(name = "linked_by")
       private User linkedBy;

       @Column(name = "updated_at", nullable = false)
       private LocalDateTime updatedAt;

       // Lifecycle hooks, helpers methods
   }
   ```

4. **SecurityAuditLog.java**
   ```java
   @Entity
   @Table(name = "security_audit_log")
   @Getter @Setter @Builder
   public class SecurityAuditLog {
       @Id @GeneratedValue(strategy = UUID)
       private UUID id;

       @Column(name = "user_id", nullable = false)
       private UUID userId;

       @Column(name = "institution_id")
       private UUID institutionId;

       @Column(nullable = false, length = 100)
       private String action;

       @Column(nullable = false, length = 100)
       private String resource;

       @Column(name = "resource_id")
       private UUID resourceId;

       @Column(columnDefinition = "TEXT")
       private String details;

       @Column(name = "ip_address", length = 45)
       private String ipAddress;

       @Column(name = "user_agent", length = 500)
       private String userAgent;

       @Column(nullable = false)
       private LocalDateTime timestamp;

       @Column(nullable = false, length = 50)
       private String result; // SUCCESS, FAILURE, DENIED

       @PrePersist
       protected void onCreate() {
           timestamp = LocalDateTime.now();
       }
   }
   ```

**Modificar**: `User.java` - adicionar relacionamento:
```java
@OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
private Set<UserInstitution> institutions = new HashSet<>();

// Helper methods
public Set<Institution> getActiveInstitutions() { ... }
public boolean belongsToInstitution(UUID institutionId) { ... }
public Set<UserRole> getRolesForInstitution(UUID institutionId) { ... }
```

**Validação**: Aplicação inicia sem erros de JPA, entidades reconhecidas

---

#### 1.3 - Criar Repositories JPA
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Interfaces para acesso ao banco de dados.

**Arquivos a criar** (package `com.simplifica.infrastructure.repository`):

1. **InstitutionRepository.java**
   ```java
   @Repository
   public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
       Optional<Institution> findByAcronym(String acronym);
       Optional<Institution> findByDomain(String domain);
       boolean existsByAcronym(String acronym);
       boolean existsByDomain(String domain);
       Page<Institution> findByActiveTrue(Pageable pageable);
       Page<Institution> findByType(InstitutionType type, Pageable pageable);

       @Query("SELECT i FROM Institution i WHERE " +
              "(:active IS NULL OR i.active = :active) AND " +
              "(:type IS NULL OR i.type = :type) AND " +
              "(:search IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
              "OR LOWER(i.acronym) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<Institution> findByFilters(
           @Param("active") Boolean active,
           @Param("type") InstitutionType type,
           @Param("search") String search,
           Pageable pageable
       );
   }
   ```

2. **UserInstitutionRepository.java**
   ```java
   @Repository
   public interface UserInstitutionRepository extends JpaRepository<UserInstitution, UUID> {
       List<UserInstitution> findByUserId(UUID userId);
       List<UserInstitution> findByInstitutionId(UUID institutionId);
       List<UserInstitution> findByUserIdAndActiveTrue(UUID userId);

       Optional<UserInstitution> findByUserIdAndInstitutionId(UUID userId, UUID institutionId);

       @Query("SELECT ui FROM UserInstitution ui " +
              "WHERE ui.user.id = :userId AND ui.institution.id = :institutionId " +
              "AND ui.active = true")
       Optional<UserInstitution> findActiveByUserAndInstitution(
           @Param("userId") UUID userId,
           @Param("institutionId") UUID institutionId
       );

       boolean existsByUserIdAndInstitutionId(UUID userId, UUID institutionId);

       @Query("SELECT COUNT(ui) FROM UserInstitution ui " +
              "WHERE ui.institution.id = :institutionId AND ui.active = true")
       long countActiveUsersByInstitution(@Param("institutionId") UUID institutionId);

       @Query("SELECT ui FROM UserInstitution ui " +
              "JOIN FETCH ui.user " +
              "JOIN FETCH ui.institution " +
              "WHERE ui.institution.id = :institutionId AND ui.active = true")
       List<UserInstitution> findActiveUsersWithInstitution(@Param("institutionId") UUID institutionId);
   }
   ```

3. **SecurityAuditLogRepository.java**
   ```java
   @Repository
   public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, UUID> {
       Page<SecurityAuditLog> findByUserId(UUID userId, Pageable pageable);
       Page<SecurityAuditLog> findByInstitutionId(UUID institutionId, Pageable pageable);

       @Query("SELECT sal FROM SecurityAuditLog sal WHERE " +
              "sal.timestamp BETWEEN :startDate AND :endDate")
       List<SecurityAuditLog> findByTimestampBetween(
           @Param("startDate") LocalDateTime startDate,
           @Param("endDate") LocalDateTime endDate
       );
   }
   ```

**Validação**: Repositories funcionam, queries personalizadas executam

---

### FASE 2: Services e DTOs

#### 2.1 - Criar DTOs
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Objetos de transferência para API REST.

**Arquivos a criar** (package `com.simplifica.application.dto`):

1. **InstitutionDTO.java**
   ```java
   @Data @Builder
   @NoArgsConstructor @AllArgsConstructor
   public class InstitutionDTO {
       private UUID id;
       private String name;
       private String acronym;
       private String logoUrl;
       private String logoThumbnailUrl;
       private InstitutionType type;
       private String domain;
       private Boolean active;
       private LocalDateTime createdAt;
       private LocalDateTime updatedAt;

       public static InstitutionDTO fromEntity(Institution institution) {
           if (institution == null) return null;
           return InstitutionDTO.builder()
               .id(institution.getId())
               .name(institution.getName())
               // ... map all fields
               .build();
       }
   }
   ```

2. **CreateInstitutionDTO.java**
   ```java
   @Data @Builder
   public class CreateInstitutionDTO {
       @NotBlank(message = "Name is required")
       @Size(max = 255)
       private String name;

       @NotBlank(message = "Acronym is required")
       @Size(max = 50)
       @Pattern(regexp = "^[A-Z0-9]+$", message = "Acronym must be uppercase alphanumeric")
       private String acronym;

       @NotNull(message = "Type is required")
       private InstitutionType type;

       @Size(max = 255)
       @Pattern(regexp = "^[a-z0-9.-]+\\.[a-z]{2,}$", message = "Invalid domain format")
       private String domain;

       private Boolean active = true;
   }
   ```

3. **UpdateInstitutionDTO.java**
   ```java
   @Data @Builder
   public class UpdateInstitutionDTO {
       @Size(max = 255)
       private String name;

       private InstitutionType type;

       @Size(max = 255)
       @Pattern(regexp = "^[a-z0-9.-]+\\.[a-z]{2,}$")
       private String domain;

       private Boolean active;
   }
   ```

4. **UserInstitutionDTO.java**
   ```java
   @Data @Builder
   public class UserInstitutionDTO {
       private UUID id;
       private UserDTO user;
       private InstitutionDTO institution;
       private Set<UserRole> roles;
       private Boolean active;
       private LocalDateTime linkedAt;

       public static UserInstitutionDTO fromEntity(UserInstitution ui) {
           // ... mapping
       }
   }
   ```

5. **AssignUserToInstitutionDTO.java**
   ```java
   @Data @Builder
   public class AssignUserToInstitutionDTO {
       @NotNull(message = "User ID is required")
       private UUID userId;

       @NotNull(message = "Institution ID is required")
       private UUID institutionId;

       @NotEmpty(message = "At least one role is required")
       private Set<UserRole> roles;
   }
   ```

**Validação**: DTOs compilam, validações Bean Validation funcionam

---

#### 2.2 - Criar Services de Negócio
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 4h

**Objetivo**: Implementar lógica de negócio para instituições e vinculações.

**Arquivos a criar** (package `com.simplifica.application.service`):

1. **InstitutionService.java**
   ```java
   @Service
   @Transactional(readOnly = true)
   @RequiredArgsConstructor
   public class InstitutionService {
       private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionService.class);

       private final InstitutionRepository institutionRepository;

       public Institution findById(UUID id) {
           LOGGER.debug("Finding institution by ID: {}", id);
           return institutionRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Institution", id.toString()));
       }

       public Institution findByAcronym(String acronym) {
           return institutionRepository.findByAcronym(acronym)
               .orElseThrow(() -> new ResourceNotFoundException("Institution", acronym));
       }

       public Page<Institution> findAll(Boolean active, InstitutionType type,
                                        String search, Pageable pageable) {
           LOGGER.debug("Finding institutions with filters - active: {}, type: {}, search: {}",
               active, type, search);

           if (search != null || active != null || type != null) {
               return institutionRepository.findByFilters(active, type, search, pageable);
           }
           return institutionRepository.findAll(pageable);
       }

       @Transactional
       public Institution create(CreateInstitutionDTO dto) {
           LOGGER.info("Creating new institution: {}", dto.getAcronym());

           // Validate uniqueness
           if (institutionRepository.existsByAcronym(dto.getAcronym())) {
               throw new ResourceAlreadyExistsException("Institution", "acronym", dto.getAcronym());
           }

           if (dto.getDomain() != null && institutionRepository.existsByDomain(dto.getDomain())) {
               throw new ResourceAlreadyExistsException("Institution", "domain", dto.getDomain());
           }

           Institution institution = Institution.builder()
               .name(dto.getName())
               .acronym(dto.getAcronym().toUpperCase())
               .type(dto.getType())
               .domain(dto.getDomain() != null ? dto.getDomain().toLowerCase() : null)
               .active(dto.getActive() != null ? dto.getActive() : true)
               .build();

           Institution saved = institutionRepository.save(institution);
           LOGGER.info("Created institution with ID: {}", saved.getId());
           return saved;
       }

       @Transactional
       public Institution update(UUID id, UpdateInstitutionDTO dto) {
           LOGGER.info("Updating institution: {}", id);

           Institution institution = findById(id);

           if (dto.getName() != null) {
               institution.setName(dto.getName());
           }
           if (dto.getType() != null) {
               institution.setType(dto.getType());
           }
           if (dto.getDomain() != null) {
               if (!dto.getDomain().equals(institution.getDomain())
                   && institutionRepository.existsByDomain(dto.getDomain())) {
                   throw new ResourceAlreadyExistsException("Institution", "domain", dto.getDomain());
               }
               institution.setDomain(dto.getDomain().toLowerCase());
           }
           if (dto.getActive() != null) {
               institution.setActive(dto.getActive());
           }

           return institutionRepository.save(institution);
       }

       @Transactional
       public void delete(UUID id) {
           LOGGER.info("Soft deleting institution: {}", id);
           Institution institution = findById(id);
           institution.setActive(false);
           institutionRepository.save(institution);
       }
   }
   ```

2. **UserInstitutionService.java**
   ```java
   @Service
   @Transactional(readOnly = true)
   @RequiredArgsConstructor
   public class UserInstitutionService {
       private static final Logger LOGGER = LoggerFactory.getLogger(UserInstitutionService.class);

       private final UserInstitutionRepository userInstitutionRepository;
       private final UserService userService;
       private final InstitutionService institutionService;
       private final EmailNotificationService emailNotificationService;

       @Value("${app.features.email-notifications:false}")
       private boolean emailNotificationsEnabled;

       @Transactional
       public UserInstitution assignUserToInstitution(AssignUserToInstitutionDTO dto) {
           LOGGER.info("Assigning user {} to institution {}", dto.getUserId(), dto.getInstitutionId());

           User user = userService.findById(dto.getUserId());
           Institution institution = institutionService.findById(dto.getInstitutionId());

           if (userInstitutionRepository.existsByUserIdAndInstitutionId(
               dto.getUserId(), dto.getInstitutionId())) {
               throw new ResourceAlreadyExistsException(
                   "User-Institution link already exists");
           }

           UserInstitution userInstitution = UserInstitution.builder()
               .user(user)
               .institution(institution)
               .roles(dto.getRoles())
               .active(true)
               .build();

           UserInstitution saved = userInstitutionRepository.save(userInstitution);

           if (emailNotificationsEnabled) {
               emailNotificationService.sendUserAssignedToInstitutionEmail(
                   user, institution, dto.getRoles());
           }

           return saved;
       }

       @Transactional
       public UserInstitution updateRoles(UUID userId, UUID institutionId, Set<UserRole> roles) {
           LOGGER.info("Updating roles for user {} in institution {}", userId, institutionId);

           UserInstitution userInstitution = userInstitutionRepository
               .findByUserIdAndInstitutionId(userId, institutionId)
               .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

           userInstitution.setRoles(roles);
           return userInstitutionRepository.save(userInstitution);
       }

       @Transactional
       public void removeUserFromInstitution(UUID userId, UUID institutionId) {
           LOGGER.info("Removing user {} from institution {}", userId, institutionId);

           UserInstitution userInstitution = userInstitutionRepository
               .findByUserIdAndInstitutionId(userId, institutionId)
               .orElseThrow(() -> new ResourceNotFoundException("User-Institution link not found"));

           User user = userInstitution.getUser();
           Institution institution = userInstitution.getInstitution();

           userInstitution.setActive(false);
           userInstitutionRepository.save(userInstitution);

           if (emailNotificationsEnabled) {
               emailNotificationService.sendUserRemovedFromInstitutionEmail(user, institution);
           }
       }

       public List<UserInstitution> getUserInstitutions(UUID userId) {
           return userInstitutionRepository.findByUserIdAndActiveTrue(userId);
       }

       public List<UserInstitution> getInstitutionUsers(UUID institutionId) {
           return userInstitutionRepository.findActiveUsersWithInstitution(institutionId);
       }

       public boolean userBelongsToInstitution(UUID userId, UUID institutionId) {
           return userInstitutionRepository
               .findActiveByUserAndInstitution(userId, institutionId)
               .isPresent();
       }
   }
   ```

3. **EmailNotificationService.java** (stub inicial)
   ```java
   @Service
   public class EmailNotificationService {
       private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

       public void sendUserAssignedToInstitutionEmail(User user, Institution institution, Set<UserRole> roles) {
           LOGGER.info("TODO: Send email to {} - Assigned to institution {} with roles {}",
               user.getEmail(), institution.getName(), roles);
           // TODO: Implement email sending (Phase 6)
       }

       public void sendUserRemovedFromInstitutionEmail(User user, Institution institution) {
           LOGGER.info("TODO: Send email to {} - Removed from institution {}",
               user.getEmail(), institution.getName());
           // TODO: Implement email sending (Phase 6)
       }
   }
   ```

**Validação**: Services funcionam, transações corretas, logs apropriados

---

### FASE 3: Controllers REST e Segurança

#### 3.1 - Criar Exception Classes
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 45min

**Objetivo**: Exceções customizadas para casos de negócio.

**Arquivos a criar** (package `com.simplifica.presentation.exception`):

1. **ResourceNotFoundException.java**
2. **ResourceAlreadyExistsException.java**
3. **UnauthorizedAccessException.java**

**Modificar**: `GlobalExceptionHandler.java` - adicionar handlers:
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
    // Return 404
}

@ExceptionHandler(ResourceAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
    // Return 409 Conflict
}

@ExceptionHandler(UnauthorizedAccessException.class)
public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
    // Return 403 Forbidden
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    // Return 400 with field errors
}
```

**Validação**: Exceções retornam JSON padronizado com status HTTP correto

---

#### 3.2 - Criar Controllers REST
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Endpoints da API REST para instituições.

**Arquivo a criar**: `/backend/src/main/java/com/simplifica/presentation/controller/InstitutionController.java`

```java
@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;
    private final UserInstitutionService userInstitutionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<InstitutionDTO>> listInstitutions(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) InstitutionType type,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "name", direction = ASC) Pageable pageable
    ) {
        Page<Institution> institutions = institutionService.findAll(active, type, search, pageable);
        Page<InstitutionDTO> dtos = institutions.map(InstitutionDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> getInstitution(@PathVariable UUID id) {
        Institution institution = institutionService.findById(id);
        return ResponseEntity.ok(InstitutionDTO.fromEntity(institution));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> createInstitution(
        @Valid @RequestBody CreateInstitutionDTO dto
    ) {
        Institution institution = institutionService.create(dto);
        return ResponseEntity.status(CREATED).body(InstitutionDTO.fromEntity(institution));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstitutionDTO> updateInstitution(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInstitutionDTO dto
    ) {
        Institution institution = institutionService.update(id, dto);
        return ResponseEntity.ok(InstitutionDTO.fromEntity(institution));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInstitution(@PathVariable UUID id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInstitutionDTO>> getInstitutionUsers(@PathVariable UUID id) {
        List<UserInstitution> userInstitutions = userInstitutionService.getInstitutionUsers(id);
        List<UserInstitutionDTO> dtos = userInstitutions.stream()
            .map(UserInstitutionDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInstitutionDTO> assignUserToInstitution(
        @PathVariable UUID id,
        @Valid @RequestBody AssignUserToInstitutionDTO dto
    ) {
        if (!id.equals(dto.getInstitutionId())) {
            throw new IllegalArgumentException("Institution ID mismatch");
        }
        UserInstitution userInstitution = userInstitutionService.assignUserToInstitution(dto);
        return ResponseEntity.status(CREATED).body(UserInstitutionDTO.fromEntity(userInstitution));
    }

    @DeleteMapping("/{institutionId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserFromInstitution(
        @PathVariable UUID institutionId,
        @PathVariable UUID userId
    ) {
        userInstitutionService.removeUserFromInstitution(userId, institutionId);
        return ResponseEntity.noContent().build();
    }
}
```

**Modificar**: `UserController.java` - adicionar endpoints:
```java
@GetMapping("/institutions")
public ResponseEntity<List<InstitutionDTO>> getUserInstitutions(
    @AuthenticationPrincipal UserPrincipal userPrincipal
) {
    List<UserInstitution> userInstitutions =
        userInstitutionService.getUserInstitutions(userPrincipal.getId());
    List<InstitutionDTO> institutions = userInstitutions.stream()
        .map(ui -> InstitutionDTO.fromEntity(ui.getInstitution()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(institutions);
}

@GetMapping("/institutions/{institutionId}/validate")
public ResponseEntity<Map<String, Object>> validateInstitutionAccess(
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @PathVariable UUID institutionId
) {
    boolean hasAccess = userInstitutionService.userBelongsToInstitution(
        userPrincipal.getId(), institutionId);
    return ResponseEntity.ok(Map.of(
        "hasAccess", hasAccess,
        "institutionId", institutionId
    ));
}
```

**Validação**:
- Endpoints retornam status HTTP corretos
- @PreAuthorize funciona (admin access only)
- Validações Bean Validation funcionam
- Paginação e filtros funcionam

---

#### 3.3 - Implementar Sistema de Tenant Context
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Infraestrutura para isolamento de dados por instituição.

**Arquivos a criar**:

1. **TenantContext.java** (package `com.simplifica.config.tenant`)
   ```java
   public class TenantContext {
       private static final ThreadLocal<UUID> CURRENT_INSTITUTION = new ThreadLocal<>();

       public static void setCurrentInstitution(UUID institutionId) {
           CURRENT_INSTITUTION.set(institutionId);
       }

       public static UUID getCurrentInstitution() {
           return CURRENT_INSTITUTION.get();
       }

       public static void clear() {
           CURRENT_INSTITUTION.remove();
       }

       public static boolean isSet() {
           return CURRENT_INSTITUTION.get() != null;
       }
   }
   ```

2. **TenantInterceptor.java**
   ```java
   @Component
   public class TenantInterceptor implements HandlerInterceptor {
       private static final Logger LOGGER = LoggerFactory.getLogger(TenantInterceptor.class);
       private static final String INSTITUTION_HEADER = "X-Institution-Id";

       @Override
       public boolean preHandle(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler) {
           String institutionIdHeader = request.getHeader(INSTITUTION_HEADER);

           if (institutionIdHeader != null && !institutionIdHeader.isEmpty()) {
               try {
                   UUID institutionId = UUID.fromString(institutionIdHeader);

                   Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                   if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                       UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                       // TODO: Validate user belongs to institution
                       TenantContext.setCurrentInstitution(institutionId);
                       LOGGER.debug("Set tenant context to institution: {}", institutionId);
                   }
               } catch (IllegalArgumentException e) {
                   LOGGER.warn("Invalid institution ID format: {}", institutionIdHeader);
               }
           }
           return true;
       }

       @Override
       public void afterCompletion(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object handler,
                                   Exception ex) {
           TenantContext.clear();
           LOGGER.debug("Cleared tenant context");
       }
   }
   ```

3. **WebMvcConfig.java**
   ```java
   @Configuration
   public class WebMvcConfig implements WebMvcConfigurer {
       @Autowired
       private TenantInterceptor tenantInterceptor;

       @Override
       public void addInterceptors(InterceptorRegistry registry) {
           registry.addInterceptor(tenantInterceptor)
               .addPathPatterns("/**")
               .excludePathPatterns(
                   "/public/**",
                   "/oauth2/**",
                   "/actuator/**",
                   "/auth/**",
                   "/institutions/**" // Admin endpoints don't need tenant context
               );
       }
   }
   ```

**Dependência**: Adicionar ao `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**Validação**: Header X-Institution-Id é capturado, TenantContext setado corretamente

---

#### 3.4 - Implementar Camada de Segurança
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Autorização granular por instituição.

**Arquivos a criar**:

1. **InstitutionSecurityService.java** (package `com.simplifica.application.service`)
   ```java
   @Service
   public class InstitutionSecurityService {
       @Autowired
       private UserInstitutionRepository userInstitutionRepository;

       public boolean hasAccessToInstitution(UUID institutionId) {
           UserPrincipal principal = getCurrentUser();
           return userInstitutionRepository.existsByUserIdAndInstitutionId(
               principal.getId(), institutionId);
       }

       public boolean hasRoleInInstitution(UUID institutionId, UserRole... roles) {
           UserPrincipal principal = getCurrentUser();
           UserInstitution userInst = userInstitutionRepository
               .findByUserIdAndInstitutionId(principal.getId(), institutionId)
               .orElse(null);

           if (userInst == null) return false;
           return userInst.getRoles().stream()
               .anyMatch(role -> Arrays.asList(roles).contains(role));
       }

       public boolean canManageInstitution(UUID institutionId) {
           UserPrincipal principal = getCurrentUser();
           if (principal.isAdmin()) return true; // Global admin bypass
           return hasRoleInInstitution(institutionId, UserRole.ADMIN);
       }

       private UserPrincipal getCurrentUser() {
           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           return (UserPrincipal) auth.getPrincipal();
       }
   }
   ```

2. **Custom Annotations** (package `com.simplifica.config.security`):
   ```java
   @Target({METHOD, TYPE})
   @Retention(RUNTIME)
   @PreAuthorize("@institutionSecurityService.hasAccessToInstitution(#institutionId)")
   public @interface RequireInstitutionAccess {
   }

   @Target({METHOD, TYPE})
   @Retention(RUNTIME)
   @PreAuthorize("@institutionSecurityService.hasRoleInInstitution(#institutionId, #roles)")
   public @interface RequireInstitutionRole {
       UserRole[] value();
   }
   ```

**Modificar**: `UserPrincipal.java` - adicionar campo:
```java
private UUID currentInstitutionId; // Institution currently selected by user

public void setCurrentInstitutionId(UUID institutionId) {
    this.currentInstitutionId = institutionId;
}

public UUID getCurrentInstitutionId() {
    return this.currentInstitutionId;
}
```

**Validação**: Annotations funcionam, segurança aplicada corretamente

---

### FASE 4: Frontend - Types, Services e State

#### 4.1 - Criar Types TypeScript
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Definir interfaces TypeScript para instituições.

**Arquivo a criar**: `/frontend/src/types/institution.types.ts`

```typescript
export enum InstitutionType {
  FEDERAL = 'FEDERAL',
  ESTADUAL = 'ESTADUAL',
  MUNICIPAL = 'MUNICIPAL',
  PRIVADA = 'PRIVADA',
}

export enum InstitutionStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export interface Institution {
  id: string
  name: string
  acronym: string
  logoUrl?: string
  logoThumbnailUrl?: string
  type: InstitutionType
  domain?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface InstitutionCreateRequest {
  name: string
  acronym: string
  type: InstitutionType
  domain?: string
  active: boolean
  logo?: File
}

export interface InstitutionUpdateRequest {
  name?: string
  type?: InstitutionType
  domain?: string
  active?: boolean
  logo?: File
}

export interface UserInstitution {
  id: string
  userId: string
  institutionId: string
  institution: Institution
  roles: string[]
  active: boolean
  linkedAt: string
}

export interface InstitutionListParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
  search?: string
  type?: InstitutionType
  active?: boolean
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
```

**Validação**: Types compilam sem erros, autocomplete funciona

---

#### 4.2 - Criar Service de API
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Comunicação com backend para instituições.

**Arquivo a criar**: `/frontend/src/services/institution.service.ts`

```typescript
import apiClient from './api'
import type {
  Institution,
  InstitutionCreateRequest,
  InstitutionUpdateRequest,
  InstitutionListParams,
  PageResponse,
  UserInstitution,
} from '@/types/institution.types'

class InstitutionService {
  private readonly BASE_PATH = '/institutions'

  async list(params: InstitutionListParams): Promise<PageResponse<Institution>> {
    const response = await apiClient.get<PageResponse<Institution>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'asc'}` : undefined,
        search: params.search,
        type: params.type,
        active: params.active,
      },
    })
    return response.data
  }

  async getById(id: string): Promise<Institution> {
    const response = await apiClient.get<Institution>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  async create(data: InstitutionCreateRequest): Promise<Institution> {
    const formData = this.buildFormData(data)
    const response = await apiClient.post<Institution>(this.BASE_PATH, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  async update(id: string, data: InstitutionUpdateRequest): Promise<Institution> {
    const formData = this.buildFormData(data)
    const response = await apiClient.put<Institution>(`${this.BASE_PATH}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  async getUserInstitutions(): Promise<UserInstitution[]> {
    const response = await apiClient.get<UserInstitution[]>('/user/institutions')
    return response.data
  }

  async linkUserToInstitution(
    institutionId: string,
    userId: string,
    roles: string[]
  ): Promise<UserInstitution> {
    const response = await apiClient.post<UserInstitution>(
      `${this.BASE_PATH}/${institutionId}/users`,
      { userId, institutionId, roles }
    )
    return response.data
  }

  async unlinkUserFromInstitution(institutionId: string, userId: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${institutionId}/users/${userId}`)
  }

  private buildFormData(data: InstitutionCreateRequest | InstitutionUpdateRequest): FormData {
    const formData = new FormData()
    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (key === 'logo' && value instanceof File) {
          formData.append('logo', value)
        } else if (key !== 'logo') {
          formData.append(key, String(value))
        }
      }
    })
    return formData
  }
}

export const institutionService = new InstitutionService()
```

**Modificar**: `/frontend/src/services/api.ts` - adicionar no interceptor de request:
```typescript
// After token header
const institutionId = localStorage.getItem('active_institution_id')
if (institutionId && config.headers) {
  config.headers['X-Institution-Id'] = institutionId
}
```

**Validação**: Service funciona, chamadas API corretas

---

#### 4.3 - Criar Pinia Store
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Gerenciar estado global de instituição ativa.

**Arquivo a criar**: `/frontend/src/stores/institution.store.ts`

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Institution, UserInstitution } from '@/types/institution.types'
import { institutionService } from '@/services/institution.service'

export const useInstitutionStore = defineStore('institution', () => {
  // State
  const activeInstitution = ref<Institution | null>(null)
  const userInstitutions = ref<UserInstitution[]>([])
  const isLoading = ref(false)

  // Getters
  const hasActiveInstitution = computed(() => !!activeInstitution.value)
  const hasMultipleInstitutions = computed(() => userInstitutions.value.length > 1)
  const activeInstitutionId = computed(() => activeInstitution.value?.id || null)

  // Actions
  async function fetchUserInstitutions(): Promise<void> {
    isLoading.value = true
    try {
      userInstitutions.value = await institutionService.getUserInstitutions()

      // Auto-select if only one institution
      if (userInstitutions.value.length === 1 && !activeInstitution.value) {
        await selectInstitution(userInstitutions.value[0].institution.id)
      } else {
        // Restore from localStorage
        const storedId = localStorage.getItem('active_institution_id')
        if (storedId) {
          const exists = userInstitutions.value.find(ui => ui.institution.id === storedId)
          if (exists) {
            await selectInstitution(storedId)
          }
        }
      }
    } catch (error) {
      console.error('Failed to fetch user institutions:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function selectInstitution(institutionId: string): Promise<void> {
    const userInstitution = userInstitutions.value.find(
      ui => ui.institution.id === institutionId
    )

    if (!userInstitution) {
      throw new Error('Institution not found in user institutions')
    }

    activeInstitution.value = userInstitution.institution
    localStorage.setItem('active_institution_id', institutionId)
  }

  function clearActiveInstitution(): void {
    activeInstitution.value = null
    localStorage.removeItem('active_institution_id')
  }

  function reset(): void {
    activeInstitution.value = null
    userInstitutions.value = []
    isLoading.value = false
    localStorage.removeItem('active_institution_id')
  }

  return {
    // State
    activeInstitution,
    userInstitutions,
    isLoading,
    // Getters
    hasActiveInstitution,
    hasMultipleInstitutions,
    activeInstitutionId,
    // Actions
    fetchUserInstitutions,
    selectInstitution,
    clearActiveInstitution,
    reset,
  }
})
```

**Arquivo a criar**: `/frontend/src/composables/useInstitution.ts`

```typescript
import { computed } from 'vue'
import { useInstitutionStore } from '@/stores/institution.store'

export function useInstitution() {
  const institutionStore = useInstitutionStore()

  const activeInstitution = computed(() => institutionStore.activeInstitution)
  const userInstitutions = computed(() => institutionStore.userInstitutions)
  const hasMultipleInstitutions = computed(() => institutionStore.hasMultipleInstitutions)
  const isLoading = computed(() => institutionStore.isLoading)

  const selectInstitution = async (institutionId: string) => {
    await institutionStore.selectInstitution(institutionId)
  }

  return {
    activeInstitution,
    userInstitutions,
    hasMultipleInstitutions,
    isLoading,
    selectInstitution,
  }
}
```

**Validação**: Store gerencia estado, persistência funciona

---

### FASE 5: Frontend - Componentes e Views

#### 5.1 - Criar Componentes Vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 6h

**Objetivo**: Componentes reutilizáveis para instituições.

**Arquivos a criar** (em `/frontend/src/components/institution/`):

1. **InstitutionSelectorModal.vue** - Modal para seleção de instituição ao login
2. **InstitutionSwitcher.vue** - Dropdown no header para troca de instituição
3. **InstitutionForm.vue** - Formulário de criação/edição com upload de logo
4. **InstitutionList.vue** - Tabela com filtros, paginação e ordenação

**Detalhes técnicos**:
- Usar Composition API (`<script setup lang="ts">`)
- Componentes Vuetify (v-dialog, v-data-table-server, v-form, v-file-input)
- Props tipadas com TypeScript
- Emits tipados para comunicação com parents
- Validações inline com rules
- Loading states e feedback visual
- Responsive design (mobile-first)

**Exemplo - InstitutionForm.vue** (estrutura):
```vue
<template>
  <v-form ref="formRef" v-model="isValid" @submit.prevent="handleSubmit">
    <v-row>
      <!-- Logo Upload -->
      <v-col cols="12" class="text-center">
        <v-avatar size="120" rounded>
          <v-img v-if="logoPreview" :src="logoPreview" />
          <v-icon v-else>mdi-office-building</v-icon>
        </v-avatar>
        <input ref="fileInput" type="file" accept="image/*" hidden @change="handleFileChange" />
        <v-btn @click="triggerFileInput">Upload Logo</v-btn>
      </v-col>

      <!-- Name -->
      <v-col cols="12" md="8">
        <v-text-field
          v-model="formData.name"
          label="Nome da Instituição *"
          :rules="[rules.required, rules.maxLength(255)]"
          counter="255"
          required
        />
      </v-col>

      <!-- Acronym -->
      <v-col cols="12" md="4">
        <v-text-field
          v-model="formData.acronym"
          label="Sigla *"
          :rules="[rules.required, rules.maxLength(50)]"
          counter="50"
          required
        />
      </v-col>

      <!-- Type -->
      <v-col cols="12" md="6">
        <v-select
          v-model="formData.type"
          label="Tipo *"
          :items="typeOptions"
          :rules="[rules.required]"
          required
        />
      </v-col>

      <!-- Domain -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.domain"
          label="Domínio (opcional)"
          placeholder="exemplo.br"
          :rules="[rules.domain]"
          hint="Domínio de e-mail para vínculo automático"
        />
      </v-col>

      <!-- Actions -->
      <v-col cols="12" class="d-flex justify-end gap-2">
        <v-btn variant="outlined" @click="$emit('cancel')">Cancelar</v-btn>
        <v-btn type="submit" color="primary" :loading="loading">
          {{ isEditMode ? 'Atualizar' : 'Criar' }}
        </v-btn>
      </v-col>
    </v-row>
  </v-form>
</template>
```

**Validação**: Componentes renderizam, validações funcionam, eventos emitidos

---

#### 5.2 - Criar Views/Páginas
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 4h

**Objetivo**: Páginas da aplicação para instituições.

**Arquivos a criar**:

1. **InstitutionsPage.vue** (`/frontend/src/views/private/admin/InstitutionsPage.vue`)
   - Usa InstitutionList component
   - Dialogs para create/edit/delete
   - Gerencia estado: formDialog, deleteDialog, selectedInstitution
   - Snackbar para feedback

2. **InstitutionSelectionPage.vue** (`/frontend/src/views/private/InstitutionSelectionPage.vue`)
   - Layout centralizado
   - Cards para cada instituição (grid)
   - Ao selecionar: chama selectInstitution() e router.push('/dashboard')

**Rotas a adicionar** (`/frontend/src/router/index.ts`):
```typescript
{
  path: '/institution-selection',
  name: 'institution-selection',
  component: () => import('@/views/private/InstitutionSelectionPage.vue'),
  meta: {
    layout: 'private',
    requiresAuth: true,
    requiresInstitution: false,
  },
},
{
  path: '/admin/institutions',
  name: 'admin-institutions',
  component: () => import('@/views/private/admin/InstitutionsPage.vue'),
  meta: {
    layout: 'private',
    requiresAuth: true,
    requiresInstitution: true,
    requiresAdmin: true,
  },
},
```

**Modificar**: `/frontend/src/router/guards.ts`
```typescript
// Check institution context requirement
if (requiresAuth && authStore.isAuthenticated && requiresInstitution) {
  if (institutionStore.userInstitutions.length === 0) {
    await institutionStore.fetchUserInstitutions()
  }

  if (institutionStore.hasMultipleInstitutions && !institutionStore.hasActiveInstitution) {
    next({ name: 'institution-selection', query: { redirect: to.fullPath } })
    return
  }
}

// Admin role check
if (requiresAdmin && !authStore.isAdmin) {
  next({ name: 'dashboard' })
  return
}
```

**Validação**: Páginas renderizam, fluxo de navegação funciona

---

#### 5.3 - Integrar Componentes no Layout
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Adicionar switcher de instituição no layout.

**Modificar**: `/frontend/src/components/navigation/AppHeader.vue`
```vue
<template>
  <v-app-bar>
    <v-app-bar-nav-icon @click="toggleDrawer" />
    <v-toolbar-title>{{ appName }}</v-toolbar-title>
    <v-spacer />

    <!-- Institution Switcher -->
    <InstitutionSwitcher />

    <v-btn icon disabled>
      <v-icon>mdi-bell-outline</v-icon>
    </v-btn>
    <UserProfile />
  </v-app-bar>
</template>

<script setup lang="ts">
import InstitutionSwitcher from '@/components/institution/InstitutionSwitcher.vue'
// ...
</script>
```

**Modificar**: `/frontend/src/components/navigation/AppSidebar.vue`
```typescript
const adminItems = computed(() => {
  if (!isAdmin.value) return []
  return [
    {
      title: 'Instituições',
      icon: 'mdi-office-building',
      to: '/admin/institutions',
    },
    {
      title: 'Usuários',
      icon: 'mdi-account-group',
      to: '/admin/users',
    },
  ]
})
```

**Modificar**: `/frontend/src/stores/auth.store.ts`
```typescript
import { useInstitutionStore } from './institution.store'

function logout(): void {
  user.value = null
  token.value = null
  localStorage.removeItem('auth_token')

  // Reset institution store
  const institutionStore = useInstitutionStore()
  institutionStore.reset()
}
```

**Validação**: Switcher aparece no header, menu admin tem Instituições

---

### FASE 6: Infraestrutura e Serviços

#### 6.1 - Configurar Variáveis de Ambiente
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Adicionar configurações necessárias.

**Modificar**: `/backend/.env.example`
```bash
# ========== FEATURES ==========
# Enable email notifications for user-institution assignments
EMAIL_NOTIFICATIONS_ENABLED=false

# Email configuration (when enabled)
# SMTP_HOST=smtp.gmail.com
# SMTP_PORT=587
# SMTP_USERNAME=your-email@gmail.com
# SMTP_PASSWORD=your-app-password
# EMAIL_FROM=noreply@simplifica.com
# EMAIL_FROM_NAME=Simplifica

# Storage configuration
STORAGE_PROVIDER=local
STORAGE_LOCAL_BASE_PATH=/var/simplifica/uploads
STORAGE_LOCAL_PUBLIC_URL=http://localhost:8080/api/public/uploads
STORAGE_MAX_FILE_SIZE_MB=5
```

**Modificar**: `/backend/src/main/resources/application.yml`
```yaml
app:
  features:
    email-notifications: ${EMAIL_NOTIFICATIONS_ENABLED:false}
  storage:
    provider: ${STORAGE_PROVIDER:local}
    local:
      base-path: ${STORAGE_LOCAL_BASE_PATH:/tmp/simplifica}
      public-url: ${STORAGE_LOCAL_PUBLIC_URL:http://localhost:8080/api/public/uploads}
    max-file-size-mb: ${STORAGE_MAX_FILE_SIZE_MB:5}
```

**Validação**: Variáveis carregadas corretamente

---

#### 6.2 - Implementar Serviço de Email (Opcional)
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 4h

**Objetivo**: Envio de emails com templates HTML.

**Dependências** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

**Arquivos a criar**:

1. **EmailServiceImpl.java** - implementação do service
2. **EmailConfig.java** - configuração do JavaMailSender
3. **Templates** (`/backend/src/main/resources/templates/email/`):
   - `institution/link-notification.html`
   - `institution/unlink-notification.html`
   - `base/layout.html`
   - `fragments/header.html`, `fragments/footer.html`

**Características**:
- Templates HTML responsivos com CSS inline
- Processamento assíncrono (@Async)
- Retry logic (spring-retry)
- Logs estruturados

**Validação**: Emails enviados (verificar logs), templates renderizam

---

#### 6.3 - Implementar Sistema de Storage (Opcional)
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 4h

**Objetivo**: Armazenamento de logos de instituições.

**Arquivos a criar** (package `com.simplifica.infrastructure.storage`):

1. **StorageService.java** (interface)
2. **impl/LocalStorageService.java** - implementação com filesystem
3. **StorageProperties.java** - configuração
4. **config/StorageConfig.java** - beans factory

**Funcionalidades**:
- Upload de imagem com validação (MIME type, tamanho)
- Conversão para WebP (opcional)
- Geração de thumbnails (opcional)
- URL pública para acesso

**Endpoint público** para servir imagens:
```java
@GetMapping("/public/uploads/**")
public void serveFile(HttpServletRequest request, HttpServletResponse response) {
    // Serve file from storage
}
```

**Validação**: Upload funciona, imagens acessíveis via URL

---

#### 6.4 - Configurar Documentação Swagger
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Documentação OpenAPI completa.

**Dependência** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Arquivo a criar**: `/backend/src/main/java/com/simplifica/config/OpenApiConfig.java`

**Anotar controllers** com:
- @Tag(name, description)
- @Operation(summary, description, responses)
- @Parameter(description, required, examples)

**Anotar DTOs** com:
- @Schema(name, description)
- @Schema(description, example) nos campos

**application.yml**:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**Validação**: Acessar http://localhost:8080/swagger-ui.html, documentação completa

---

### FASE 7: Testes e Qualidade

#### 7.1 - Criar Testes Unitários Backend
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 4h

**Objetivo**: Testes unitários com Mockito.

**Arquivos a criar** (em `/backend/src/test/java/com/simplifica/unit/`):

1. **service/InstitutionServiceTest.java**
   - Testar create com validação de unicidade
   - Testar update parcial
   - Testar delete (soft delete)
   - Testar findByFilters

2. **service/UserInstitutionServiceTest.java**
   - Testar assignUser com validação de duplicata
   - Testar removeUser (soft delete)
   - Testar updateRoles

3. **security/InstitutionSecurityServiceTest.java**
   - Testar hasAccessToInstitution (positivo/negativo)
   - Testar hasRoleInInstitution
   - Testar canManageInstitution com admin bypass

**Fixture**: `fixture/InstitutionFixture.java` - métodos helper para criar objetos de teste

**Padrão**:
- @ExtendWith(MockitoExtension.class)
- @Mock para dependências
- @InjectMocks para service
- Nomenclatura: `shouldDoSomethingWhenCondition()`

**Validação**: `mvn test` passa com 90%+ cobertura em services

---

#### 7.2 - Criar Testes de Integração Backend
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 4h

**Objetivo**: Testes de integração com Testcontainers.

**Arquivos a criar** (em `/backend/src/test/java/com/simplifica/integration/`):

1. **repository/InstitutionRepositoryIT.java**
   - Testar queries complexas com filtros
   - Testar paginação e ordenação

2. **controller/InstitutionControllerIT.java**
   - Testar endpoints protegidos (admin)
   - Testar CRUD completo via REST
   - Testar validações de input

**Setup**:
- @SpringBootTest com webEnvironment = RANDOM_PORT
- @Testcontainers com PostgreSQLContainer
- @DynamicPropertySource
- TestRestTemplate para requests HTTP

**Validação**: `mvn verify` passa, testes de integração funcionam

---

#### 7.3 - Criar Testes Unitários Frontend
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Testes de componentes e composables.

**Arquivos a criar** (em `/frontend/tests/unit/`):

1. **stores/institution.store.spec.ts**
   - Testar fetchUserInstitutions
   - Testar selectInstitution (persistência localStorage)
   - Testar reset

2. **composables/useInstitution.spec.ts**
   - Testar computed properties
   - Testar métodos

**Setup**: Vitest + Vue Test Utils

**Validação**: `npm test` passa

---

### FASE 8: Documentação Final

#### 8.1 - Atualizar Documentação
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Documentar feature completa.

**Arquivos a atualizar**:

1. **/README.md**
   - Adicionar seção "Features" com Multi-Tenant
   - Instruções de configuração (.env)

2. **/CONTRIBUTING.md**
   - Já atualizado com padrões

3. **/management/features/feature-001-multi-tenant.md**
   - Marcar tarefas como concluídas
   - Adicionar notas de implementação

**Arquivo a criar**: `/docs/architecture/multi-tenant.md`
- Diagrama de entidades
- Fluxo de autenticação
- Estratégia de isolamento
- Decisões arquiteturais

**Validação**: Documentação clara e completa

---

## Dependências Técnicas

### Backend
```xml
<!-- Já existentes -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
postgresql
flyway
lombok

<!-- Novas -->
spring-boot-starter-aop (Tenant Context)
spring-boot-starter-mail (Email - opcional)
spring-boot-starter-thymeleaf (Templates Email - opcional)
springdoc-openapi-starter-webmvc-ui (Swagger)
```

### Frontend
```json
// Já existentes
"vue": "^3.4",
"vuetify": "^3.5",
"pinia": "^2.1",
"vue-router": "^4.2",
"axios": "^1.6",
"typescript": "^5.2"

// Novas - nenhuma
```

---

## Riscos e Mitigações

### Risco 1: Complexidade do Tenant Context
**Mitigação**: Implementar de forma incremental, começar com ThreadLocal simples antes de adicionar Hibernate Filters

### Risco 2: Performance com Filtros Hibernate
**Mitigação**: Índices bem planejados, queries otimizadas, monitoramento de slow queries

### Risco 3: Confusão de Contexto (usuário acessa dados errados)
**Mitigação**: Testes rigorosos de isolamento, validações em múltiplas camadas, auditoria completa

### Risco 4: Upload de Arquivos Maliciosos
**Mitigação**: Validação rigorosa de MIME type, tamanho, extensão; scan de malware (futuro)

---

## Validação Final

**Critérios de Aceite (resumo)**:
- [ ] Backend: CRUD de instituições funciona
- [ ] Backend: Vinculação de usuários funciona
- [ ] Backend: Tenant context isola dados corretamente
- [ ] Backend: Documentação Swagger completa
- [ ] Frontend: Seleção de instituição funciona
- [ ] Frontend: CRUD de instituições (admin) funciona
- [ ] Frontend: Switcher de instituição funciona
- [ ] Testes: 85%+ cobertura global
- [ ] Segurança: Admin global bypassa, usuário comum isolado
- [ ] Docs: README e CONTRIBUTING atualizados

**Estimativa Total**: 50-60 horas de desenvolvimento

---

## Próximos Passos

1. **Implementação**: Entregar feature completa ao agente **coder**
2. **Code Review**: Revisar código, testes e documentação
3. **QA**: Testes manuais do fluxo completo
4. **Deploy**: Subir para ambiente de staging
5. **Feedback**: Coletar feedback de usuários piloto
6. **Melhorias**: Implementar features futuras (vínculo automático, personalização)
