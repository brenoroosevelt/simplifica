# 🧪 Plano de Testes End-to-End Multi-Tenant

> **Princípio Fundamental:**
> *"As permissões de um usuário são determinadas exclusivamente pelo papel que ele possui na instituição ativa, nunca pelo conjunto de vínculos."*

---

## 📋 Sumário Executivo

Este documento define um conjunto robusto de testes **end-to-end** (E2E) que garantem o correto funcionamento das regras de multi-tenancy. Os testes **NÃO usam mocks de repositories** para garantir que as consultas SQL estejam corretas e que a segregação de dados funcione adequadamente.

---

## 🎯 Regras de Negócio (Resumo)

### Estrutura Base

| Conceito | Descrição |
|----------|-----------|
| **Instituições** | Organizações (tenants) que usam o sistema |
| **SIMP-ADMIN** | Instituição especial (id=1, acronym=SIMP-ADMIN) para administração do sistema |
| **Usuários** | Pessoas físicas que acessam via OAuth2 |
| **Vínculos** | Relacionamento User ↔ Institution com papéis específicos |
| **Instituição Ativa** | Instituição selecionada no contexto da requisição (obrigatória) |

### Papéis (Roles)

| Papel | Código | Onde pode ser atribuído | Permissões |
|-------|--------|------------------------|------------|
| **Admin** | `ADMIN` | **Apenas SIMP-ADMIN** | Acesso total ao sistema, gerencia todas instituições e usuários |
| **Gestor** | `MANAGER` | Qualquer instituição **exceto SIMP-ADMIN** | Gerencia usuários e dados da própria instituição |
| **Visualizador** | `VIEWER` | Qualquer instituição | Apenas leitura |

### Status de Usuário

| Status | Quando ocorre | Comportamento |
|--------|---------------|---------------|
| `PENDING` | Após primeiro login OAuth, sem vínculos ativos | Aguarda aprovação de ADMIN |
| `ACTIVE` | Possui pelo menos um vínculo ativo | Pode acessar o sistema |
| `INACTIVE` | Desativado manualmente | Bloqueado do sistema |

### Regras Críticas

1. ✅ **Segregação por Instituição Ativa**: Permissões são **SEMPRE** baseadas na instituição ativa, não no conjunto de vínculos
2. ✅ **Role ADMIN exclusivo**: Só pode ser atribuído em SIMP-ADMIN
3. ✅ **GESTOR não acessa SIMP-ADMIN**: Gestor não pode gerenciar a instituição administrativa
4. ✅ **Status automático**: PENDING é gerenciado automaticamente ao criar/remover vínculos
5. ✅ **Validação redundante**: Backend E frontend devem validar as mesmas regras

---

## 🏗️ Estrutura dos Testes

### Tipo de Testes

**Testes de Integração E2E** (Integration Tests)
- Sobem contexto Spring completo (`@SpringBootTest`)
- Banco H2 in-memory (sem mocks de repository)
- TestRestTemplate ou MockMvc para chamadas HTTP
- Validam desde JSON de entrada até JSON de saída
- Verificam estado do banco de dados após operações

### Organização

```
backend/src/test/java/com/simplifica/
└── integration/
    ├── BaseIntegrationTest.java           # Classe base com setup comum
    ├── auth/
    │   ├── OAuthFlowIntegrationTest.java  # Fluxo OAuth e primeiro login
    │   └── JwtAuthenticationTest.java     # Autenticação JWT
    ├── admin/
    │   ├── AdminUserManagementTest.java   # ADMIN gerenciando usuários
    │   ├── AdminInstitutionLinkTest.java  # ADMIN vinculando usuários
    │   └── AdminInstitutionAccessTest.java # ADMIN acessando todas instituições
    ├── manager/
    │   ├── ManagerUserManagementTest.java # GESTOR gerenciando usuários
    │   ├── ManagerInstitutionAccessTest.java # GESTOR acessando própria instituição
    │   └── ManagerSecurityTest.java       # Tentativas de violação de segurança
    ├── viewer/
    │   └── ViewerAccessTest.java          # VIEWER apenas lendo dados
    ├── multitenancy/
    │   ├── InstitutionSwitchingTest.java  # Trocar instituição ativa
    │   ├── CrossTenantIsolationTest.java  # Segregação entre tenants
    │   └── PermissionByActiveInstitutionTest.java # Permissões por inst. ativa
    └── status/
        ├── UserStatusLifecycleTest.java   # Ciclo de vida de status
        └── PendingUserFlowTest.java       # Fluxo de usuário pendente
```

---

## 🧪 Casos de Teste Detalhados

### 1. Autenticação e Primeiro Acesso

#### 1.1 Primeiro Login via OAuth
**Arquivo:** `OAuthFlowIntegrationTest.java`

```java
@Test
void novoUsuarioDeveSerCriadoComStatusPending() {
    // Simula callback OAuth com novo usuário
    // DADO: Usuário não existe no sistema
    // QUANDO: Faz login via Google OAuth
    // ENTÃO:
    //   - Usuário criado com status PENDING
    //   - Sem instituições vinculadas
    //   - Não pode acessar recursos protegidos
}

@Test
void usuarioPendenteNaoPodeAcessarRecursosProtegidos() {
    // DADO: Usuário com status PENDING (sem vínculos)
    // QUANDO: Tenta acessar /api/dashboard
    // ENTÃO: 403 Forbidden
}
```

#### 1.2 JWT e Authorities
**Arquivo:** `JwtAuthenticationTest.java`

```java
@Test
void jwtDeveConterRoleAdminParaUsuarioAdminNaSimpAdmin() {
    // DADO: Usuário com ADMIN na SIMP-ADMIN
    // QUANDO: Gera JWT
    // ENTÃO: Token contém ROLE_ADMIN
}

@Test
void jwtDeveConterRoleManagerParaUsuarioManagerEmInstituicao() {
    // DADO: Usuário com MANAGER na UFMS
    // QUANDO: Gera JWT
    // ENTÃO: Token contém ROLE_MANAGER
}

@Test
void jwtNaoDeveConterRoleAdminSeAdminNaoEstiverNaSimpAdmin() {
    // DADO: Usuário com ADMIN na UFMS (configuração inválida hipotética)
    // QUANDO: Gera JWT
    // ENTÃO: Token NÃO contém ROLE_ADMIN
}
```

---

### 2. Admin - Gerenciamento Total do Sistema

#### 2.1 Listar Usuários
**Arquivo:** `AdminUserManagementTest.java`

```java
@Test
void adminDeveVerTodosOsUsuariosDeSistema() {
    // SETUP:
    //   - Admin com SIMP-ADMIN ativa
    //   - 5 usuários em diferentes instituições
    // QUANDO: GET /api/admin/users
    // ENTÃO: Retorna todos os 5 usuários
}

@Test
void adminDevePodeFiltrarPorInstituicao() {
    // SETUP:
    //   - Admin com SIMP-ADMIN ativa
    //   - 3 usuários na UFMS, 2 na UFPR
    // QUANDO: GET /api/admin/users?institutionId=UFMS_ID
    // ENTÃO: Retorna apenas 3 usuários da UFMS
}

@Test
void adminDevePodeFiltrarPorStatus() {
    // SETUP:
    //   - 2 PENDING, 3 ACTIVE, 1 INACTIVE
    // QUANDO: GET /api/admin/users?status=PENDING
    // ENTÃO: Retorna apenas 2 PENDING
}

@Test
void adminDevePodeFiltrarPorRole() {
    // QUANDO: GET /api/admin/users?role=MANAGER
    // ENTÃO: Retorna apenas usuários com role MANAGER
}
```

#### 2.2 Vincular Usuários a Instituições
**Arquivo:** `AdminInstitutionLinkTest.java`

```java
@Test
void adminDeveVincularUsuarioPendenteAInstituicao() {
    // DADO:
    //   - Admin com SIMP-ADMIN ativa
    //   - Usuário PENDING (sem vínculos)
    // QUANDO: POST /api/admin/users/{userId}/institutions
    //   { institutionId: UFMS_ID, roles: [MANAGER] }
    // ENTÃO:
    //   - Vínculo criado
    //   - Status muda de PENDING para ACTIVE
    //   - Auditoria registrada
}

@Test
void adminDeveVincularUsuarioComRoleAdmin() {
    // DADO: Admin com SIMP-ADMIN ativa
    // QUANDO: POST /api/admin/users/{userId}/institutions
    //   { institutionId: SIMP_ADMIN_ID, roles: [ADMIN] }
    // ENTÃO:
    //   - Vínculo criado com sucesso
    //   - Usuário tem ADMIN na SIMP-ADMIN
}

@Test
void naoDevePermitirRoleAdminForaDaSimpAdmin() {
    // DADO: Admin com SIMP-ADMIN ativa
    // QUANDO: POST /api/admin/users/{userId}/institutions
    //   { institutionId: UFMS_ID, roles: [ADMIN] }
    // ENTÃO:
    //   - 400 Bad Request
    //   - Erro: "ADMIN role can only be assigned to SIMP-ADMIN"
}

@Test
void adminDeveDesvincularUsuarioDaInstituicao() {
    // DADO:
    //   - Admin com SIMP-ADMIN ativa
    //   - Usuário vinculado à UFMS
    // QUANDO: DELETE /api/admin/users/{userId}/institutions/{UFMS_ID}
    // ENTÃO:
    //   - Vínculo desativado (soft delete)
    //   - Se era último vínculo, status volta para PENDING
    //   - Auditoria registrada
}

@Test
void desvincularUltimoVinculoDeveVoltarParaPending() {
    // DADO:
    //   - Usuário ACTIVE com apenas 1 vínculo (UFMS)
    // QUANDO: DELETE /api/admin/users/{userId}/institutions/{UFMS_ID}
    // ENTÃO:
    //   - Vínculo desativado
    //   - Status muda de ACTIVE para PENDING
}

@Test
void desvincularSegundoVinculoNaoDeveMudarStatus() {
    // DADO:
    //   - Usuário ACTIVE com 2 vínculos (UFMS + UFPR)
    // QUANDO: DELETE /api/admin/users/{userId}/institutions/{UFMS_ID}
    // ENTÃO:
    //   - Vínculo UFMS desativado
    //   - Status permanece ACTIVE (ainda tem UFPR)
}
```

#### 2.3 Atualizar Papéis
**Arquivo:** `AdminUserManagementTest.java`

```java
@Test
void adminDeveAtualizarRolesDeUsuarioEmQualquerInstituicao() {
    // DADO:
    //   - Admin com SIMP-ADMIN ativa
    //   - Usuário com MANAGER na UFMS
    // QUANDO: PUT /api/admin/users/{userId}/roles
    //   { institutionId: UFMS_ID, roles: [VIEWER] }
    // ENTÃO:
    //   - Roles atualizadas
    //   - Auditoria registrada
}

@Test
void adminDeveAtualizarStatusDeUsuario() {
    // DADO: Usuário ACTIVE
    // QUANDO: PUT /api/admin/users/{userId}
    //   { status: INACTIVE }
    // ENTÃO:
    //   - Status atualizado para INACTIVE
    //   - Auditoria registrada
}

@Test
void naoDevePermitirSetarStatusPendingManualmente() {
    // DADO: Usuário ACTIVE com vínculos
    // QUANDO: PUT /api/admin/users/{userId}
    //   { status: PENDING }
    // ENTÃO:
    //   - 400 Bad Request
    //   - Erro: "Status PENDING is managed automatically"
}
```

#### 2.4 Gerenciar Instituições
**Arquivo:** `AdminInstitutionAccessTest.java`

```java
@Test
void adminDeveVerTodasAsInstituicoes() {
    // DADO: 5 instituições cadastradas (incluindo SIMP-ADMIN)
    // QUANDO: GET /api/institutions
    // ENTÃO: Retorna todas as 5 instituições
}

@Test
void adminDeveCriarNovaInstituicao() {
    // QUANDO: POST /api/institutions
    //   { name: "UFRJ", acronym: "UFRJ", type: FEDERAL }
    // ENTÃO:
    //   - Instituição criada
    //   - Retorna 201 Created
}

@Test
void adminDeveEditarQualquerInstituicao() {
    // QUANDO: PUT /api/institutions/{UFMS_ID}
    //   { name: "Nova UFMS", ... }
    // ENTÃO: Instituição atualizada
}

@Test
void adminDeveAlterarStatusDaInstituicao() {
    // QUANDO: PUT /api/institutions/{UFMS_ID}
    //   { active: false }
    // ENTÃO: Instituição desativada
}

@Test
void adminDeveDeletarInstituicao() {
    // QUANDO: DELETE /api/institutions/{UFMS_ID}
    // ENTÃO: Instituição deletada
}
```

---

### 3. Gestor (Manager) - Gerenciamento da Própria Instituição

#### 3.1 Listar Usuários
**Arquivo:** `ManagerUserManagementTest.java`

```java
@Test
void gestorDeveVerApenasUsuariosDaPropriaInstituicao() {
    // SETUP:
    //   - Gestor com MANAGER na UFMS (ativa)
    //   - 3 usuários na UFMS
    //   - 2 usuários na UFPR
    //   - 1 usuário na SIMP-ADMIN
    // QUANDO: GET /api/admin/users
    //   Header: X-Institution-Id: UFMS_ID
    // ENTÃO:
    //   - Retorna apenas 3 usuários da UFMS
    //   - Não retorna usuários da UFPR
    //   - Não retorna usuários da SIMP-ADMIN
}

@Test
void gestorNaoDeveVerColunaDeInstituicoes() {
    // QUANDO: GET /api/admin/users
    // ENTÃO:
    //   - Campo "institutions" não é populado no DTO
    //   - Ou retorna apenas instituição ativa
}

@Test
void gestorNaoPodeFiltrarPorOutraInstituicao() {
    // DADO: Gestor com MANAGER na UFMS (ativa)
    // QUANDO: GET /api/admin/users?institutionId=UFPR_ID
    // ENTÃO:
    //   - Backend ignora filtro de institutionId
    //   - Força filtro pela UFMS (instituição ativa)
    //   - Retorna apenas usuários da UFMS
}
```

#### 3.2 Atualizar Papéis
**Arquivo:** `ManagerUserManagementTest.java`

```java
@Test
void gestorDeveAtualizarRolesNaPropriaInstituicao() {
    // DADO:
    //   - Gestor com MANAGER na UFMS (ativa)
    //   - Usuário com VIEWER na UFMS
    // QUANDO: PUT /api/admin/users/{userId}/roles
    //   { institutionId: UFMS_ID, roles: [MANAGER] }
    // ENTÃO:
    //   - Roles atualizadas
    //   - Auditoria registrada
}

@Test
void gestorNaoPodeAtribuirRoleAdmin() {
    // QUANDO: PUT /api/admin/users/{userId}/roles
    //   { institutionId: UFMS_ID, roles: [ADMIN] }
    // ENTÃO:
    //   - 400 Bad Request
    //   - Erro: "ADMIN role can only be assigned to SIMP-ADMIN"
}

@Test
void gestorNaoPodeAtualizarRolesEmOutraInstituicao() {
    // DADO: Gestor com MANAGER na UFMS (ativa)
    // QUANDO: PUT /api/admin/users/{userId}/roles
    //   { institutionId: UFPR_ID, roles: [VIEWER] }
    // ENTÃO:
    //   - 403 Forbidden
    //   - Erro: "GESTOR can only manage users in their own institution"
}

@Test
void gestorNaoPodeAtualizarRolesDeUsuarioDeOutraInstituicao() {
    // DADO:
    //   - Gestor com MANAGER na UFMS (ativa)
    //   - Usuário vinculado APENAS à UFPR (não vinculado à UFMS)
    // QUANDO: GET /api/admin/users/{userId}
    // ENTÃO:
    //   - 403 Forbidden
    //   - Erro: "GESTOR can only access users from their own institution"
}
```

#### 3.3 Acesso à Instituição
**Arquivo:** `ManagerInstitutionAccessTest.java`

```java
@Test
void gestorDeveVerApenasPropriaInstituicao() {
    // DADO: Gestor com MANAGER na UFMS (ativa)
    // QUANDO: GET /api/institutions
    // ENTÃO:
    //   - Retorna apenas 1 instituição (UFMS)
    //   - Não retorna SIMP-ADMIN
    //   - Não retorna outras instituições
}

@Test
void gestorDeveEditarPropriaInstituicao() {
    // QUANDO: PUT /api/institutions/{UFMS_ID}
    //   { name: "UFMS Atualizada" }
    // ENTÃO: Instituição atualizada
}

@Test
void gestorNaoPodeAlterarStatusDaInstituicao() {
    // QUANDO: PUT /api/institutions/{UFMS_ID}
    //   { active: false }
    // ENTÃO:
    //   - 403 Forbidden OU
    //   - Campo ignorado (não é atualizado)
}

@Test
void gestorNaoPodeAlterarDominioDaInstituicao() {
    // QUANDO: PUT /api/institutions/{UFMS_ID}
    //   { domain: "novo-dominio.com" }
    // ENTÃO:
    //   - 403 Forbidden OU
    //   - Campo ignorado
}

@Test
void gestorNaoPodeDeletarInstituicao() {
    // QUANDO: DELETE /api/institutions/{UFMS_ID}
    // ENTÃO: 403 Forbidden
}

@Test
void gestorNaoPodeCriarNovaInstituicao() {
    // QUANDO: POST /api/institutions
    // ENTÃO: 403 Forbidden
}
```

#### 3.4 Tentativas de Violação de Segurança
**Arquivo:** `ManagerSecurityTest.java`

```java
@Test
void gestorNaoPodeVincularUsuarioAInstituicao() {
    // QUANDO: POST /api/admin/users/{userId}/institutions
    // ENTÃO: 403 Forbidden (endpoint requer ROLE_ADMIN)
}

@Test
void gestorNaoPodeDesvincularUsuarioDaInstituicao() {
    // QUANDO: DELETE /api/admin/users/{userId}/institutions/{instId}
    // ENTÃO: 403 Forbidden
}

@Test
void gestorNaoPodeAcessarSimpAdmin() {
    // DADO: Gestor com MANAGER na UFMS
    // QUANDO: GET /api/institutions/1 (SIMP-ADMIN)
    // ENTÃO: 404 Not Found (instituição não aparece)
}

@Test
void gestorNaoPodeAcessarPaginaDeUsuariosSemInstituicaoAtiva() {
    // QUANDO: GET /api/admin/users
    //   Header: X-Institution-Id: (vazio)
    // ENTÃO:
    //   - 400 Bad Request
    //   - Erro: "GESTOR must have an active institution"
}
```

---

### 4. Viewer - Apenas Leitura

#### 4.1 Acesso Somente Leitura
**Arquivo:** `ViewerAccessTest.java`

```java
@Test
void viewerNaoPodeAcessarPaginaDeUsuarios() {
    // DADO: Usuário com VIEWER na UFMS
    // QUANDO: GET /api/admin/users
    // ENTÃO: 403 Forbidden
}

@Test
void viewerNaoPodeAcessarPaginaDeInstituicoes() {
    // QUANDO: GET /api/institutions
    // ENTÃO: 403 Forbidden (ou retorna vazio, dependendo da implementação)
}

@Test
void viewerDeveAcessarProprioProfile() {
    // QUANDO: GET /api/profile
    // ENTÃO: 200 OK com dados do próprio usuário
}

@Test
void viewerNaoPodeEditarNadaNoSistema() {
    // QUANDO: PUT, POST, DELETE em qualquer recurso administrativo
    // ENTÃO: 403 Forbidden
}
```

---

### 5. Multi-Tenancy e Troca de Instituição

#### 5.1 Trocar Instituição Ativa
**Arquivo:** `InstitutionSwitchingTest.java`

```java
@Test
void usuarioDevePoderTrocarInstituicaoAtiva() {
    // DADO:
    //   - Usuário com ADMIN na SIMP-ADMIN
    //   - Usuário com MANAGER na UFMS
    // QUANDO: Faz requisição com X-Institution-Id: SIMP_ADMIN_ID
    // ENTÃO: Permissões de ADMIN ativas
    //
    // QUANDO: Faz requisição com X-Institution-Id: UFMS_ID
    // ENTÃO: Permissões de MANAGER ativas
}

@Test
void permissoesDevemMudarAoTrocarInstituicao() {
    // SETUP:
    //   - Usuário "João" com:
    //     - ADMIN na SIMP-ADMIN
    //     - MANAGER na UFMS
    //     - VIEWER na UFPR
    //
    // CENÁRIO 1 - Com SIMP-ADMIN ativa:
    // QUANDO: GET /api/admin/users
    // ENTÃO: Retorna todos os usuários
    //
    // CENÁRIO 2 - Com UFMS ativa:
    // QUANDO: GET /api/admin/users
    // ENTÃO: Retorna apenas usuários da UFMS
    //
    // CENÁRIO 3 - Com UFPR ativa:
    // QUANDO: GET /api/admin/users
    // ENTÃO: 403 Forbidden (VIEWER não acessa)
}

@Test
void usuarioNaoPodeSelecionarInstituicaoQueNaoPertence() {
    // DADO: Usuário vinculado apenas à UFMS
    // QUANDO: Faz requisição com X-Institution-Id: UFPR_ID
    // ENTÃO:
    //   - 403 Forbidden OU
    //   - 400 Bad Request: "User not linked to this institution"
}

@Test
void requisicaoSemInstituicaoAtivaDeveSerRejeitada() {
    // QUANDO: Faz requisição sem header X-Institution-Id
    // ENTÃO:
    //   - 400 Bad Request
    //   - Erro: "X-Institution-Id header is required"
}
```

#### 5.2 Segregação Entre Tenants (Cross-Tenant Isolation)
**Arquivo:** `CrossTenantIsolationTest.java`

```java
@Test
void gestorNaoDeveVerDadosDeOutraInstituicao() {
    // SETUP:
    //   - Gestor A com MANAGER na UFMS
    //   - Gestor B com MANAGER na UFPR
    //   - 3 usuários na UFMS, 2 na UFPR
    //
    // QUANDO: Gestor A lista usuários (com UFMS ativa)
    // ENTÃO: Vê apenas 3 usuários da UFMS
    //
    // QUANDO: Gestor B lista usuários (com UFPR ativa)
    // ENTÃO: Vê apenas 2 usuários da UFPR
}

@Test
void gestorNaoDeveEditarUsuarioDeOutraInstituicao() {
    // DADO:
    //   - Gestor A com MANAGER na UFMS
    //   - Usuário X vinculado apenas à UFPR
    // QUANDO: Gestor A tenta editar Usuário X
    // ENTÃO: 403 Forbidden
}

@Test
void usuarioComMultiplosVinculosDeveVerDadosApenasInstituicaoAtiva() {
    // DADO:
    //   - Usuário "Maria" com:
    //     - MANAGER na UFMS
    //     - MANAGER na UFPR
    //   - 5 usuários na UFMS
    //   - 3 usuários na UFPR
    //
    // QUANDO: Maria acessa com UFMS ativa
    // ENTÃO: Vê 5 usuários da UFMS
    //
    // QUANDO: Maria acessa com UFPR ativa
    // ENTÃO: Vê 3 usuários da UFPR
}

@Test
void permissoesDevemSerBaseadasNaInstituicaoAtivaNaoNoConjunto() {
    // SETUP:
    //   - Usuário "Pedro" com:
    //     - ADMIN na SIMP-ADMIN
    //     - VIEWER na UFMS
    //
    // CENÁRIO 1 - Com SIMP-ADMIN ativa:
    // QUANDO: Tenta criar instituição
    // ENTÃO: 201 Created (tem permissão de ADMIN)
    //
    // CENÁRIO 2 - Com UFMS ativa:
    // QUANDO: Tenta criar instituição
    // ENTÃO: 403 Forbidden (VIEWER não pode criar)
}
```

#### 5.3 Permissões por Instituição Ativa
**Arquivo:** `PermissionByActiveInstitutionTest.java`

```java
@Test
void adminComSimpAdminInativaDeveTerPermissoesReduzidas() {
    // DADO:
    //   - Usuário com ADMIN na SIMP-ADMIN
    //   - Usuário com VIEWER na UFMS
    //   - Instituição ativa: UFMS
    //
    // QUANDO: Tenta gerenciar todos os usuários
    // ENTÃO: 403 Forbidden (age como VIEWER)
}

@Test
void managerComInstituicaoInativaDevePerdederPermissoes() {
    // DADO:
    //   - Usuário com MANAGER na UFMS
    //   - Usuário com MANAGER na UFPR
    //   - Instituição ativa: UFMS
    //
    // QUANDO: Tenta editar usuário da UFPR
    // ENTÃO: 403 Forbidden
}
```

---

### 6. Ciclo de Vida de Status

#### 6.1 Status Automático
**Arquivo:** `UserStatusLifecycleTest.java`

```java
@Test
void primeiroLoginDeveCriarUsuarioPending() {
    // QUANDO: Usuário faz primeiro login OAuth
    // ENTÃO: Status = PENDING
}

@Test
void primeiroVinculoDeveMudarParaActive() {
    // DADO: Usuário PENDING
    // QUANDO: Admin vincula à UFMS
    // ENTÃO: Status = ACTIVE
}

@Test
void removerUltimoVinculoDeveMudarParaPending() {
    // DADO: Usuário ACTIVE com 1 vínculo
    // QUANDO: Admin remove o vínculo
    // ENTÃO: Status = PENDING
}

@Test
void removerSegundoVinculoNaoDeveMudarStatus() {
    // DADO: Usuário ACTIVE com 2 vínculos
    // QUANDO: Admin remove 1 vínculo
    // ENTÃO: Status = ACTIVE (ainda tem 1 vínculo)
}

@Test
void usuarioPendenteNaoPodeSerAtivadoManualmente() {
    // DADO: Usuário PENDING sem vínculos
    // QUANDO: Admin tenta mudar status para ACTIVE
    // ENTÃO: 400 Bad Request
}

@Test
void statusPendingNaoPodeSerSetadoManualmente() {
    // DADO: Usuário ACTIVE
    // QUANDO: Admin tenta mudar status para PENDING
    // ENTÃO: 400 Bad Request
}

@Test
void usuarioAtivoComVinculoPodeSerInativado() {
    // DADO: Usuário ACTIVE com vínculos
    // QUANDO: Admin muda status para INACTIVE
    // ENTÃO: Status = INACTIVE
}

@Test
void usuarioInativoComVinculoPodeSerReativado() {
    // DADO: Usuário INACTIVE com vínculos
    // QUANDO: Admin muda status para ACTIVE
    // ENTÃO: Status = ACTIVE
}
```

#### 6.2 Fluxo Completo de Usuário Pendente
**Arquivo:** `PendingUserFlowTest.java`

```java
@Test
void fluxoCompletoDeUsuarioPendente() {
    // ETAPA 1: Primeiro Login
    // QUANDO: Usuário faz login OAuth pela primeira vez
    // ENTÃO:
    //   - Usuário criado
    //   - Status = PENDING
    //   - Sem vínculos

    // ETAPA 2: Tentativa de Acesso
    // QUANDO: Usuário tenta acessar /api/dashboard
    // ENTÃO: 403 Forbidden (pendente não pode acessar)

    // ETAPA 3: Admin Vincula
    // QUANDO: Admin vincula usuário à UFMS com role MANAGER
    // ENTÃO:
    //   - Vínculo criado
    //   - Status muda para ACTIVE
    //   - Auditoria registrada

    // ETAPA 4: Acesso Liberado
    // QUANDO: Usuário tenta acessar /api/dashboard (com UFMS ativa)
    // ENTÃO: 200 OK (agora tem acesso)

    // ETAPA 5: Usuário Gerencia Sua Instituição
    // QUANDO: Usuário lista usuários da UFMS
    // ENTÃO: 200 OK com lista de usuários
}

@Test
void usuarioPendenteDeveAparecerNaListagemParaAdmin() {
    // DADO: Usuário PENDING
    // QUANDO: Admin lista usuários
    // ENTÃO:
    //   - Usuário aparece na lista
    //   - Status exibido como PENDING
    //   - Sem instituições vinculadas
}

@Test
void adminDevePodeFiltrarUsuariosPendentes() {
    // QUANDO: Admin filtra por status=PENDING
    // ENTÃO: Retorna apenas usuários pendentes
}
```

---

## 🔧 Implementação dos Testes

### Classe Base para Testes de Integração

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected InstitutionRepository institutionRepository;

    @Autowired
    protected UserInstitutionRepository userInstitutionRepository;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    // Instituições para testes
    protected Institution simpAdmin;
    protected Institution ufms;
    protected Institution ufpr;

    // Usuários para testes
    protected User adminUser;
    protected User managerUserUfms;
    protected User managerUserUfpr;
    protected User viewerUser;
    protected User pendingUser;
    protected User multiInstitutionUser;

    @BeforeEach
    void baseSetup() {
        // Criar instituições
        simpAdmin = createInstitution("SIMP-ADMIN", "Simplifica Admin", InstitutionType.FEDERAL);
        ufms = createInstitution("UFMS", "Universidade Federal de MS", InstitutionType.FEDERAL);
        ufpr = createInstitution("UFPR", "Universidade Federal do PR", InstitutionType.FEDERAL);

        // Criar usuários base
        adminUser = createUserWithLink(
            "admin@simplifica.com", "Admin User",
            simpAdmin, Set.of(InstitutionRole.ADMIN)
        );

        managerUserUfms = createUserWithLink(
            "gestor.ufms@ufms.br", "Gestor UFMS",
            ufms, Set.of(InstitutionRole.MANAGER)
        );

        managerUserUfpr = createUserWithLink(
            "gestor.ufpr@ufpr.br", "Gestor UFPR",
            ufpr, Set.of(InstitutionRole.MANAGER)
        );

        viewerUser = createUserWithLink(
            "viewer@ufms.br", "Viewer User",
            ufms, Set.of(InstitutionRole.VIEWER)
        );

        pendingUser = createPendingUser("pending@email.com", "Pending User");

        // Usuário com múltiplos vínculos
        multiInstitutionUser = createUser("multi@email.com", "Multi User");
        linkUserToInstitution(multiInstitutionUser, simpAdmin, Set.of(InstitutionRole.ADMIN));
        linkUserToInstitution(multiInstitutionUser, ufms, Set.of(InstitutionRole.MANAGER));
        linkUserToInstitution(multiInstitutionUser, ufpr, Set.of(InstitutionRole.VIEWER));
    }

    // Métodos auxiliares

    protected Institution createInstitution(String acronym, String name, InstitutionType type) {
        return institutionRepository.save(Institution.builder()
            .acronym(acronym)
            .name(name)
            .type(type)
            .active(true)
            .build());
    }

    protected User createPendingUser(String email, String name) {
        return userRepository.save(User.builder()
            .email(email)
            .name(name)
            .provider(OAuth2Provider.GOOGLE)
            .providerId(UUID.randomUUID().toString())
            .status(UserStatus.PENDING)
            .build());
    }

    protected User createUser(String email, String name) {
        return userRepository.save(User.builder()
            .email(email)
            .name(name)
            .provider(OAuth2Provider.GOOGLE)
            .providerId(UUID.randomUUID().toString())
            .status(UserStatus.ACTIVE)
            .build());
    }

    protected User createUserWithLink(String email, String name,
                                     Institution institution, Set<InstitutionRole> roles) {
        User user = createUser(email, name);
        linkUserToInstitution(user, institution, roles);
        return user;
    }

    protected UserInstitution linkUserToInstitution(User user, Institution institution,
                                                   Set<InstitutionRole> roles) {
        UserInstitution link = UserInstitution.builder()
            .user(user)
            .institution(institution)
            .roles(roles)
            .linkedBy(adminUser)
            .active(true)
            .build();
        return userInstitutionRepository.save(link);
    }

    protected String generateToken(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        return jwtTokenProvider.generateToken(principal);
    }

    protected HttpHeaders createHeaders(User user, Institution activeInstitution) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(generateToken(user));
        headers.set("X-Institution-Id", activeInstitution.getId().toString());
        return headers;
    }

    protected <T> ResponseEntity<T> get(String url, User user, Institution activeInstitution,
                                       Class<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders(user, activeInstitution));
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    protected <T, R> ResponseEntity<R> post(String url, T body, User user,
                                           Institution activeInstitution, Class<R> responseType) {
        HttpEntity<T> entity = new HttpEntity<>(body, createHeaders(user, activeInstitution));
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    protected <T, R> ResponseEntity<R> put(String url, T body, User user,
                                          Institution activeInstitution, Class<R> responseType) {
        HttpEntity<T> entity = new HttpEntity<>(body, createHeaders(user, activeInstitution));
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }

    protected <T> ResponseEntity<T> delete(String url, User user, Institution activeInstitution,
                                          Class<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders(user, activeInstitution));
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
    }
}
```

---

## 📊 Matriz de Testes (Resumo)

### Por Papel e Operação

| Operação | ADMIN (SIMP-ADMIN ativa) | MANAGER (Inst. ativa) | VIEWER | PENDING |
|----------|--------------------------|----------------------|---------|---------|
| **Listar todos usuários** | ✅ Sim | ❌ Não (apenas sua inst.) | ❌ Não | ❌ Não |
| **Listar usuários filtrados** | ✅ Sim | ✅ Força filtro inst. ativa | ❌ Não | ❌ Não |
| **Ver detalhes de usuário** | ✅ Qualquer usuário | ✅ Apenas de sua inst. | ❌ Não | ❌ Não |
| **Editar usuário** | ✅ Qualquer usuário | ✅ Apenas de sua inst. | ❌ Não | ❌ Não |
| **Mudar status** | ✅ Sim (exceto PENDING) | ✅ Apenas sua inst. | ❌ Não | ❌ Não |
| **Atualizar roles** | ✅ Qualquer inst. | ✅ Apenas sua inst. | ❌ Não | ❌ Não |
| **Atribuir ADMIN role** | ✅ Apenas SIMP-ADMIN | ❌ Não | ❌ Não | ❌ Não |
| **Vincular usuário** | ✅ Qualquer inst. | ❌ Não | ❌ Não | ❌ Não |
| **Desvincular usuário** | ✅ Qualquer inst. | ❌ Não | ❌ Não | ❌ Não |
| **Listar instituições** | ✅ Todas | ✅ Apenas própria | ❌ Não | ❌ Não |
| **Criar instituição** | ✅ Sim | ❌ Não | ❌ Não | ❌ Não |
| **Editar instituição** | ✅ Qualquer | ✅ Apenas própria (limitado) | ❌ Não | ❌ Não |
| **Mudar status inst.** | ✅ Sim | ❌ Não | ❌ Não | ❌ Não |
| **Mudar domínio inst.** | ✅ Sim | ❌ Não | ❌ Não | ❌ Não |
| **Deletar instituição** | ✅ Sim | ❌ Não | ❌ Não | ❌ Não |

---

## 🎯 Cobertura de Testes

### Por Funcionalidade

| Categoria | Quantidade de Testes | Prioridade |
|-----------|---------------------|-----------|
| **Autenticação e Primeiro Acesso** | ~6 testes | 🔴 Crítica |
| **Admin - Gerenciamento de Usuários** | ~15 testes | 🔴 Crítica |
| **Admin - Vínculos de Instituições** | ~10 testes | 🔴 Crítica |
| **Admin - Gerenciamento de Instituições** | ~8 testes | 🟡 Alta |
| **Manager - Listar Usuários** | ~5 testes | 🔴 Crítica |
| **Manager - Atualizar Papéis** | ~8 testes | 🔴 Crítica |
| **Manager - Acesso a Instituição** | ~10 testes | 🔴 Crítica |
| **Manager - Violações de Segurança** | ~6 testes | 🔴 Crítica |
| **Viewer - Acesso Limitado** | ~5 testes | 🟡 Alta |
| **Multi-Tenancy - Troca de Instituição** | ~6 testes | 🔴 Crítica |
| **Multi-Tenancy - Segregação** | ~8 testes | 🔴 Crítica |
| **Multi-Tenancy - Permissões** | ~5 testes | 🔴 Crítica |
| **Status - Ciclo de Vida** | ~10 testes | 🔴 Crítica |
| **Status - Fluxo Pendente** | ~5 testes | 🟡 Alta |
| **TOTAL** | **~107 testes** | |

---

## ✅ Checklist de Validação

Após implementar os testes, validar:

- [ ] Todos os testes passam sem mocks de repository
- [ ] Queries SQL estão corretas (verificar logs do Hibernate)
- [ ] Segregação de dados funciona (gestor não vê outras instituições)
- [ ] Permissões baseadas em instituição ativa (não no conjunto)
- [ ] Status PENDING é automático
- [ ] Role ADMIN só na SIMP-ADMIN
- [ ] Auditoria registrada em todas operações
- [ ] Não há N+1 queries
- [ ] Testes cobrem cenários de sucesso E falha
- [ ] Testes cobrem violações de segurança
- [ ] Testes cobrem edge cases (último vínculo, múltiplos vínculos, etc)
- [ ] Cobertura de código >= 80%

---

## 🚀 Próximos Passos

1. **Implementar BaseIntegrationTest**: Classe base com setup comum
2. **Implementar testes críticos primeiro**:
   - Segregação multi-tenant
   - Permissões por instituição ativa
   - Fluxo de usuário pendente
3. **Implementar testes de segurança**:
   - Manager tentando violar regras
   - Cross-tenant isolation
4. **Implementar testes de Admin**
5. **Implementar testes de Viewer**
6. **Implementar testes de ciclo de vida**
7. **Executar todos os testes e garantir 100% de aprovação**
8. **Documentar qualquer regra adicional descoberta**

---

## 📝 Notas Finais

### Por que SEM Mocks de Repository?

- ✅ Garante que as queries JPA/JPQL estão corretas
- ✅ Valida joins, filtros e cláusulas WHERE
- ✅ Detecta problemas de N+1 queries
- ✅ Testa a segregação real de dados
- ✅ Mais confiança nas regras de negócio

### Configuração H2 para Testes

O banco H2 deve ser configurado para simular PostgreSQL:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
```

### Performance

- Use `@DirtiesContext` para limpar contexto entre testes
- Considere usar `@Sql` para popular dados específicos
- Profile testes com muitos dados para detectar gargalos

---

**Versão:** 1.0
**Data:** 26/01/2026
**Autor:** Claude Code
**Status:** 📋 Planejamento Completo
