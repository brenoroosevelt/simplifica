# Feature 000 - Bootstrap do Projeto

## Objetivo
Criar estrutura base do monorepo com frontend, backend, banco e tooling.

## Escopo
- Monorepo com separação clara frontend/backend/docker
- Backend: Spring Boot 3.2 + Java 21 + PostgreSQL + Flyway
- Frontend: Vite + Vue 3 + Vuetify 3 + TypeScript + Pinia
- OAuth2: Google e Microsoft (arquitetura extensível)
- Docker Compose para desenvolvimento
- Testes básicos e lint configurados
- Hot reload funcionando

## Critérios de Aceite
- [x] Frontend sobe em http://localhost:5173 com hot reload
- [x] Backend sobe em http://localhost:8080
- [x] Banco PostgreSQL conecta via Docker
- [x] Migrations Flyway executam automaticamente
- [x] Testes de exemplo passam (backend e frontend)
- [x] Landing page pública renderiza
- [x] Layout privado com sidebar offcanvas funcionando
- [x] Login OAuth Google funciona end-to-end
- [x] Login OAuth Microsoft funciona end-to-end
- [x] Arquitetura permite adicionar novos providers OAuth
- [x] Arquivos .env configurados (não commitados)
- [x] Build de produção funciona

---

## Tarefas

### FASE 1: Estrutura Base do Monorepo

#### 1.1 - Criar Estrutura de Diretórios
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Estabelecer organização do monorepo com separação clara de responsabilidades.

**Estrutura a criar**:
```
/home/breno/dev/claude-agents/
├── frontend/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/{common,auth,navigation}/
│   │   ├── composables/
│   │   ├── layouts/
│   │   ├── plugins/
│   │   ├── router/
│   │   ├── services/oauth/
│   │   ├── stores/
│   │   ├── types/
│   │   ├── views/{public,auth,private}/
│   │   └── utils/
│   ├── public/
│   └── tests/unit/
├── backend/
│   └── src/
│       ├── main/java/com/claudeagents/
│       │   ├── config/security/{jwt,oauth/providers}/
│       │   ├── domain/entity/
│       │   ├── infrastructure/repository/
│       │   ├── application/{service,dto}/
│       │   └── presentation/{controller,exception}/
│       ├── main/resources/db/migration/
│       └── test/java/com/claudeagents/{unit,integration}/
├── docker/postgres/
├── scripts/
└── shared/types/
```

**Validação**: Estrutura de pastas criada e vazia, pronta para receber código.

---

#### 1.2 - Configurar Docker Compose
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Orquestrar PostgreSQL, backend e frontend em containers Docker.

**Arquivo**: `/home/breno/dev/claude-agents/docker/docker-compose.yml`

**Requisitos**:
- Serviço **postgres** (imagem postgres:15-alpine)
  - Porta 5432 exposta
  - Variáveis de ambiente: DB_NAME, DB_USER, DB_PASSWORD
  - Volume persistente para dados
  - Healthcheck com `pg_isready`
  - Script de inicialização em `./postgres/init.sql`
- Network bridge: `claude-network`
- Volumes: `postgres-data`

**Arquivo**: `/home/breno/dev/claude-agents/docker/postgres/init.sql`
- Criar extensão `uuid-ossp`
- Configurar encoding UTF8

**Validação**: `docker-compose up postgres` sobe banco sem erros e aceita conexões.

---

#### 1.3 - Configurar Variáveis de Ambiente
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Centralizar configurações sensíveis em arquivos .env não commitados.

**Backend** (`/home/breno/dev/claude-agents/backend/.env.example`):
```env
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=claudeagents
DB_USER=admin
DB_PASSWORD=<change-me>

# OAuth2
OAUTH_GOOGLE_CLIENT_ID=<your-client-id>
OAUTH_GOOGLE_CLIENT_SECRET=<your-secret>
OAUTH_MICROSOFT_CLIENT_ID=<your-client-id>
OAUTH_MICROSOFT_CLIENT_SECRET=<your-secret>
OAUTH_REDIRECT_BASE_URL=http://localhost:8080/api

# JWT
JWT_SECRET=<generate-256-bit-key>
JWT_EXPIRATION_MS=86400000

# Spring
SPRING_PROFILES_ACTIVE=dev
```

**Frontend** (`/home/breno/dev/claude-agents/frontend/.env.example`):
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_OAUTH_GOOGLE_CLIENT_ID=<your-client-id>
VITE_OAUTH_MICROSOFT_CLIENT_ID=<your-client-id>
VITE_OAUTH_REDIRECT_URI=http://localhost:5173/auth/callback
VITE_APP_NAME=Simplifica
```

**Arquivo**: `/home/breno/dev/claude-agents/.gitignore`
- Adicionar: `.env`, `.env.local`, `node_modules/`, `target/`, `dist/`, etc.

**Validação**:
- `.env.example` commitados
- `.env` no .gitignore e não rastreados pelo git

---

### FASE 2: Backend - Setup Inicial Spring Boot

#### 2.1 - Inicializar Projeto Maven
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Criar projeto Spring Boot 3.2 com Java 21 e todas as dependências necessárias.

**Arquivo**: `/home/breno/dev/claude-agents/backend/pom.xml`

**Dependências principais**:
- Spring Boot Starters: web, data-jpa, security, oauth2-client, validation, actuator
- PostgreSQL driver
- Flyway
- JWT (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson)
- Lombok
- spring-dotenv (para carregar .env)
- Testes: spring-boot-starter-test, spring-security-test, testcontainers, h2

**Plugins**:
- spring-boot-maven-plugin
- maven-checkstyle-plugin (referenciando checkstyle.xml)
- flyway-maven-plugin

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/ClaudeAgentsApplication.java`
- Classe principal com `@SpringBootApplication` e `@EnableJpaRepositories`

**Validação**: `mvn clean compile` executa sem erros.

---

#### 2.2 - Configurar Application Properties
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Configurar Spring Boot para usar variáveis de ambiente e profiles.

**Arquivos**:

1. `/home/breno/dev/claude-agents/backend/src/main/resources/application.yml`
   - Datasource: URL com variáveis ${DB_HOST}, ${DB_PORT}, ${DB_NAME}
   - JPA: hibernate.ddl-auto=validate (Flyway controla schema)
   - Flyway: enabled=true, baseline-on-migrate=true
   - OAuth2 clients: google e microsoft com redirect-uri dinâmico
   - Server: port=8080, context-path=/api
   - JWT: secret e expiration via variáveis
   - CORS: allowed-origins do .env

2. `/home/breno/dev/claude-agents/backend/src/main/resources/application-dev.yml`
   - JPA show-sql: true
   - DevTools: restart enabled
   - Logging: DEBUG para com.claudeagents e security

3. `/home/breno/dev/claude-agents/backend/src/test/resources/application-test.yml`
   - Datasource: H2 em memória
   - Flyway: disabled
   - JWT: secret de teste

**Validação**: Aplicação carrega configurações do .env corretamente.

---

### FASE 3: Backend - Database Schema e Entidades

#### 3.1 - Criar Migration Inicial
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Definir schema inicial do banco com Flyway.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/resources/db/migration/V1__create_initial_schema.sql`

**Tabelas**:

1. **users**
   - id (UUID, PK, default uuid_generate_v4())
   - email (VARCHAR 255, NOT NULL, UNIQUE)
   - name (VARCHAR 255, NOT NULL)
   - picture_url (VARCHAR 512)
   - provider (VARCHAR 50, NOT NULL) -- 'GOOGLE', 'MICROSOFT'
   - provider_id (VARCHAR 255, NOT NULL)
   - role (VARCHAR 50, NOT NULL, DEFAULT 'USER') -- 'USER', 'ADMIN'
   - status (VARCHAR 50, NOT NULL, DEFAULT 'PENDING') -- 'PENDING', 'ACTIVE', 'INACTIVE'
   - created_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)
   - updated_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)
   - CONSTRAINT uk_provider_user UNIQUE (provider, provider_id)
   - Índices: email, (provider, provider_id), status

2. **refresh_tokens**
   - id (UUID, PK)
   - user_id (UUID, NOT NULL, FK -> users.id ON DELETE CASCADE)
   - token (VARCHAR 512, NOT NULL, UNIQUE)
   - expires_at (TIMESTAMP, NOT NULL)
   - created_at (TIMESTAMP, NOT NULL)
   - Índices: user_id, token, expires_at

**Validação**:
- `mvn flyway:migrate` executa sem erros
- Tabelas criadas no PostgreSQL

---

#### 3.2 - Criar Enums de Domínio
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Representar valores controlados do domínio como enums Java.

**Arquivos** (package `com.claudeagents.domain.entity`):

1. **OAuth2Provider.java**
   - Enum: GOOGLE, MICROSOFT
   - Método: `static OAuth2Provider fromString(String)`

2. **UserRole.java**
   - Enum: USER, ADMIN
   - Método: `static UserRole fromString(String)`

3. **UserStatus.java**
   - Enum: PENDING, ACTIVE, INACTIVE
   - Método: `static UserStatus fromString(String)`

**Validação**: Código compila, enums funcionam corretamente.

---

#### 3.3 - Criar Entidades JPA
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Mapear tabelas do banco para objetos Java com JPA.

**Arquivos** (package `com.claudeagents.domain.entity`):

1. **User.java**
   - Anotações: `@Entity`, `@Table(name = "users")`
   - Campos conforme tabela users
   - Lombok: `@Getter`, `@Setter`, `@Builder`, etc.
   - Métodos: `@PrePersist`, `@PreUpdate` para timestamps
   - Helpers: `isPending()`, `isActive()`, `isAdmin()`

2. **RefreshToken.java**
   - Anotações: `@Entity`, `@Table(name = "refresh_tokens")`
   - Relacionamento: `@ManyToOne` com User (LAZY)
   - Método: `isExpired()` para validar expiração

**Validação**: Entidades mapeadas corretamente, aplicação inicia sem erros de JPA.

---

#### 3.4 - Criar Repositories JPA
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Interfaces para acesso ao banco de dados.

**Arquivos** (package `com.claudeagents.infrastructure.repository`):

1. **UserRepository.java** extends `JpaRepository<User, UUID>`
   - Métodos:
     - `Optional<User> findByEmail(String email)`
     - `Optional<User> findByProviderAndProviderId(OAuth2Provider, String)`
     - `boolean existsByEmail(String email)`

2. **RefreshTokenRepository.java** extends `JpaRepository<RefreshToken, UUID>`
   - Métodos:
     - `Optional<RefreshToken> findByToken(String token)`
     - `void deleteByUser(User user)`
     - `void deleteByExpiresAtBefore(LocalDateTime now)`

**Validação**: Repositories funcionam, queries personalizadas executam corretamente.

---

### FASE 4: Backend - Spring Security e JWT

#### 4.1 - Implementar JWT Token Provider
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Componente responsável por gerar e validar tokens JWT.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/config/security/jwt/JwtTokenProvider.java`

**Responsabilidades**:
- Gerar token a partir de `Authentication` (após OAuth2 success)
- Gerar token a partir de `userId` (para refresh)
- Validar token (verificar assinatura, expiração)
- Extrair `userId` do token
- Usar secret key de 256 bits do .env
- Claims: subject=userId, email, name, issuedAt, expiration

**Assinaturas principais**:
```java
public String generateToken(Authentication authentication)
public String generateTokenFromUserId(UUID userId)
public UUID getUserIdFromToken(String token)
public boolean validateToken(String token)
```

**Validação**: Tokens gerados são válidos e podem ser decodificados.

---

#### 4.2 - Criar JWT Authentication Filter
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Interceptar requests HTTP e validar JWT no header Authorization.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/config/security/jwt/JwtAuthenticationFilter.java`

**Responsabilidades**:
- Extends `OncePerRequestFilter`
- Extrair token do header `Authorization: Bearer <token>`
- Validar token usando `JwtTokenProvider`
- Extrair userId, buscar User no banco
- Criar `UserPrincipal` e setar no SecurityContext
- Continuar filter chain

**Validação**: Requests com token válido são autenticados, requests sem token ou token inválido são rejeitados.

---

#### 4.3 - Criar UserPrincipal
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Adaptador entre User do domínio e UserDetails do Spring Security.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/config/security/UserPrincipal.java`

**Responsabilidades**:
- Implements `UserDetails` e `OAuth2User`
- Campos: UUID id, String email, String name, String pictureUrl, authorities
- Factory: `static UserPrincipal create(User user)` e `create(User, Map<> attributes)`
- Métodos de UserDetails: getAuthorities(), getUsername() (retorna email), isEnabled(), etc.

**Validação**: UserPrincipal criado a partir de User contém informações corretas.

---

#### 4.4 - Implementar OAuth2 Strategy Pattern
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Abstrair extração de dados de diferentes providers OAuth2 (extensível).

**Arquivos** (package `com.claudeagents.config.security.oauth`):

1. **OAuth2UserInfo.java** (classe abstrata)
   - Campo: `Map<String, Object> attributes`
   - Métodos abstratos: `getId()`, `getName()`, `getEmail()`, `getImageUrl()`

2. **providers/GoogleOAuth2UserInfo.java** extends OAuth2UserInfo
   - Implementa métodos para extrair dados do Google (sub, name, email, picture)

3. **providers/MicrosoftOAuth2UserInfo.java** extends OAuth2UserInfo
   - Implementa métodos para extrair dados da Microsoft (id, displayName, userPrincipalName)

4. **OAuth2UserInfoFactory.java**
   - Método estático: `static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<> attributes)`
   - Usa switch/case para instanciar provider correto
   - Facilita adição de novos providers

**Validação**: Factory retorna provider correto baseado no registrationId.

---

#### 4.5 - Criar CustomOAuth2UserService
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Processar autenticação OAuth2 e criar/atualizar usuário no banco.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/application/service/CustomOAuth2UserService.java`

**Responsabilidades**:
- Extends `DefaultOAuth2UserService`
- Override `loadUser(OAuth2UserRequest)`
- Extrair `OAuth2UserInfo` usando factory
- Buscar usuário por provider+providerId
- Se não existe: criar novo User com status PENDING
- Se existe: atualizar nome e foto
- Retornar `UserPrincipal` com dados do usuário

**Fluxo**:
```
OAuth2UserRequest → OAuth2User → OAuth2UserInfo → User (DB) → UserPrincipal
```

**Validação**: Primeiro login cria usuário, logins subsequentes atualizam dados.

---

#### 4.6 - Criar OAuth2 Success/Failure Handlers
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Gerar JWT após autenticação bem-sucedida e redirecionar para frontend.

**Arquivos** (package `com.claudeagents.config.security.oauth`):

1. **OAuth2AuthenticationSuccessHandler.java**
   - Extends `SimpleUrlAuthenticationSuccessHandler`
   - Gera JWT usando `JwtTokenProvider`
   - Redireciona para frontend com token como query param
   - URL: `http://localhost:5173/auth/callback?token=<jwt>`

2. **OAuth2AuthenticationFailureHandler.java**
   - Extends `SimpleUrlAuthenticationFailureHandler`
   - Redireciona para frontend com erro: `?error=<message>`

**Validação**: Após OAuth2 success, usuário é redirecionado com token válido.

---

#### 4.7 - Configurar Spring Security
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Configuração central de segurança integrando JWT e OAuth2.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/config/SecurityConfig.java`

**Configurações**:
- CORS: allowed origins do .env
- CSRF: desabilitado (API REST stateless)
- Session: STATELESS
- Endpoints públicos: `/public/**`, `/oauth2/**`, `/actuator/health`
- Endpoints protegidos: `/auth/**`, `/user/**` (authenticated)
- Endpoints admin: `/admin/**` (ROLE_ADMIN)
- OAuth2 login:
  - userInfoEndpoint: `customOAuth2UserService`
  - successHandler: `oAuth2AuthenticationSuccessHandler`
  - failureHandler: `oAuth2AuthenticationFailureHandler`
- Filtro JWT: antes de `UsernamePasswordAuthenticationFilter`

**Validação**:
- Endpoints públicos acessíveis sem autenticação
- Endpoints protegidos exigem JWT válido
- OAuth2 flow funciona end-to-end

---

### FASE 5: Backend - Services, DTOs e Controllers

#### 5.1 - Criar UserService
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Lógica de negócio para operações com usuários.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/main/java/com/claudeagents/application/service/UserService.java`

**Métodos**:
- `User findById(UUID id)` - busca por id, lança exceção se não existe
- `User findByEmail(String email)` - busca por email
- (Futuro: update, activate, deactivate)

**Validação**: Service busca usuários corretamente, exceções apropriadas para não encontrados.

---

#### 5.2 - Criar DTOs
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Objetos de transferência para API REST.

**Arquivos** (package `com.claudeagents.application.dto`):

1. **UserDTO.java**
   - Campos: id, email, name, pictureUrl, provider, role, status, createdAt
   - Método: `static UserDTO fromEntity(User user)`

2. **AuthResponseDTO.java**
   - Campos: accessToken, tokenType, user (UserDTO)

**Validação**: DTOs mapeiam corretamente de/para entidades.

---

#### 5.3 - Criar Controllers REST
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Endpoints da API REST.

**Arquivos** (package `com.claudeagents.presentation.controller`):

1. **HealthController.java** (`/public`)
   - `GET /public/health` → status, timestamp, service name

2. **AuthController.java** (`/auth`)
   - `GET /auth/me` → UserDTO do usuário autenticado (via @AuthenticationPrincipal)

3. **UserController.java** (`/user`)
   - `GET /user/profile` → UserDTO completo do usuário

**Validação**:
- `/public/health` retorna 200 sem autenticação
- `/auth/me` exige token JWT válido
- `/user/profile` retorna dados do usuário autenticado

---

#### 5.4 - Criar Global Exception Handler
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Tratamento centralizado de exceções com respostas padronizadas.

**Arquivos** (package `com.claudeagents.presentation.exception`):

1. **ErrorResponse.java**
   - Campos: timestamp, status, error, message, path

2. **GlobalExceptionHandler.java** (`@RestControllerAdvice`)
   - Handler para `OAuth2AuthenticationException` → 401
   - Handler para `AccessDeniedException` → 403
   - Handler para `RuntimeException` → 500
   - Logs apropriados

**Validação**: Exceções retornam JSON padronizado com status HTTP correto.

---

### FASE 6: Backend - Qualidade e Testes

#### 6.1 - Configurar Checkstyle
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Garantir padrões de código consistentes.

**Arquivo**: `/home/breno/dev/claude-agents/backend/checkstyle.xml`

**Regras principais**:
- Line length: 120 caracteres
- Indentação: 4 espaços
- Naming conventions (Java standard)
- Import organization
- Whitespace rules

**Validação**: `mvn checkstyle:check` passa sem violações.

---

#### 6.2 - Criar Testes Unitários
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Testar componentes isoladamente com mocks.

**Arquivos** (package `com.claudeagents.unit`):

1. **security/JwtTokenProviderTest.java**
   - Testar geração de token
   - Testar validação de token válido/inválido
   - Testar extração de userId

2. **service/UserServiceTest.java**
   - Mock UserRepository
   - Testar findById (sucesso e exceção)
   - Testar findByEmail

**Tecnologias**: JUnit 5, Mockito, AssertJ

**Validação**: `mvn test` executa e passa 100%.

---

#### 6.3 - Criar Teste de Integração
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Testar fluxo completo com banco de dados.

**Arquivo**: `/home/breno/dev/claude-agents/backend/src/test/java/com/claudeagents/integration/HealthCheckIT.java`

**Setup**:
- `@SpringBootTest` com `webEnvironment = RANDOM_PORT`
- Usar H2 ou Testcontainers (PostgreSQL)
- `@ActiveProfiles("test")`

**Testes**:
- GET /api/public/health retorna 200 e JSON correto

**Validação**: `mvn verify` executa testes de integração com sucesso.

---

### FASE 7: Backend - Docker

#### 7.1 - Criar Dockerfile do Backend
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Containerizar aplicação Spring Boot.

**Arquivo**: `/home/breno/dev/claude-agents/backend/Dockerfile`

**Estratégia**: Multi-stage build
- Stage 1 (build): Maven + JDK 21 → compila e gera JAR
- Stage 2 (runtime): JRE 21 Alpine → roda aplicação
- Criar usuário não-root (appuser)
- Healthcheck: wget em `/api/public/health`
- Expor porta 8080

**Validação**: `docker build -t claude-agents-backend .` constrói imagem sem erros.

---

#### 7.2 - Integrar Backend no Docker Compose
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Adicionar serviço backend no docker-compose.yml.

**Arquivo**: `/home/breno/dev/claude-agents/docker/docker-compose.yml`

**Serviço backend**:
- Build: context=../backend
- Porta: 8080:8080
- env_file: ../backend/.env
- Environment: DB_HOST=postgres (override para Docker network)
- depends_on: postgres (com condition: service_healthy)
- network: claude-network
- Healthcheck: wget health endpoint

**Validação**:
- `docker-compose up -d backend` sobe aplicação
- Backend conecta no PostgreSQL
- Migrations executam automaticamente
- Health endpoint retorna 200

---

### FASE 8: Frontend - Setup Inicial

#### 8.1 - Inicializar Projeto Vite + Vue 3
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Criar projeto frontend com stack moderna.

**Comandos**:
```bash
cd /home/breno/dev/claude-agents/frontend
npm create vite@latest . -- --template vue-ts
npm install
```

**Validação**: `npm run dev` inicia servidor de desenvolvimento.

---

#### 8.2 - Instalar Dependências
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Adicionar bibliotecas necessárias ao projeto.

**Dependências**:
- UI: `vuetify@^3.5`, `@mdi/font`
- State: `pinia`
- Router: `vue-router@4`
- HTTP: `axios`
- OAuth: `vue3-google-login`, `@azure/msal-browser`
- Dev: `sass`, `typescript`, `@types/node`
- Lint: `eslint`, `@typescript-eslint/*`, `eslint-plugin-vue`, `prettier`
- Test: `vitest`, `@vue/test-utils`, `jsdom`, `@vitest/ui`

**Validação**: Todas as dependências instaladas, sem conflitos.

---

#### 8.3 - Configurar Vite com Hot Reload
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Configurar Vite para desenvolvimento com hot reload no Docker.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/vite.config.ts`

**Configurações essenciais**:
- Plugin Vue
- Plugin Vuetify (com autoImport)
- Alias: `@` para `./src`
- Server:
  - host: `0.0.0.0` (acessível de fora do container)
  - port: 5173
  - **watch.usePolling: true** (CRÍTICO para hot reload no Docker)
  - hmr.clientPort: 5173

**Validação**: Hot reload funciona ao editar arquivos .vue dentro do Docker.

---

#### 8.4 - Configurar TypeScript
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Configurar TypeScript com strict mode e path mapping.

**Arquivos**:

1. `/home/breno/dev/claude-agents/frontend/tsconfig.json`
   - target: ES2020
   - strict: true
   - paths: `@/*` → `./src/*`
   - include: src/**/*.vue, src/**/*.ts

2. `/home/breno/dev/claude-agents/frontend/tsconfig.node.json`
   - Para arquivos de config (vite.config.ts)

3. `/home/breno/dev/claude-agents/frontend/src/env.d.ts`
   - Interface ImportMetaEnv com todas as variáveis VITE_*

**Validação**: TypeScript compila sem erros, autocomplete funciona na IDE.

---

#### 8.5 - Configurar ESLint e Prettier
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Enforçar estilo de código consistente.

**Arquivos**:

1. `/home/breno/dev/claude-agents/frontend/.eslintrc.cjs`
   - Extends: vue3-recommended, typescript-recommended, prettier
   - Rules: vue/multi-word-component-names off, no-explicit-any warn

2. `/home/breno/dev/claude-agents/frontend/.prettierrc.json`
   - semi: false
   - singleQuote: true
   - printWidth: 100

**Scripts no package.json**:
- `"lint": "eslint src --ext .vue,.ts"`
- `"format": "prettier --write src"`

**Validação**: `npm run lint` passa sem erros.

---

#### 8.6 - Configurar Vitest
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Setup de testes unitários para Vue components.

**Arquivos**:

1. `/home/breno/dev/claude-agents/frontend/vitest.config.ts`
   - environment: jsdom
   - globals: true
   - setupFiles: ./tests/setup.ts
   - coverage: v8 provider

2. `/home/breno/dev/claude-agents/frontend/tests/setup.ts`
   - Mock CSS.supports
   - Configurações globais do Vue Test Utils

**Scripts**: `"test": "vitest"`, `"test:ui": "vitest --ui"`

**Validação**: `npm test` executa sem erros (mesmo sem testes ainda).

---

### FASE 9: Frontend - Plugins e Configurações Base

#### 9.1 - Configurar Vuetify
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Setup do framework UI Vuetify 3.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/plugins/vuetify.ts`

**Configurações**:
- Tema: cores primárias/secundárias customizadas
- Icons: Material Design Icons (@mdi/font)
- Display: breakpoints mobile/tablet/desktop
- Preparar para light/dark mode (futuro)

**Integração**: Importar e usar plugin no `main.ts`

**Validação**: Componentes Vuetify (v-btn, v-card) renderizam corretamente.

---

#### 9.2 - Configurar Vue Router
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Sistema de rotas com layouts públicos/privados.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/router/index.ts`

**Rotas iniciais**:
- `/` → LandingPage (public layout)
- `/login` → LoginPage (public layout)
- `/auth/callback` → OAuthCallbackPage (sem layout)
- `/dashboard` → DashboardPage (private layout, requiresAuth: true)
- `/profile` → ProfilePage (private layout, requiresAuth: true)

**Meta fields**: `requiresAuth`, `layout`

**Validação**: Navegação entre rotas funciona, layouts aplicados.

---

#### 9.3 - Criar Navigation Guards
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Proteger rotas privadas com autenticação.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/router/guards.ts`

**Lógica**:
- Antes de cada rota: verificar `requiresAuth` na meta
- Se requer auth e usuário não autenticado: redirect para `/login`
- Se usuário autenticado tenta acessar `/login`: redirect para `/dashboard`
- Validar token JWT no localStorage

**Validação**: Rotas privadas inacessíveis sem autenticação.

---

#### 9.4 - Configurar Pinia
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Setup do state management.

**Arquivo**: Plugin padrão do Pinia criado e registrado no `main.ts`

**Stores a criar** (próxima fase):
- auth.store.ts
- ui.store.ts

**Validação**: Pinia funciona, stores podem ser criadas.

---

### FASE 10: Frontend - Sistema de Autenticação

#### 10.1 - Criar Types de Autenticação
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Definir interfaces TypeScript para autenticação.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/types/auth.types.ts`

**Interfaces**:
```typescript
enum OAuthProvider { GOOGLE, MICROSOFT }

interface User {
  id: string
  email: string
  name: string
  avatar?: string
  role: 'USER' | 'ADMIN'
  status: 'PENDING' | 'ACTIVE'
}

interface AuthState {
  isAuthenticated: boolean
  user: User | null
  token: string | null
}
```

**Validação**: Types importáveis e usáveis em outros arquivos.

---

#### 10.2 - Criar Provider OAuth Abstrato
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Arquitetura extensível para múltiplos providers OAuth.

**Arquivos** (em `src/services/oauth/`):

1. **base.provider.ts**
   - Interface `IOAuthProvider`:
     - `login(): Promise<void>`
     - `logout(): void`
     - `handleCallback(params): Promise<AuthResponse>`

2. **google.provider.ts**
   - Implementa IOAuthProvider
   - Usa `vue3-google-login`
   - Configuração com clientId do .env

3. **microsoft.provider.ts**
   - Implementa IOAuthProvider
   - Usa `@azure/msal-browser`
   - Configuração com clientId do .env

**Validação**: Providers instanciáveis e métodos executam sem erro.

---

#### 10.3 - Criar Auth Store
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Gerenciar estado global de autenticação.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/stores/auth.store.ts`

**Estado**:
- isAuthenticated: boolean
- user: User | null
- token: string | null

**Actions**:
- `loginWithProvider(provider: OAuthProvider)` - inicia flow OAuth
- `handleCallback(token: string)` - processa token do backend
- `logout()` - limpa estado e localStorage
- `checkAuth()` - valida token ao carregar app
- `fetchUser()` - busca dados do usuário no backend

**Getters**:
- isAuthenticated
- currentUser
- isPending (user.status === 'PENDING')

**Persistência**: Salvar token no localStorage

**Validação**: Store gerencia login/logout corretamente.

---

#### 10.4 - Criar Composable useAuth
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: API reativa para autenticação em components.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/composables/useAuth.ts`

**Exports**:
- `login(provider)` - wrapper para store action
- `logout()` - wrapper para store action
- `isAuthenticated` - computed do store
- `user` - computed do store

**Validação**: Composable utilizável em qualquer componente Vue.

---

#### 10.5 - Criar Axios Instance
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Cliente HTTP centralizado com interceptors.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/services/api.ts`

**Configuração**:
- baseURL: `import.meta.env.VITE_API_BASE_URL`
- timeout: 30000

**Interceptors**:
- Request: adicionar `Authorization: Bearer <token>` do localStorage
- Response:
  - 401 → logout automático
  - Erros genéricos → tratamento padronizado

**Validação**: Requests incluem token automaticamente quando disponível.

---

#### 10.6 - Criar Auth Service
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Comunicação com backend para autenticação.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/services/auth.service.ts`

**Métodos**:
- `getMe(): Promise<User>` - GET /auth/me
- `validateToken(token): Promise<boolean>`

**Validação**: Métodos retornam dados do backend corretamente.

---

### FASE 11: Frontend - Layouts e Componentes Base

#### 11.1 - Criar Public Layout
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Layout para páginas públicas (landing, login).

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/layouts/PublicLayout.vue`

**Estrutura**:
- Header simples: logo + botão "Entrar"
- `<router-view />` para conteúdo
- Footer com links úteis
- Design minimalista

**Validação**: Landing page renderiza dentro do layout.

---

#### 11.2 - Criar Private Layout
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Layout principal da aplicação autenticada.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/layouts/PrivateLayout.vue`

**Componentes Vuetify**:
- `<v-app>`
- `<v-app-bar>`: header com toggle drawer, logo, user menu
- `<v-navigation-drawer>`:
  - Modo: rail no desktop, temporary no mobile
  - Comportamento: push content (não overlay no desktop)
  - Conteúdo: lista de navegação, logout
- `<v-main>`: conteúdo principal

**Responsividade**:
- Mobile (< 1024px): drawer temporary (sobrepõe)
- Desktop (>= 1024px): drawer permanent rail ou expanded

**Validação**: Layout responsivo, drawer funciona em mobile e desktop.

---

#### 11.3 - Criar App Sidebar
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Menu de navegação lateral.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/components/navigation/AppSidebar.vue`

**Conteúdo**:
- `<v-list>` com itens de navegação:
  - Dashboard (icon: mdi-view-dashboard)
  - Perfil (icon: mdi-account)
  - Configurações (icon: mdi-cog) - futuro
- Seção admin (visível apenas se user.role === 'ADMIN')
- Footer: versão da app

**Validação**: Navegação funciona, active state visual correto.

---

#### 11.4 - Criar App Header
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Barra superior da aplicação.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/components/navigation/AppHeader.vue`

**Conteúdo**:
- Botão toggle drawer (mobile)
- Logo/título
- Spacer
- Ícone notificações (futuro)
- User profile menu

**Validação**: Header renderiza corretamente, botões funcionam.

---

#### 11.5 - Criar User Profile Menu
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Dropdown com informações e ações do usuário.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/components/navigation/UserProfile.vue`

**Conteúdo**:
- Avatar (fallback para iniciais)
- Nome e email
- `<v-menu>` com:
  - Link para perfil
  - Link para configurações
  - Divider
  - Botão logout (chama `useAuth().logout()`)

**Validação**: Menu abre/fecha, logout funciona.

---

#### 11.6 - Criar UI Store
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Gerenciar estado da interface (drawer, mobile, etc).

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/stores/ui.store.ts`

**Estado**:
- drawerOpen: boolean
- isMobile: boolean

**Actions**:
- toggleDrawer()
- setMobile(value)

**Validação**: Store controla abertura/fechamento do drawer.

---

#### 11.7 - Criar Composable useDrawer
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 20min

**Objetivo**: Facilitar controle do drawer em componentes.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/composables/useDrawer.ts`

**Exports**:
- drawerOpen (computed)
- toggleDrawer (function)
- isMobile (computed)

**Validação**: Composable funciona em qualquer componente.

---

### FASE 12: Frontend - Views/Páginas

#### 12.1 - Criar Landing Page
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Página inicial pública da aplicação.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/views/public/LandingPage.vue`

**Seções**:
- Hero: título, subtítulo, CTA "Começar agora"
- Features: 3-4 cards com benefícios
- Call-to-action final: botão "Entrar"

**Design**: Moderno, clean, responsivo

**Validação**: Página renderiza, navegação para login funciona.

---

#### 12.2 - Criar Login Page
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Página de autenticação OAuth.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/views/public/LoginPage.vue`

**Conteúdo**:
- Card centralizado
- Logo e título
- Botão "Entrar com Google" (GoogleLoginButton)
- Botão "Entrar com Microsoft" (MicrosoftLoginButton)
- Loading state durante autenticação
- Mensagens de erro

**Validação**: Botões OAuth redirecionam corretamente.

---

#### 12.3 - Criar Botões de Login OAuth
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Componentes para iniciar autenticação OAuth.

**Arquivos**:

1. **GoogleLoginButton.vue**
   - Logo do Google
   - Texto "Continuar com Google"
   - Click: chama `useAuth().login('google')`
   - Loading state

2. **MicrosoftLoginButton.vue**
   - Logo da Microsoft
   - Texto "Continuar com Microsoft"
   - Click: chama `useAuth().login('microsoft')`
   - Loading state

**Validação**: Botões iniciam flow OAuth corretamente.

---

#### 12.4 - Criar OAuth Callback Page
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Processar retorno do OAuth e salvar token.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/views/auth/OAuthCallbackPage.vue`

**Lógica**:
- Extrair token da query string (`?token=...`)
- Chamar `useAuth().handleCallback(token)`
- Buscar dados do usuário
- Redirect para `/dashboard`
- Mostrar loading enquanto processa
- Error handling: redirect para `/login` com mensagem

**Validação**: Token processado, usuário redirecionado para dashboard.

---

#### 12.5 - Criar Dashboard Page
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Página inicial do usuário autenticado.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/views/private/DashboardPage.vue`

**Conteúdo**:
- Mensagem de boas-vindas com nome do usuário
- Alerta para usuários PENDING: "Sua conta está pendente de aprovação"
- Cards com estatísticas placeholder (futuro)
- Grid responsivo

**Validação**: Dashboard renderiza, dados do usuário exibidos.

---

#### 12.6 - Criar Profile Page
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Página de perfil do usuário.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/src/views/private/ProfilePage.vue`

**Conteúdo**:
- Avatar grande
- Informações: nome, email, provider, role, status
- Botão "Editar" (funcionalidade futura)

**Validação**: Profile exibe dados corretos do usuário.

---

### FASE 13: Frontend - Docker e Integração

#### 13.1 - Criar Dockerfile do Frontend
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Containerizar frontend para desenvolvimento.

**Arquivo**: `/home/breno/dev/claude-agents/frontend/Dockerfile`

**Estratégia**:
- Imagem: node:20-alpine
- Copiar package.json, npm install
- Copiar código fonte
- Expor porta 5173
- CMD: `npm run dev`

**Importante**: Volumes no docker-compose para hot reload

**Validação**: Container inicia, frontend acessível em localhost:5173.

---

#### 13.2 - Integrar Frontend no Docker Compose
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Adicionar serviço frontend no docker-compose.yml.

**Arquivo**: `/home/breno/dev/claude-agents/docker/docker-compose.yml`

**Serviço frontend**:
- Build: context=../frontend
- Porta: 5173:5173
- env_file: ../frontend/.env
- Volumes:
  - `./frontend:/app` (código fonte)
  - `/app/node_modules` (evitar sobrescrever)
- depends_on: backend
- network: claude-network

**Validação**:
- `docker-compose up -d frontend` sobe aplicação
- Hot reload funciona ao editar arquivos
- Frontend comunica com backend

---

#### 13.3 - Criar Testes Unitários Frontend
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Testes básicos de componentes e composables.

**Arquivos**:

1. **tests/unit/composables/useAuth.spec.ts**
   - Mock auth store
   - Testar login, logout
   - Testar computed (isAuthenticated, user)

2. **tests/unit/components/GoogleLoginButton.spec.ts**
   - Renderiza corretamente
   - Click emite evento correto
   - Loading state funciona

**Validação**: `npm test` executa e passa.

---

### FASE 14: Scripts de Automação e Documentação

#### 14.1 - Criar Scripts de Setup
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Facilitar setup inicial do ambiente.

**Arquivos** (em `/home/breno/dev/claude-agents/scripts/`):

1. **setup-dev.sh**
   - Verificar pré-requisitos (Docker, Node)
   - Copiar .env.example → .env (se não existe)
   - Build dos containers
   - Instruções para configurar OAuth

2. **start-dev.sh**
   - `docker-compose up -d`
   - Exibir URLs (frontend, backend, banco)

3. **stop-dev.sh**
   - `docker-compose down`

4. **reset-db.sh**
   - Parar containers
   - Remover volume do postgres
   - Recriar e subir

**Validação**: Scripts executam sem erros, ambiente funciona.

---

#### 14.2 - Criar Documentação
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Documentar projeto para desenvolvedores.

**Arquivos**:

1. `/home/breno/dev/claude-agents/README.md`
   - Descrição do projeto
   - Tech stack
   - Pré-requisitos
   - Setup inicial (rodar scripts)
   - Como obter credentials OAuth (Google, Microsoft)
   - Estrutura do projeto
   - Comandos úteis
   - Troubleshooting

2. `/home/breno/dev/claude-agents/CONTRIBUTING.md`
   - Como contribuir
   - Padrões de código
   - Fluxo de Git
   - Como reportar bugs

**Validação**: Desenvolvedor consegue configurar projeto seguindo README.

---

### FASE 15: Validação Final e Ajustes

#### 15.1 - Validar Critérios de Aceite
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Garantir que todos os critérios da feature foram atendidos.

**Checklist**:
- [ ] Frontend sobe em http://localhost:5173
- [ ] Hot reload funciona (editar .vue reflete mudança)
- [ ] Backend sobe em http://localhost:8080
- [ ] GET /api/public/health retorna 200
- [ ] Banco PostgreSQL conecta
- [ ] Migrations executam ao subir backend
- [ ] Landing page renderiza corretamente
- [ ] Botão "Entrar" redireciona para /login
- [ ] Página de login mostra botões OAuth
- [ ] Click em "Google" inicia flow OAuth (pode falhar sem credentials)
- [ ] Click em "Microsoft" inicia flow OAuth
- [ ] Callback processa token e redireciona para dashboard
- [ ] Dashboard exibe nome do usuário
- [ ] Layout privado com sidebar funciona
- [ ] Sidebar mobile é temporary, desktop é rail/permanent
- [ ] User menu funciona (avatar, dropdown)
- [ ] Logout funciona (limpa token, redireciona)
- [ ] Alerta exibido para usuários PENDING
- [ ] Testes backend passam: `mvn test`
- [ ] Testes frontend passam: `npm test`
- [ ] Lint backend passa: `mvn checkstyle:check`
- [ ] Lint frontend passa: `npm run lint`
- [ ] Build de produção: `docker-compose -f docker-compose.prod.yml build`
- [ ] Arquivos .env não estão commitados
- [ ] Arquivos .env.example estão atualizados

**Validação**: Todos os itens marcados, feature completa.

---

#### 15.2 - Criar docker-compose.prod.yml (Opcional)
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Configuração para build de produção.

**Arquivo**: `/home/breno/dev/claude-agents/docker/docker-compose.prod.yml`

**Diferenças do dev**:
- Frontend: build estático, servir com nginx
- Backend: JAR otimizado, sem devtools
- Variáveis de ambiente de produção

**Validação**: Build de produção funciona.

---

#### 15.3 - Ajustes Finais e Polimento
**Status**: [x] Concluído
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Refinar UX e corrigir pequenos bugs.

**Tarefas**:
- Adicionar loading spinners onde apropriado
- Melhorar mensagens de erro
- Adicionar transições entre rotas
- Ajustar espaçamentos e responsividade
- Verificar acessibilidade básica
- Testar em diferentes navegadores

**Validação**: Aplicação funciona suavemente, boa experiência de usuário.

---

## Resumo Final

**Total de Tarefas**: 75 tarefas organizadas em 15 fases
**Estimativa Total**: 60-80 horas de desenvolvimento
**Arquivos Principais**:
- Backend: ~30 arquivos Java + configs
- Frontend: ~25 arquivos Vue/TS + configs
- Infra: 5 arquivos Docker/scripts

**Dependências entre Fases**:
1. FASE 1 (estrutura) → FASE 2-7 (backend) e FASE 8-13 (frontend) podem rodar em paralelo
2. FASE 13 (integração) depende de backend e frontend funcionando
3. FASE 14-15 (documentação e validação) são finais

**Próximos Passos**:
1. Entregar ao agente **coder** para implementação sequencial
2. Validar cada fase antes de avançar
3. Ajustar plano conforme necessário durante implementação
