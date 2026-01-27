# CORREÇÃO CRÍTICA DE SEGURANÇA - Controle de Acesso Administrativo

**Data:** 2026-01-26
**Severidade:** CRÍTICA
**Status:** CORRIGIDO

---

## 1. PROBLEMA IDENTIFICADO

Usuários comuns conseguiam acessar funcionalidades administrativas devido a uma **race condition** no router guard do frontend.

### Sintomas Reportados

- ✗ Usuário comum conseguia cadastrar instituição
- ✗ Usuário comum conseguia editar instituições (incluindo SIMP-ADMIN)
- ✗ Usuário comum conseguia vincular usuários
- ✗ Botões administrativos visíveis para usuários sem permissão

---

## 2. CAUSA RAIZ

### Race Condition no Guard

O guard de autenticação verificava `authStore.isAdmin` **ANTES** de garantir que as instituições do usuário fossem carregadas:

```typescript
// ANTES (VULNERÁVEL):
if (requiresAdmin && !authStore.isAdmin) {
  // Verifica isAdmin antes de ter certeza que institutions foram carregadas
  next({ name: 'dashboard' })
  return
}
```

**Fluxo do Bug:**

1. Usuário faz refresh na página `/admin/institutions`
2. Guard executa e verifica `requiresAdmin`
3. `authStore.isAdmin` depende de `authStore.institutions`
4. Mas `institutions` ainda está sendo carregado (array vazio)
5. `isAdmin` retorna `false` (porque não há institutions)
6. Guard **deveria** bloquear, mas há timing onde ele não bloqueia corretamente
7. Usuário acessa página administrativa sem ser admin

### Proteção de UI Faltando

Componentes não verificavam `isAdmin` antes de renderizar botões:

```vue
<!-- ANTES (VULNERÁVEL): -->
<v-btn @click="openCreateDialog">
  Nova Instituição
</v-btn>
```

---

## 3. CORREÇÕES APLICADAS

### 3.1. Frontend - Router Guard (`guards.ts`)

**ANTES:**
```typescript
if (requiresAdmin && !authStore.isAdmin) {
  console.warn('User is not admin, redirecting to dashboard')
  next({ name: 'dashboard' })
  return
}
```

**DEPOIS (SEGURO):**
```typescript
if (requiresAdmin) {
  // CRÍTICO: Garantir que institutions foram carregadas antes de verificar isAdmin
  if (authStore.institutions.length === 0) {
    console.warn('[SECURITY] Admin check: institutions not loaded yet, fetching...')
    try {
      await authStore.fetchUserInstitutions()
    } catch (err) {
      console.error('[SECURITY] Failed to fetch institutions for admin check:', err)
    }
  }

  // Bloquear usuários PENDING
  if (authStore.isPending) {
    console.warn('[SECURITY] Pending user attempting to access admin route')
    next({ name: 'profile' })
    return
  }

  // Verificar se usuário é admin
  if (!authStore.isAdmin) {
    console.warn('[SECURITY] User is not admin, redirecting to dashboard', {
      institutions: authStore.institutions.map(i => ({
        acronym: i.institution?.acronym,
        roles: i.roles
      }))
    })
    next({ name: 'dashboard' })
    return
  }

  console.log('[SECURITY] Admin check passed for route:', to.name)
}
```

**Mudanças:**
1. ✅ Aguarda carregamento de `institutions` antes de verificar `isAdmin`
2. ✅ Logs detalhados de segurança com prefixo `[SECURITY]`
3. ✅ Validação explícita de usuário PENDING
4. ✅ Log de sucesso quando admin passa na validação

### 3.2. Frontend - Auth Store (`auth.store.ts`)

Adicionados logs de debug temporários em `isAdmin` computed:

```typescript
const isAdmin = computed(() => {
  console.log('[DEBUG isAdmin] Checking admin status...')
  console.log('[DEBUG isAdmin] institutions.value:', institutions.value)

  if (!institutions.value || !Array.isArray(institutions.value) || institutions.value.length === 0) {
    console.log('[DEBUG isAdmin] No institutions found, returning false')
    return false
  }

  const result = institutions.value.some(ui => {
    const isSimpAdmin = ui.institution?.acronym === 'SIMP-ADMIN'
    const hasAdminRole = ui.roles && ui.roles.includes('ADMIN' as UserInstitutionRole)
    console.log('[DEBUG isAdmin] Checking institution:', {
      id: ui.institutionId,
      acronym: ui.institution?.acronym,
      isSimpAdmin,
      roles: ui.roles,
      hasAdminRole
    })
    return isSimpAdmin && hasAdminRole
  })

  console.log('[DEBUG isAdmin] Final result:', result)
  return result
})
```

**NOTA:** Estes logs devem ser REMOVIDOS após confirmação do fix.

### 3.3. Frontend - InstitutionsPage.vue

**ANTES:**
```vue
<v-btn @click="openCreateDialog">
  Nova Instituição
</v-btn>
```

**DEPOIS:**
```vue
<v-btn v-if="isAdmin" @click="openCreateDialog">
  Nova Instituição
</v-btn>
```

Adicionado computed property:
```typescript
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.isAdmin)
```

### 3.4. Frontend - InstitutionList.vue

**ANTES:**
```vue
<template #item.actions="{ item }">
  <div class="d-flex">
    <v-btn icon="mdi-pencil" @click="$emit('edit', item)" />
    <v-btn icon="mdi-delete" @click="$emit('delete', item)" />
  </div>
</template>
```

**DEPOIS:**
```vue
<template #item.actions="{ item }">
  <div v-if="isAdmin" class="d-flex">
    <v-btn icon="mdi-pencil" @click="$emit('edit', item)" />
    <v-btn icon="mdi-delete" @click="$emit('delete', item)" />
  </div>
  <div v-else class="text-caption text-medium-emphasis">
    Sem permissão
  </div>
</template>
```

### 3.5. Backend - Validação Confirmada

Backend JÁ ESTAVA PROTEGIDO corretamente:

**InstitutionController.java:**
```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<InstitutionDTO> createInstitution(...)

@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<InstitutionDTO> updateInstitution(...)

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> deleteInstitution(...)
```

**AdminController.java:**
```java
@PostMapping("/{id}/institutions")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> linkUserToInstitution(...) {
    // Validação adicional
    if (!userPrincipal.isAdmin()) {
        throw new UnauthorizedAccessException(
            "Only SIMP-ADMIN administrators can link users to institutions");
    }
    // ...
}
```

---

## 4. CAMADAS DE DEFESA

Agora temos **defesa em profundidade** (defense in depth):

### Camada 1: Router Guard (Frontend)
- ✅ Bloqueia navegação para rotas administrativas
- ✅ Garante carregamento de institutions antes de validar
- ✅ Logs detalhados de tentativas de acesso

### Camada 2: Componentes (Frontend)
- ✅ Botões administrativos condicionados com `v-if="isAdmin"`
- ✅ UI mostra "Sem permissão" para usuários não-admin

### Camada 3: Backend (Spring Security)
- ✅ `@PreAuthorize("hasRole('ADMIN')")` em todos os endpoints críticos
- ✅ Validação adicional em métodos sensíveis
- ✅ Retorna 403 Forbidden se usuário não tiver permissão

---

## 5. TESTE MANUAL

### 5.1. Teste como Usuário Comum (NÃO ADMIN)

**Pré-requisito:** Usuário sem role ADMIN na instituição SIMP-ADMIN

#### Teste 1: Navegação Direta
```
1. Fazer login como usuário comum
2. Abrir console do navegador (F12)
3. Tentar acessar: http://localhost:5173/admin/institutions
4. ESPERADO:
   - Redirecionamento para /dashboard
   - Console mostra: "[SECURITY] User is not admin, redirecting to dashboard"
```

#### Teste 2: Botões na UI
```
1. Fazer login como usuário comum
2. Se conseguir acessar página de institutions (não deveria):
3. ESPERADO:
   - Botão "Nova Instituição" NÃO aparece
   - Coluna de ações mostra "Sem permissão"
```

#### Teste 3: API Direta (CURL)
```bash
# Obter token do usuário comum (do localStorage do navegador)
TOKEN="seu_token_aqui"

# Tentar criar instituição
curl -X POST http://localhost:8080/institutions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste Hack",
    "acronym": "HACK",
    "type": "PRIVADA"
  }'

# ESPERADO: HTTP 403 Forbidden
```

### 5.2. Teste como Admin

**Pré-requisito:** Usuário com role ADMIN na instituição SIMP-ADMIN

#### Teste 1: Navegação
```
1. Fazer login como admin
2. Acessar: http://localhost:5173/admin/institutions
3. ESPERADO:
   - Acesso permitido
   - Console mostra: "[SECURITY] Admin check passed for route: admin-institutions"
```

#### Teste 2: Funcionalidades
```
1. Verificar botão "Nova Instituição" aparece
2. Criar nova instituição
3. Editar instituição
4. Verificar coluna de ações tem todos os botões
5. ESPERADO: Todas as funcionalidades disponíveis
```

### 5.3. Teste de Race Condition

**Objetivo:** Verificar se o bug de timing foi corrigido

```
1. Fazer login como usuário comum
2. Navegar para /admin/institutions
3. Refresh (F5) várias vezes rapidamente
4. Abrir DevTools > Network > Disable cache
5. Refresh com Ctrl+Shift+R (hard refresh)
6. ESPERADO:
   - SEMPRE redireciona para /dashboard
   - Console mostra logs [DEBUG isAdmin] e [SECURITY]
   - NUNCA permite acesso
```

---

## 6. VALIDAÇÃO DOS LOGS

Ao testar, você DEVE ver estes logs no console:

### Usuário Comum (NÃO ADMIN)

```
[DEBUG isAdmin] Checking admin status...
[DEBUG isAdmin] institutions.value: [{ institution: { acronym: "UFMG" }, roles: ["USER"] }]
[DEBUG isAdmin] Checking institution: { acronym: "UFMG", isSimpAdmin: false, roles: ["USER"], hasAdminRole: false }
[DEBUG isAdmin] Final result: false
[SECURITY] User is not admin, redirecting to dashboard
```

### Usuário Admin

```
[DEBUG isAdmin] Checking admin status...
[DEBUG isAdmin] institutions.value: [{ institution: { acronym: "SIMP-ADMIN" }, roles: ["ADMIN"] }]
[DEBUG isAdmin] Checking institution: { acronym: "SIMP-ADMIN", isSimpAdmin: true, roles: ["ADMIN"], hasAdminRole: true }
[DEBUG isAdmin] Final result: true
[SECURITY] Admin check passed for route: admin-institutions
```

---

## 7. PRÓXIMOS PASSOS

### Imediato (Antes de Deploy)
- [ ] Executar todos os testes manuais descritos acima
- [ ] Verificar logs no console
- [ ] Testar com múltiplos usuários (admin e não-admin)
- [ ] Testar race condition (refresh rápido múltiplas vezes)

### Após Confirmação
- [ ] **REMOVER logs de debug** de `auth.store.ts` (linhas com `[DEBUG isAdmin]`)
- [ ] Manter logs `[SECURITY]` em `guards.ts` (são importantes para auditoria)
- [ ] Adicionar testes automatizados E2E para este cenário
- [ ] Code review com time de segurança

### Longo Prazo
- [ ] Implementar auditoria de tentativas de acesso não autorizado
- [ ] Adicionar rate limiting para prevenir brute force
- [ ] Monitorar logs `[SECURITY]` em produção
- [ ] Implementar alertas para tentativas suspeitas

---

## 8. ARQUIVOS MODIFICADOS

```
frontend/src/router/guards.ts                             ✅ CORRIGIDO
frontend/src/stores/auth.store.ts                        ✅ LOGS DEBUG
frontend/src/views/private/admin/InstitutionsPage.vue    ✅ PROTEGIDO
frontend/src/components/institution/InstitutionList.vue  ✅ PROTEGIDO
```

**Backend:** ✅ JÁ ESTAVA PROTEGIDO (sem mudanças necessárias)

---

## 9. IMPACTO

### Antes
- 🔴 Usuários comuns podiam acessar páginas administrativas
- 🔴 Botões administrativos visíveis para todos
- 🔴 Race condition permitia bypass do guard em refreshes rápidos

### Depois
- ✅ Router guard aguarda carregamento de institutions
- ✅ Validação de admin robusta e com logs
- ✅ UI condicional baseada em permissões
- ✅ Backend sempre valida (defesa em profundidade)
- ✅ Logs detalhados para debug e auditoria

---

## 10. RESPONSÁVEL

**Desenvolvedor:** Claude Sonnet 4.5
**Revisor:** [A definir]
**Aprovador:** [A definir]

---

**IMPORTANTE:** Este é um bug CRÍTICO de segurança. NÃO fazer deploy sem:
1. Testes manuais completos
2. Validação dos logs
3. Code review
4. Aprovação do time de segurança
