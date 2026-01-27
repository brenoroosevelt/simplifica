# Feature 002 - Vincular Usuário a Instituição - Plano de Implementação

## Análise de Gaps

### O que JÁ está implementado ✅

**Backend:**
- ✅ Models completos: User, Institution, UserInstitution com roles
- ✅ UserController com endpoints básicos (/profile, /institutions, /institutions/{id}/validate)
- ✅ InstitutionController com CRUD completo de instituições
- ✅ OAuth2 com Google/Microsoft funcionando
- ✅ Multi-tenant schema completo no banco (user_institutions, user_institution_roles)
- ✅ Security: JWT, UserPrincipal, TenantContext, TenantInterceptor
- ✅ Soft delete pattern implementado

**Frontend:**
- ✅ UsersPage.vue (estrutura básica, em desenvolvimento)
- ✅ UserList.vue (componente completo com filtros, paginação, ordenação)
- ✅ Componentes de instituição (switcher, selector, form, list)
- ✅ Auth store e Institution store (Pinia)
- ✅ Router guards básicos (requiresAuth, requiresInstitution, requiresAdmin)
- ✅ InstitutionSwitcher no header

**Banco de dados:**
- ✅ Tables: users, institutions, user_institutions, user_institution_roles
- ✅ Enums: UserRole (USER, ADMIN), InstitutionRole (ADMIN, MANAGER, VIEWER), UserStatus (PENDING, ACTIVE, INACTIVE)
- ✅ Constraints e índices otimizados

---

### O que FALTA implementar ⚠️

#### Backend

**1. AdminController - Gerenciamento de usuários**
   - [ ] GET /admin/users - Listar usuários com filtros
     - Parâmetros: search (nome/email), status, institutionId, role, page, size, sort
     - Permissão: ADMIN (todos usuários) ou GESTOR (apenas sua instituição)
   - [ ] GET /admin/users/{id} - Obter usuário específico
   - [ ] PUT /admin/users/{id} - Atualizar usuário (nome, status)
     - Permissão: ADMIN ou GESTOR (apenas sua instituição)
   - [ ] PUT /admin/users/{id}/roles - Alterar papéis em instituição
     - Permissão: ADMIN ou GESTOR (apenas sua instituição)
   - [ ] POST /admin/users/{id}/institutions - Vincular usuário a instituição
     - Permissão: apenas ADMIN
   - [ ] DELETE /admin/users/{id}/institutions/{institutionId} - Desvincular
     - Permissão: apenas ADMIN

**2. UserAdminService - Lógica de negócio**
   - [ ] Método listUsers com filtros e paginação
   - [ ] Método updateUser
   - [ ] Método updateUserRoles
   - [ ] Método linkUserToInstitution
   - [ ] Método unlinkUserFromInstitution
   - [ ] Validações de permissão (ADMIN vs GESTOR)

**3. DTOs necessários**
   - [ ] UserListDTO (lista simplificada)
   - [ ] UserDetailDTO (detalhes completos com instituições e roles)
   - [ ] UpdateUserRequest
   - [ ] UpdateUserRolesRequest
   - [ ] LinkUserInstitutionRequest
   - [ ] PagedResponseDTO<T> (genérico para paginação)

**4. OAuth - Vínculo automático por domínio**
   - [ ] Modificar CustomOAuth2UserService:
     - Extrair domínio do email
     - Buscar instituição por domínio
     - Se encontrar: criar vínculo automático com role VIEWER
     - Se encontrar: mudar status para ACTIVE
     - Se não encontrar: manter status PENDING

**5. Middleware - Validação de status PENDING**
   - [ ] PendingUserInterceptor ou Filter
     - Bloquear todos os requests de usuários PENDING
     - Exceto: /user/profile, /auth/*, /public/*
     - Retornar 403 com mensagem específica

**6. Migration - Instituição padrão**
   - [ ] V4__insert_default_admin_institution.sql
     - Inserir "Administração Simplifica" (tipo PRIVADA, domínio simplifica.ai)

#### Frontend

**1. UserService - API client**
   - [ ] listUsers(filters, pagination)
   - [ ] getUserById(id)
   - [ ] updateUser(id, data)
   - [ ] updateUserRoles(id, institutionId, roles)
   - [ ] linkUserToInstitution(userId, institutionId, roles)
   - [ ] unlinkUserFromInstitution(userId, institutionId)

**2. UsersPage.vue - Implementação completa**
   - [ ] Conectar com UserService
   - [ ] Buscar usuários com paginação server-side
   - [ ] Implementar filtros (search, status, role, institution)
   - [ ] Filtro de instituição: apenas para ADMIN (GESTOR não vê)
   - [ ] Dialog de edição de usuário
   - [ ] Dialog de gerenciamento de papéis
   - [ ] Dialog de gerenciamento de instituições (apenas ADMIN)
   - [ ] Snackbar com feedback de operações

**3. Componentes de usuário**
   - [ ] UserEditDialog.vue
     - Campos: nome, status (PENDING/ACTIVE/INACTIVE)
     - Validações inline
   - [ ] UserRolesDialog.vue
     - Selecionar instituição
     - Checkboxes para roles (ADMIN, MANAGER, VIEWER)
     - Permissão: ADMIN ou GESTOR (apenas sua instituição)
   - [ ] UserInstitutionsDialog.vue
     - Listar instituições vinculadas
     - Adicionar nova instituição (apenas ADMIN)
     - Remover instituição (apenas ADMIN)
     - Mostrar roles por instituição

**4. ProfilePage.vue - Melhorias**
   - [ ] Seção "Minhas Instituições"
     - Listar instituições vinculadas
     - Mostrar roles de cada instituição
     - Botão para alternar instituição (usar InstitutionSwitcher)
   - [ ] Seção "Dados Pessoais"
     - Nome, email, foto (somente leitura, vem do OAuth)
   - [ ] Seção "Segurança"
     - Provider usado
     - Data de criação da conta

**5. PendingUserAlert.vue - Alerta global**
   - [ ] Componente de alerta persistente
   - [ ] Exibir em todas as páginas se status = PENDING
   - [ ] Mensagem: "Sua conta está pendente de aprovação. Aguarde a vinculação a uma instituição."
   - [ ] Cor: warning (amarelo)
   - [ ] Não pode ser fechado

**6. Router guards - Melhorias**
   - [ ] Adicionar validação de status PENDING
   - [ ] Se PENDING: redirecionar para /profile com alerta
   - [ ] Bloquear acesso a todas as rotas exceto /profile e /auth/*

**7. Auth store - Melhorias**
   - [ ] Adicionar campo institutions (lista de instituições do usuário com roles)
   - [ ] Método para verificar se tem role específica em instituição
   - [ ] Computed isPending, isActive

#### Banco de dados

**1. Migration V4**
   - [ ] Criar arquivo V4__insert_default_admin_institution.sql
   - [ ] Inserir instituição "Administração Simplifica"
     - Nome: Administração Simplifica
     - Sigla: SIMP-ADMIN
     - Tipo: PRIVADA
     - Domínio: simplifica.ai
     - Active: true

---

## Plano de Implementação - Trilhas Paralelas

### FASE 1 - Trilhas Independentes (Executar em PARALELO)

#### Trilha 1: Database Setup
**Objetivo:** Criar instituição padrão no banco
**Dependências:** Nenhuma
**Tarefas:**
1. Criar migration V4__insert_default_admin_institution.sql
2. Inserir instituição "Administração Simplifica"

**Critérios de aceite:**
- Migration executa sem erros
- Instituição existe no banco com dados corretos

---

#### Trilha 2: Backend Admin API
**Objetivo:** Criar endpoints para gerenciamento de usuários
**Dependências:** Nenhuma
**Tarefas:**

1. **DTOs** (backend/src/main/java/com/simplifica/application/dto/)
   - UserListDTO.java
   - UserDetailDTO.java
   - UpdateUserRequest.java
   - UpdateUserRolesRequest.java
   - LinkUserInstitutionRequest.java
   - PagedResponseDTO.java

2. **Service** (backend/src/main/java/com/simplifica/application/service/)
   - UserAdminService.java
     - listUsers(filters, pageable) -> Page<UserListDTO>
     - getUserById(id) -> UserDetailDTO
     - updateUser(id, request) -> UserDetailDTO
     - updateUserRoles(userId, institutionId, roles) -> void
     - linkUserToInstitution(userId, institutionId, roles) -> void
     - unlinkUserFromInstitution(userId, institutionId) -> void

3. **Controller** (backend/src/main/java/com/simplifica/presentation/controller/)
   - AdminController.java
     - GET /admin/users
     - GET /admin/users/{id}
     - PUT /admin/users/{id}
     - PUT /admin/users/{id}/roles
     - POST /admin/users/{id}/institutions
     - DELETE /admin/users/{id}/institutions/{institutionId}

4. **Repository** (adicionar queries customizadas)
   - UserRepository.java
     - findAllWithFilters(filters, pageable)
     - findByIdWithInstitutions(id)

5. **Middleware**
   - PendingUserInterceptor.java (bloquear PENDING users)

**Critérios de aceite:**
- Todos os endpoints retornam 200/201 com dados corretos
- ADMIN pode listar todos os usuários
- GESTOR pode listar apenas usuários de sua instituição
- ADMIN pode vincular/desvincular instituições
- GESTOR não pode vincular/desvincular instituições
- Usuários PENDING são bloqueados (exceto /user/profile, /auth/*)
- Validações de permissão funcionam corretamente

---

#### Trilha 3: Frontend Profile & Alerts
**Objetivo:** Melhorar página de perfil e adicionar alerta para usuários pendentes
**Dependências:** Usa APIs já existentes (/user/profile, /user/institutions)
**Tarefas:**

1. **Componente de Alerta** (frontend/src/components/user/)
   - PendingUserAlert.vue

2. **ProfilePage.vue** (frontend/src/views/private/)
   - Melhorar layout
   - Adicionar seção "Minhas Instituições" com roles
   - Adicionar seção "Dados Pessoais"
   - Adicionar seção "Segurança"

3. **Auth Store** (frontend/src/stores/auth.store.ts)
   - Adicionar campo institutions
   - Método hasRoleInInstitution(institutionId, role)
   - Computed: isPending, isActive, isInactive

4. **Router Guards** (frontend/src/router/index.ts)
   - Adicionar validação de PENDING
   - Redirecionar PENDING para /profile

5. **App.vue**
   - Adicionar PendingUserAlert global

**Critérios de aceite:**
- Perfil mostra instituições e roles corretamente
- Alerta de PENDING aparece em todas as páginas
- Usuários PENDING não conseguem acessar outras rotas
- Redirecionamento funciona corretamente

---

### FASE 2 - Trilhas com Dependências (Executar APÓS Fase 1)

#### Trilha 4: Backend OAuth Auto-Link
**Objetivo:** Vincular automaticamente usuários por domínio de email
**Dependências:** Trilha 1 (instituição padrão deve existir)
**Tarefas:**

1. **Modificar CustomOAuth2UserService** (backend/src/main/java/com/simplifica/config/security/)
   - Extrair domínio do email (@ufms.br, @gmail.com, etc)
   - Buscar instituição por campo domain
   - Se encontrar:
     - Criar vínculo automático (UserInstitution)
     - Atribuir role VIEWER
     - Mudar status para ACTIVE
   - Se não encontrar:
     - Manter status PENDING

2. **InstitutionRepository**
   - Adicionar método findByDomain(domain)

**Critérios de aceite:**
- Usuário com email @ufms.br é vinculado automaticamente à UFMS
- Vínculo é criado com role VIEWER
- Status muda para ACTIVE
- Usuário com domínio não cadastrado fica PENDING
- Log de auditoria registra vinculação automática

---

#### Trilha 5: Frontend User Management
**Objetivo:** Implementar gerenciamento completo de usuários no frontend
**Dependências:** Trilha 2 (Backend Admin API deve estar pronta)
**Tarefas:**

1. **API Service** (frontend/src/services/)
   - userService.ts
     - listUsers(filters, pagination)
     - getUserById(id)
     - updateUser(id, data)
     - updateUserRoles(id, institutionId, roles)
     - linkUserToInstitution(userId, institutionId, roles)
     - unlinkUserFromInstitution(userId, institutionId)

2. **Types** (frontend/src/types/)
   - user.types.ts
     - UserListItem
     - UserDetail
     - UpdateUserRequest
     - UpdateUserRolesRequest
     - LinkUserInstitutionRequest

3. **Componentes de usuário** (frontend/src/components/user/)
   - UserEditDialog.vue (editar nome, status)
   - UserRolesDialog.vue (alterar papéis por instituição)
   - UserInstitutionsDialog.vue (vincular/desvincular instituições)

4. **UsersPage.vue** (frontend/src/views/private/admin/)
   - Implementar fetchUsers com filtros e paginação
   - Conectar filtros com backend
   - Implementar dialogs de edição
   - Implementar ações (edit, delete, manage-institutions)
   - Adicionar filtro de instituição (apenas para ADMIN)
   - Esconder filtro de instituição para GESTOR

**Critérios de aceite:**
- Listagem de usuários funciona com paginação
- Filtros (search, status, role, institution) funcionam
- ADMIN vê todos os usuários
- GESTOR vê apenas usuários de sua instituição
- GESTOR não vê filtro de instituição
- Edição de usuário funciona
- Alteração de papéis funciona
- Vinculação/desvinculação de instituições funciona (apenas ADMIN)
- Feedback visual (snackbar) em todas as operações

---

## Resumo das Trilhas

### Trilhas Independentes (FASE 1 - Paralelo):
- ✅ **Trilha 1**: Database Setup (migrations)
- ✅ **Trilha 2**: Backend Admin API (endpoints, services, DTOs)
- ✅ **Trilha 3**: Frontend Profile & Alerts (perfil, alerta PENDING)

### Trilhas Dependentes (FASE 2 - Paralelo):
- ✅ **Trilha 4**: Backend OAuth Auto-Link (vínculo automático por domínio)
- ✅ **Trilha 5**: Frontend User Management (gerenciamento completo de usuários)

---

## Critérios de Aceite Gerais

- [ ] Admin pode listar todos os usuários
- [ ] Gestor pode listar apenas usuários de sua instituição
- [ ] Admin pode vincular/desvincular usuários de instituições
- [ ] Gestor não pode vincular/desvincular usuários de instituições
- [ ] Admin e Gestor podem alterar papéis de usuários em suas instituições
- [ ] Vínculo automático por domínio de email funciona
- [ ] Usuários PENDING veem alerta em todas as páginas
- [ ] Usuários PENDING não conseguem acessar rotas além de /profile
- [ ] Filtros (search, status, role, institution) funcionam corretamente
- [ ] Paginação server-side funciona
- [ ] Instituição "Administração Simplifica" existe no banco
- [ ] Feedback visual (snackbar) em todas as operações
- [ ] Validações de permissão funcionam (ADMIN vs GESTOR)
- [ ] Sistema funciona em desktop e mobile

---

## Observações Técnicas

### Backend
- Usar @PreAuthorize para validação de roles
- Usar TenantContext para filtrar dados por instituição
- Usar soft delete (campo active) em vez de DELETE físico
- Registrar auditoria em security_audit_log
- Usar transações (@Transactional) em operações de escrita
- Validar dados com @Valid e javax.validation

### Frontend
- Usar Composition API com TypeScript
- Usar Pinia stores para state management
- Usar Vuetify para componentes UI
- Validações inline nos formulários
- Loading states e skeleton loaders
- Error handling com try/catch e snackbar
- Mobile-first design (responsivo)

### Banco de dados
- Usar Flyway para migrations
- Não modificar migrations já aplicadas
- Usar UUID como PK
- Indexes em colunas de filtro e ordenação
- Constraints de integridade referencial

---

## Próximos Passos

1. Revisar e aprovar este plano
2. Criar tarefas no backlog por trilha
3. Iniciar implementação da FASE 1 (trilhas paralelas)
4. Code review após cada trilha
5. Testar integração entre trilhas
6. Iniciar implementação da FASE 2
7. Code review final
8. QA completo
9. Deploy

---

**Autor:** Claude Sonnet 4.5
**Data:** 2026-01-23
