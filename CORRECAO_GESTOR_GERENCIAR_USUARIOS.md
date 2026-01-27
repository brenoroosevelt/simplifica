# ✅ CORREÇÃO: Gestor Pode Gerenciar Usuários da Instituição

## 📋 PROBLEMA IDENTIFICADO

Após a correção de segregação multi-tenant, o menu "Usuários" desapareceu para Gestores (MANAGER), impedindo que eles gerenciem os usuários da própria instituição.

### Requisitos Funcionais
1. ✅ Gestor deve ver menu "Usuários" (renomeado para "Gestão")
2. ✅ Gestor deve listar apenas usuários da sua instituição ativa
3. ✅ Gestor pode alterar perfis dos usuários (exceto ADMIN)
4. ✅ Gestor pode ativar/desativar usuários
5. ✅ Gestor NÃO pode atribuir role ADMIN
6. ✅ Gestor NÃO pode acessar menu "Instituições"

## ✅ SOLUÇÃO IMPLEMENTADA

### 1. Nova Computed Property: `canManageUsers`

**Arquivo**: `frontend/src/stores/auth.store.ts`

Criada nova propriedade que retorna true se o usuário é ADMIN ou MANAGER na instituição ativa:

```typescript
const canManageUsers = computed(() => isAdmin.value || isManager.value)
```

### 2. Menu Sidebar Atualizado

**Arquivo**: `frontend/src/components/navigation/AppSidebar.vue`

**Mudanças**:
- Menu agora usa `canManageUsers` ao invés de apenas `isAdmin`
- Título do menu muda dinamicamente:
  - "Administração" para ADMIN
  - "Gestão" para MANAGER
- Itens do menu construídos dinamicamente:
  - "Usuários": visível para ADMIN e MANAGER
  - "Instituições": visível apenas para ADMIN
  - "Configurações": visível apenas para ADMIN

```typescript
const adminItems = computed(() => {
  const items = []

  // Usuários: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: 'Usuários',
      icon: 'mdi-account-group',
      to: '/admin/users',
    })
  }

  // Instituições e Configurações: apenas para ADMIN
  if (isAdmin.value) {
    items.push(
      {
        title: 'Instituições',
        icon: 'mdi-office-building',
        to: '/admin/institutions',
      },
      {
        title: 'Configurações',
        icon: 'mdi-cog',
        to: '/admin/settings',
      }
    )
  }

  return items
})
```

### 3. Rota Protegida com Novo Flag

**Arquivo**: `frontend/src/router/index.ts`

Rota `/admin/users` agora usa `requiresUserManagement` ao invés de `requiresAdmin`:

```typescript
{
  path: '/admin/users',
  name: 'admin-users',
  component: () => import('@/views/private/admin/UsersPage.vue'),
  meta: {
    layout: 'private',
    requiresAuth: true,
    requiresInstitution: true,
    requiresUserManagement: true, // ADMIN ou MANAGER
  },
}
```

### 4. Guard Atualizado

**Arquivo**: `frontend/src/router/guards.ts`

Adicionada validação para `requiresUserManagement`:

```typescript
const requiresUserManagement = to.meta.requiresUserManagement === true
if (requiresUserManagement) {
  // Aguardar carregamento de institutions
  if (authStore.institutions.length === 0) {
    await authStore.fetchUserInstitutions()
  }

  // Bloquear usuários PENDING
  if (authStore.isPending) {
    next({ name: 'profile' })
    return
  }

  // Verificar se pode gerenciar usuários (ADMIN ou MANAGER)
  if (!authStore.canManageUsers) {
    console.warn('[SECURITY] User cannot manage users, redirecting to dashboard')
    next({ name: 'dashboard' })
    return
  }

  console.log('[SECURITY] User management check passed for route:', to.name)
}
```

### 5. Composable Atualizado

**Arquivo**: `frontend/src/composables/useAuth.ts`

Exportadas as novas propriedades:

```typescript
const isManager = computed(() => authStore.isManager)
const canManageUsers = computed(() => authStore.canManageUsers)

return {
  // ... outros
  isManager,
  canManageUsers,
}
```

## 🔐 PERMISSÕES POR PERFIL

### ADMIN (na instituição SIMP-ADMIN ativa)
| Funcionalidade | Acesso |
|----------------|--------|
| Ver menu "Administração" | ✅ Sim |
| Listar todos os usuários | ✅ Sim |
| Filtrar por instituição | ✅ Sim |
| Gerenciar qualquer usuário | ✅ Sim |
| Atribuir role ADMIN | ✅ Sim (apenas em SIMP-ADMIN) |
| Atribuir MANAGER/VIEWER | ✅ Sim |
| Vincular usuários a instituições | ✅ Sim |
| Ver/editar instituições | ✅ Sim |

### MANAGER (na instituição ativa)
| Funcionalidade | Acesso |
|----------------|--------|
| Ver menu "Gestão" | ✅ Sim |
| Listar usuários da sua instituição | ✅ Sim |
| Filtrar por instituição | ❌ Não (filtro forçado) |
| Gerenciar usuários da sua instituição | ✅ Sim |
| Atribuir role ADMIN | ❌ Não (opção desabilitada) |
| Atribuir MANAGER/VIEWER | ✅ Sim |
| Vincular usuários a instituições | ❌ Não |
| Ver/editar instituições | ❌ Não |

### VIEWER (na instituição ativa)
| Funcionalidade | Acesso |
|----------------|--------|
| Ver menu "Gestão" | ❌ Não |
| Todas as funcionalidades administrativas | ❌ Não |

## 🎯 COMPORTAMENTO ESPERADO

### Cenário 1: Usuário com MANAGER na UFMS

**Setup**:
- Usuário vinculado à UFMS com role MANAGER
- Instituição ativa: UFMS

**Resultado**:
- ✅ Vê menu "Gestão" no sidebar
- ✅ Item "Usuários" visível
- ✅ Acessa `/admin/users`
- ✅ Vê apenas usuários da UFMS
- ✅ Pode editar usuários da UFMS
- ✅ Pode atribuir roles MANAGER e VIEWER
- ❌ NÃO vê opção de atribuir ADMIN
- ❌ NÃO vê menu "Instituições"

### Cenário 2: Usuário com ADMIN em SIMP-ADMIN e MANAGER na UFMS

**Setup**:
- Usuário vinculado a:
  - SIMP-ADMIN: role ADMIN
  - UFMS: role MANAGER
- Instituição ativa: **UFMS**

**Resultado**:
- ✅ `isAdmin` = false (UFMS não é SIMP-ADMIN)
- ✅ `isManager` = true
- ✅ `canManageUsers` = true
- ✅ Vê menu "Gestão" (não "Administração")
- ✅ Item "Usuários" visível
- ❌ NÃO vê "Instituições"
- ✅ Vê apenas usuários da UFMS

**Ao trocar para SIMP-ADMIN**:
- ✅ `isAdmin` = true
- ✅ `isManager` = false
- ✅ `canManageUsers` = true
- ✅ Vê menu "Administração"
- ✅ Vê "Usuários" e "Instituições"
- ✅ Vê todos os usuários
- ✅ Pode atribuir role ADMIN

## 🧪 TESTES DE VALIDAÇÃO

### Teste 1: Menu aparece para MANAGER

**Passos**:
1. Login com usuário MANAGER na UFMS
2. Selecionar UFMS como instituição ativa
3. Verificar sidebar

**Esperado**:
- ✅ Menu "Gestão" visível
- ✅ Item "Usuários" visível
- ❌ Item "Instituições" NÃO visível

**Console esperado**:
```
[DEBUG isAdmin] Active institution: UFMS
[DEBUG isAdmin] Final result: false
(isManager deve ser true)
(canManageUsers deve ser true)
```

### Teste 2: MANAGER vê apenas usuários da sua instituição

**Passos**:
1. Como MANAGER da UFMS
2. Acessar `/admin/users`
3. Verificar lista de usuários

**Esperado**:
- ✅ Lista mostra apenas usuários vinculados à UFMS
- ❌ Filtro de instituição NÃO aparece (forçado automaticamente)
- ✅ Backend retorna apenas usuários da UFMS

### Teste 3: MANAGER não pode atribuir ADMIN

**Passos**:
1. Como MANAGER da UFMS
2. Abrir diálogo de gerenciar papéis de um usuário
3. Tentar selecionar role ADMIN

**Esperado**:
- ✅ Chip "Administrador" está desabilitado
- ✅ Alerta aparece: "O papel de Administrador só pode ser atribuído na instituição SIMP-ADMIN"
- ✅ Chips MANAGER e VIEWER funcionam normalmente

### Teste 4: ADMIN em SIMP-ADMIN pode tudo

**Passos**:
1. Login com ADMIN
2. Selecionar SIMP-ADMIN
3. Acessar `/admin/users`

**Esperado**:
- ✅ Menu "Administração" visível
- ✅ "Usuários" e "Instituições" visíveis
- ✅ Vê todos os usuários de todas as instituições
- ✅ Filtro de instituição disponível
- ✅ Pode atribuir role ADMIN em usuários da SIMP-ADMIN

### Teste 5: Trocar instituição muda permissões

**Passos**:
1. Login com usuário que tem:
   - SIMP-ADMIN: ADMIN
   - UFMS: MANAGER
2. Selecionar SIMP-ADMIN
3. Verificar menu → deve mostrar "Administração" com Instituições
4. Trocar para UFMS
5. Verificar menu → deve mostrar "Gestão" sem Instituições

**Esperado**:
- ✅ Menu muda dinamicamente ao trocar instituição
- ✅ Permissões respeitam a instituição ativa

## 📊 LOGS ESPERADOS

### Console - MANAGER na UFMS:
```
[DEBUG isAdmin] Active institution: UFMS
[DEBUG isAdmin] Final result: false
[SECURITY] User management check passed for route: admin-users
```

### Console - ADMIN na SIMP-ADMIN:
```
[DEBUG isAdmin] Active institution: SIMP-ADMIN
[DEBUG isAdmin] Final result: true
[SECURITY] User management check passed for route: admin-users
```

## 📁 ARQUIVOS MODIFICADOS

```
✅ frontend/src/stores/auth.store.ts
   - Adicionado: isManager computed
   - Adicionado: canManageUsers computed

✅ frontend/src/composables/useAuth.ts
   - Exportado: isManager
   - Exportado: canManageUsers

✅ frontend/src/components/navigation/AppSidebar.vue
   - Menu usa canManageUsers ao invés de isAdmin
   - Título dinâmico: "Administração" ou "Gestão"
   - Itens construídos dinamicamente

✅ frontend/src/router/index.ts
   - Rota /admin/users: requiresUserManagement: true

✅ frontend/src/router/guards.ts
   - Adicionado: validação de requiresUserManagement
```

## ✅ BACKEND JÁ ESTAVA CORRETO

O backend já implementava corretamente a segregação:

1. **UserAdminService.listUsers()**:
   - Se não é admin, força filtro pela instituição do gestor
   - Gestor só vê usuários da própria instituição

2. **UserAdminService.updateUserRoles()**:
   - Valida que ADMIN só pode ser atribuído em SIMP-ADMIN
   - Gestor só pode atribuir roles na própria instituição

3. **AdminController.linkUserToInstitution()**:
   - Apenas ADMIN pode vincular usuários
   - Gestor NÃO tem acesso

## 🎉 RESUMO

Com essas mudanças:

1. ✅ GESTOR vê menu "Gestão" com opção "Usuários"
2. ✅ GESTOR pode gerenciar usuários da própria instituição
3. ✅ GESTOR pode atribuir roles MANAGER e VIEWER
4. ✅ GESTOR NÃO pode atribuir role ADMIN
5. ✅ GESTOR NÃO pode vincular usuários a instituições
6. ✅ ADMIN mantém acesso total ao sistema
7. ✅ Segregação multi-tenant respeitada
8. ✅ Permissões mudam dinamicamente ao trocar instituição

---

**Status**: ✅ IMPLEMENTADO
**Build**: ✅ PASSOU
**Testes**: ⏳ PENDENTE VALIDAÇÃO MANUAL
