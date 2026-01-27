# ✅ CORREÇÃO: Gestor Pode Gerenciar Própria Instituição

## 📋 REQUISITOS IMPLEMENTADOS

### Para GESTOR (MANAGER):
1. ✅ Ver menu "Instituição" (singular) no sidebar
2. ✅ Acessar página de instituições
3. ✅ Visualizar APENAS a instituição ativa (uma linha)
4. ✅ Editar dados da própria instituição
5. ❌ NÃO pode criar novas instituições
6. ❌ NÃO pode deletar instituições
7. ✅ Botão "Gerenciar Usuários" redireciona para página de usuários

### Para ADMIN:
1. ✅ Ver menu "Instituições" (plural) no sidebar
2. ✅ Acessar página de instituições
3. ✅ Visualizar TODAS as instituições
4. ✅ Filtros disponíveis (busca, tipo, status)
5. ✅ Criar novas instituições
6. ✅ Editar qualquer instituição
7. ✅ Deletar instituições
8. ✅ Botão "Gerenciar Usuários" redireciona para página de usuários

## ✅ ALTERAÇÕES IMPLEMENTADAS

### 1. Menu Sidebar - Texto Dinâmico

**Arquivo**: `frontend/src/components/navigation/AppSidebar.vue`

O menu agora mostra texto diferente baseado no perfil:

```typescript
{
  title: isAdmin.value ? 'Instituições' : 'Instituição',
  icon: 'mdi-office-building',
  to: '/admin/institutions',
}
```

- **ADMIN**: "Instituições" (plural)
- **MANAGER**: "Instituição" (singular)

### 2. Rota Protegida para ADMIN e MANAGER

**Arquivo**: `frontend/src/router/index.ts`

```typescript
{
  path: '/admin/institutions',
  name: 'admin-institutions',
  meta: {
    requiresUserManagement: true, // ADMIN ou MANAGER
  },
}
```

Agora tanto ADMIN quanto MANAGER podem acessar.

### 3. InstitutionsPage - Comportamento Dinâmico

**Arquivo**: `frontend/src/views/private/admin/InstitutionsPage.vue`

**Título e Subtítulo Dinâmicos**:
```typescript
const pageTitle = computed(() =>
  isAdmin.value ? 'Instituições' : 'Instituição'
)

const pageSubtitle = computed(() =>
  isAdmin.value
    ? 'Gerencie as instituições do sistema'
    : 'Visualize e edite os dados da sua instituição'
)
```

**Botão "Nova Instituição" - Apenas para ADMIN**:
```vue
<v-btn
  v-if="isAdmin"
  @click="openCreateDialog"
>
  Nova Instituição
</v-btn>
```

**Filtragem Automática para MANAGER**:
```typescript
async function fetchInstitutions() {
  // Se não for admin, mostrar apenas a instituição ativa
  if (!isAdmin.value && institutionStore.activeInstitution) {
    institutions.value = [institutionStore.activeInstitution]
    totalInstitutions.value = 1
    return
  }

  // Admin vê todas as instituições
  const response = await institutionService.list(params)
  institutions.value = response.content
  totalInstitutions.value = response.totalElements
}
```

**Botão "Gerenciar Usuários" Redireciona**:
```typescript
function handleManageUsers() {
  router.push({ name: 'admin-users' })
}
```

### 4. InstitutionList - Controles Dinâmicos

**Arquivo**: `frontend/src/components/institution/InstitutionList.vue`

**Props Adicionadas**:
```typescript
interface Props {
  items: Institution[]
  totalItems: number
  loading?: boolean
  isAdmin?: boolean      // Nova prop
  showFilters?: boolean  // Nova prop
}
```

**Filtros - Apenas para ADMIN**:
```vue
<div v-if="showFilters" class="filters-section">
  <!-- filtros de busca, tipo, status -->
</div>
```

**Ações por Perfil**:
```vue
<template #item.actions="{ item }">
  <div class="d-flex">
    <!-- Editar: ADMIN e MANAGER -->
    <v-btn icon="mdi-pencil" @click="$emit('edit', item)" />

    <!-- Deletar: APENAS ADMIN -->
    <v-btn
      v-if="isAdmin"
      icon="mdi-delete"
      @click="$emit('delete', item)"
    />

    <!-- Gerenciar Usuários: ADMIN e MANAGER -->
    <v-btn
      icon="mdi-account-group"
      @click="$emit('manage-users', item)"
    />
  </div>
</template>
```

## 🎯 COMPORTAMENTO ESPERADO

### Cenário 1: ADMIN na SIMP-ADMIN

**Menu Sidebar**:
- ✅ "Administração"
- ✅ "Usuários"
- ✅ "Instituições" (plural)
- ✅ "Configurações"

**Página de Instituições**:
- ✅ Título: "Instituições"
- ✅ Subtítulo: "Gerencie as instituições do sistema"
- ✅ Botão "Nova Instituição" visível
- ✅ Filtros visíveis (busca, tipo, status)
- ✅ Lista todas as instituições
- ✅ Ações: Editar, Deletar, Gerenciar Usuários

### Cenário 2: MANAGER na UFMS

**Menu Sidebar**:
- ✅ "Gestão"
- ✅ "Usuários"
- ✅ "Instituição" (singular)

**Página de Instituição**:
- ✅ Título: "Instituição"
- ✅ Subtítulo: "Visualize e edite os dados da sua instituição"
- ❌ Botão "Nova Instituição" oculto
- ❌ Filtros ocultos
- ✅ Lista APENAS a UFMS (instituição ativa)
- ✅ Ações: Editar, Gerenciar Usuários
- ❌ Botão Deletar não aparece

### Cenário 3: Usuário com ADMIN em SIMP-ADMIN e MANAGER na UFMS

**Quando instituição ativa é SIMP-ADMIN**:
- ✅ Menu mostra "Administração" com "Instituições"
- ✅ Vê todas as instituições
- ✅ Pode criar, editar e deletar

**Quando troca para UFMS**:
- ✅ Menu muda para "Gestão" com "Instituição"
- ✅ Vê apenas UFMS
- ✅ Pode editar mas não deletar
- ❌ Não vê botão "Nova Instituição"

## 🧪 TESTES DE VALIDAÇÃO

### Teste 1: Menu Correto por Perfil

**Passos**:
1. Login como ADMIN
2. Selecionar SIMP-ADMIN
3. Verificar sidebar

**Esperado**:
- ✅ Menu "Administração"
- ✅ Item "Instituições" (plural)

**Passos**:
1. Login como MANAGER da UFMS
2. Selecionar UFMS
3. Verificar sidebar

**Esperado**:
- ✅ Menu "Gestão"
- ✅ Item "Instituição" (singular)

### Teste 2: MANAGER Vê Apenas Própria Instituição

**Passos**:
1. Como MANAGER da UFMS
2. Acessar `/admin/institutions`
3. Verificar tabela

**Esperado**:
- ✅ Tabela mostra 1 linha (UFMS)
- ❌ Filtros não aparecem
- ❌ Botão "Nova Instituição" não aparece
- ✅ Botão "Editar" aparece
- ❌ Botão "Deletar" não aparece

### Teste 3: MANAGER Pode Editar Própria Instituição

**Passos**:
1. Como MANAGER da UFMS
2. Acessar `/admin/institutions`
3. Clicar no botão "Editar" da UFMS
4. Alterar algum dado (ex: nome)
5. Salvar

**Esperado**:
- ✅ Dialog de edição abre
- ✅ Consegue salvar alterações
- ✅ Backend aceita (200 OK)

### Teste 4: MANAGER NÃO Pode Deletar

**Passos**:
1. Como MANAGER da UFMS
2. Acessar `/admin/institutions`
3. Verificar ações na tabela

**Esperado**:
- ❌ Botão "Deletar" não aparece
- ✅ Apenas botões "Editar" e "Gerenciar Usuários"

### Teste 5: ADMIN Vê Todas as Instituições

**Passos**:
1. Como ADMIN
2. Selecionar SIMP-ADMIN
3. Acessar `/admin/institutions`

**Esperado**:
- ✅ Vê múltiplas instituições (SIMP-ADMIN, UFMS, etc)
- ✅ Filtros disponíveis
- ✅ Botão "Nova Instituição" disponível
- ✅ Botões Editar, Deletar e Gerenciar Usuários

### Teste 6: Botão "Gerenciar Usuários" Funciona

**Passos**:
1. Como ADMIN ou MANAGER
2. Acessar `/admin/institutions`
3. Clicar no botão "Gerenciar Usuários"

**Esperado**:
- ✅ Redireciona para `/admin/users`

## 📊 COMPARAÇÃO ADMIN vs MANAGER

| Funcionalidade | ADMIN | MANAGER |
|----------------|-------|---------|
| **Menu** | "Administração" / "Instituições" | "Gestão" / "Instituição" |
| **Título da Página** | "Instituições" (plural) | "Instituição" (singular) |
| **Visualizar** | Todas as instituições | Apenas a ativa |
| **Filtros** | ✅ Sim | ❌ Não |
| **Criar Nova** | ✅ Sim | ❌ Não |
| **Editar** | ✅ Qualquer instituição | ✅ Própria instituição |
| **Deletar** | ✅ Sim | ❌ Não |
| **Gerenciar Usuários** | ✅ Sim | ✅ Sim |

## 📁 ARQUIVOS MODIFICADOS

```
✅ frontend/src/components/navigation/AppSidebar.vue
   - Texto dinâmico: "Instituições" vs "Instituição"

✅ frontend/src/router/index.ts
   - Rota /admin/institutions: requiresUserManagement

✅ frontend/src/views/private/admin/InstitutionsPage.vue
   - Título/subtítulo dinâmicos
   - Botão "Nova" apenas para admin
   - Filtragem automática para manager
   - Botão "Gerenciar Usuários" redireciona

✅ frontend/src/components/institution/InstitutionList.vue
   - Props: isAdmin, showFilters
   - Filtros condicionais
   - Botão deletar apenas para admin
```

## ✅ RESUMO

Com essas mudanças:

1. ✅ GESTOR vê menu "Instituição" (singular)
2. ✅ GESTOR pode acessar página de instituições
3. ✅ GESTOR vê apenas a instituição ativa (uma linha)
4. ✅ GESTOR pode editar dados da instituição
5. ✅ GESTOR NÃO vê botão "Nova Instituição"
6. ✅ GESTOR NÃO vê botão "Deletar"
7. ✅ GESTOR NÃO vê filtros (desnecessários)
8. ✅ Botão "Gerenciar Usuários" redireciona para página de usuários
9. ✅ ADMIN mantém acesso total
10. ✅ Segregação multi-tenant respeitada

---

**Status**: ✅ IMPLEMENTADO
**Build**: ✅ PASSOU
**Testes**: ⏳ PENDENTE VALIDAÇÃO MANUAL
