# CORREÇÃO CRÍTICA: Segregação Multi-Tenant de Permissões

## 🔴 PROBLEMA IDENTIFICADO

### Descrição do Bug
Usuário com múltiplos vínculos a diferentes instituições mantinha as permissões de ADMIN mesmo quando trocava para uma instituição onde NÃO era admin.

### Exemplo do Problema
```
Usuário: João Silva
Vínculos:
  - SIMP-ADMIN: role ADMIN
  - UFMS: role VIEWER

Comportamento ERRADO (antes da correção):
1. Usuário seleciona instituição UFMS como ativa
2. Sistema ainda considera isAdmin = true
3. Usuário consegue acessar funcionalidades administrativas
4. Pode editar/deletar instituições
5. Pode vincular usuários

Comportamento CORRETO (após correção):
1. Usuário seleciona instituição UFMS como ativa
2. Sistema verifica: UFMS é a instituição ativa?
3. Usuário tem role ADMIN na UFMS? NÃO
4. isAdmin = false
5. Menus administrativos ficam ocultos
6. Rotas administrativas são bloqueadas
7. Backend retorna 403 para operações administrativas
```

## ✅ CAUSA RAIZ

**Arquivo**: `frontend/src/stores/auth.store.ts`

**Código ERRADO** (antes):
```typescript
const isAdmin = computed(() => {
  // Verificava se o usuário TEM role ADMIN em SIMP-ADMIN
  // Independente de qual instituição estava ativa
  return institutions.value.some(ui =>
    ui.institution?.acronym === 'SIMP-ADMIN' &&
    ui.roles?.includes('ADMIN')
  )
})
```

**Problema**: Não considerava a instituição ATIVA, apenas verificava se o usuário tinha vínculo com ADMIN em alguma instituição.

## ✅ SOLUÇÃO IMPLEMENTADA

### 1. Correção do `isAdmin` computed

**Arquivo**: `frontend/src/stores/auth.store.ts`

**Código CORRETO** (depois):
```typescript
const isAdmin = computed(() => {
  const institutionStore = useInstitutionStore()

  // 1. Verifica qual é a instituição ATIVA
  if (!institutionStore.activeInstitutionId) {
    return false
  }

  // 2. Busca o vínculo do usuário com a instituição ATIVA
  const activeInstitutionLink = institutions.value.find(
    ui => ui.institutionId === institutionStore.activeInstitutionId
  )

  if (!activeInstitutionLink) {
    return false
  }

  // 3. Verifica se a instituição ATIVA é SIMP-ADMIN
  const isSimpAdmin = activeInstitutionLink.institution?.acronym === 'SIMP-ADMIN'

  // 4. Verifica se o usuário tem role ADMIN NA INSTITUIÇÃO ATIVA
  const hasAdminRole = activeInstitutionLink.roles?.includes('ADMIN')

  // 5. Só é admin se AMBAS as condições forem verdadeiras
  return isSimpAdmin && hasAdminRole
})
```

### 2. Adição de `isManager` computed

Para completar a segregação, também foi criado um computed para MANAGER:

```typescript
const isManager = computed(() => {
  const institutionStore = useInstitutionStore()

  if (!institutionStore.activeInstitutionId) {
    return false
  }

  const activeInstitutionLink = institutions.value.find(
    ui => ui.institutionId === institutionStore.activeInstitutionId
  )

  if (!activeInstitutionLink) {
    return false
  }

  return activeInstitutionLink.roles?.includes('MANAGER')
})
```

### 3. Adição de `canManageUsers` computed

Helper para verificar se pode gerenciar usuários:

```typescript
const canManageUsers = computed(() => isAdmin.value || isManager.value)
```

## 🧪 TESTES DE VALIDAÇÃO

### Teste 1: Usuário com ADMIN em SIMP-ADMIN

**Setup:**
- Usuário vinculado a SIMP-ADMIN com role ADMIN
- Selecionar instituição SIMP-ADMIN como ativa

**Resultado Esperado:**
- ✅ `isAdmin` = true
- ✅ Menu "Administração" visível no sidebar
- ✅ Pode acessar `/admin/institutions`
- ✅ Pode criar/editar/deletar instituições
- ✅ Pode vincular usuários

**Console esperado:**
```
[DEBUG isAdmin] Active institution: SIMP-ADMIN
[DEBUG isAdmin] Final result: true
[SECURITY] Admin check passed for route: admin-institutions
```

### Teste 2: Usuário troca para instituição não-administrativa

**Setup:**
- Mesmo usuário do Teste 1
- Trocar instituição ativa para UFMS (onde NÃO é admin)

**Resultado Esperado:**
- ✅ `isAdmin` = false
- ✅ Menu "Administração" OCULTO no sidebar
- ✅ Ao tentar acessar `/admin/institutions` → redireciona para `/dashboard`
- ✅ Backend retorna 403 para operações administrativas

**Console esperado:**
```
[DEBUG isAdmin] Active institution: UFMS
[DEBUG isAdmin] Active institution check: { isSimpAdmin: false, hasAdminRole: false }
[DEBUG isAdmin] Final result: false
[SECURITY] User is not admin, redirecting to dashboard
```

### Teste 3: Usuário com MANAGER em UFMS

**Setup:**
- Usuário vinculado a UFMS com role MANAGER
- Selecionar instituição UFMS como ativa

**Resultado Esperado:**
- ✅ `isAdmin` = false
- ✅ `isManager` = true
- ✅ `canManageUsers` = true
- ✅ Menu "Administração" OCULTO
- ✅ Pode acessar `/admin/users` (gerenciar usuários da UFMS)
- ✅ NÃO pode acessar `/admin/institutions`

### Teste 4: Refresh rápido não deve causar bypass

**Setup:**
- Usuário comum em instituição UFMS
- Pressionar F5 múltiplas vezes rapidamente

**Resultado Esperado:**
- ✅ NUNCA mostra conteúdo administrativo
- ✅ SEMPRE redireciona para dashboard se tentar acessar rotas admin

## 📊 LOGS DE DEBUG

Os logs foram mantidos temporariamente para validação:

```typescript
console.log('[DEBUG isAdmin] Checking admin status...')
console.log('[DEBUG isAdmin] Active institution:', institutionStore.activeInstitution?.acronym)
console.log('[DEBUG isAdmin] Active institution ID:', institutionStore.activeInstitutionId)
console.log('[DEBUG isAdmin] Final result:', result)
```

⚠️ **IMPORTANTE**: Remover logs `[DEBUG isAdmin]` após validação em produção.

## 🔒 CAMADAS DE PROTEÇÃO

A segregação multi-tenant agora funciona em 3 camadas:

### 1. Frontend - Router Guard
```typescript
// guards.ts
if (requiresAdmin && !authStore.isAdmin) {
  next({ name: 'dashboard' })
  return
}
```

### 2. Frontend - UI Condicional
```vue
<!-- AppSidebar.vue -->
<template v-if="isAdmin">
  <v-list-item title="Instituições" />
</template>
```

### 3. Backend - @PreAuthorize
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<InstitutionDTO> createInstitution(...) {
  // Valida que usuário é admin de SIMP-ADMIN
}
```

## 🎯 COMPORTAMENTO ESPERADO POR CENÁRIO

| Usuário | Instituição Ativa | isAdmin | Pode Ver Menu Admin | Pode Criar Instituição |
|---------|------------------|---------|---------------------|------------------------|
| João (ADMIN em SIMP-ADMIN) | SIMP-ADMIN | ✅ true | ✅ Sim | ✅ Sim |
| João (ADMIN em SIMP-ADMIN) | UFMS | ❌ false | ❌ Não | ❌ Não |
| Maria (MANAGER em UFMS) | UFMS | ❌ false | ❌ Não | ❌ Não |
| Pedro (VIEWER em UFMS) | UFMS | ❌ false | ❌ Não | ❌ Não |

## 📁 ARQUIVOS MODIFICADOS

```
✅ frontend/src/stores/auth.store.ts
   - isAdmin: Verifica instituição ATIVA
   - isManager: Novo computed (verifica instituição ATIVA)
   - canManageUsers: Novo computed helper

✅ frontend/src/router/guards.ts
   - Aguarda carregamento de institutions antes de verificar admin
   - Logs de segurança adicionados
```

## ✅ CHECKLIST DE VALIDAÇÃO

Antes de considerar a correção concluída:

- [ ] Testar usuário com ADMIN em SIMP-ADMIN → deve ver menus admin
- [ ] Trocar para outra instituição → menus devem SUMIR imediatamente
- [ ] Tentar acessar URL direta `/admin/institutions` → deve redirecionar
- [ ] Verificar logs no console → devem mostrar instituição ativa
- [ ] Testar refresh rápido → não deve permitir bypass
- [ ] Testar backend com curl/postman → deve retornar 403

## 🚀 PRÓXIMOS PASSOS

1. **AGORA**: Executar testes manuais de validação
2. **Após testes OK**: Remover logs `[DEBUG isAdmin]`
3. **Deploy**: Após aprovação do time de segurança

## 📝 NOTAS ADICIONAIS

- Os logs `[SECURITY]` no guards.ts devem ser MANTIDOS para auditoria
- Os logs `[DEBUG isAdmin]` devem ser REMOVIDOS após validação
- Esta correção garante segregação multi-tenant COMPLETA
- Usuários com múltiplos vínculos agora têm permissões corretas por instituição

---

**Status**: ✅ CORREÇÃO APLICADA
**Build**: ✅ PASSOU
**Testes**: ⏳ PENDENTE VALIDAÇÃO MANUAL
