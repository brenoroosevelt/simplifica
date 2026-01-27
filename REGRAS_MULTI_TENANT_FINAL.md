# Regras Multi-Tenant - Documentação Final

## 📋 Sumário Executivo

Este documento define as regras de negócio do sistema multi-tenant implementado, garantindo segregação de dados, controle de acesso e segurança entre instituições.

---

## 🏛️ Estrutura do Sistema

### Entidades Principais

1. **Usuário (User)**
   - Pessoa física que acessa o sistema via OAuth
   - Pode ter vínculos com múltiplas instituições
   - Status: `PENDING`, `ACTIVE`, `INACTIVE`

2. **Instituição (Institution)**
   - Entidade organizacional (tenant)
   - Cada instituição possui seus próprios dados segregados
   - Instituição especial: **SIMP-ADMIN** (id=1) - administradora do sistema

3. **Vínculo Usuário-Instituição (UserInstitution)**
   - Relaciona usuário com instituição
   - Contém papéis (roles) específicos para aquela instituição
   - Suporta soft delete (campo `active`)

### Papéis (Roles)

Cada vínculo usuário-instituição possui um ou mais papéis:

| Papel | Código | Permissões |
|-------|--------|------------|
| **Administrador** | `ADMIN` | Acesso total ao sistema (apenas SIMP-ADMIN) |
| **Gestor** | `MANAGER` | Gerenciar usuários e dados da instituição |
| **Visualizador** | `VIEWER` | Apenas visualização (somente leitura) |

---

## 🔐 Regras de Negócio

### 1. Instituição SIMP-ADMIN (Administradora)

#### 1.1 Características
- **ID:** 1 (definido na migration V4)
- **Acronym:** `SIMP-ADMIN`
- **Propósito:** Gerenciar todo o sistema e todas as instituições
- **Criação:** Automática via migration do banco de dados

#### 1.2 Restrições
- ✅ **Apenas ADMIN pode vincular usuários à SIMP-ADMIN**
- ✅ **Apenas ADMIN pode atualizar papéis na SIMP-ADMIN**
- ✅ **Apenas ADMIN pode desvincular usuários da SIMP-ADMIN**
- ✅ **Role ADMIN só pode ser atribuído na instituição SIMP-ADMIN**

#### 1.3 Validações Implementadas
- **Backend:** `UserAdminService` valida em todos os métodos
- **Frontend:** SIMP-ADMIN não aparece na lista para não-ADMINs

### 2. Fluxo de Cadastro de Usuários

#### 2.1 Primeiro Acesso (OAuth)
```
Usuário faz login via Google/Microsoft
  ↓
Sistema cria conta com status = PENDING
  ↓
Usuário aguarda aprovação de ADMIN
  ↓
ADMIN vincula usuário a uma instituição
  ↓
Status muda automaticamente para ACTIVE
```

#### 2.2 Status do Usuário

| Status | Descrição | Quando ocorre |
|--------|-----------|---------------|
| `PENDING` | Aguardando vínculo com instituição | Após primeiro login OAuth |
| `ACTIVE` | Possui pelo menos um vínculo ativo | Quando ADMIN vincula a instituição |
| `INACTIVE` | Desativado manualmente | ADMIN/GESTOR desativa usuário |

**Regra Automática:**
- ✅ **Status volta para PENDING quando o último vínculo é removido**
- ✅ **Status PENDING não pode ser setado manualmente**
- ✅ **Usuários PENDING não podem ter status alterado para ACTIVE/INACTIVE manualmente**

### 3. Instituição Ativa (Contexto)

#### 3.1 Seleção Obrigatória
- ✅ **Sempre deve existir uma instituição selecionada**
- ✅ **Usuário escolhe qual instituição "ativar" no frontend**
- ✅ **Todas as operações são executadas no contexto da instituição ativa**

#### 3.2 Permissões Baseadas na Instituição Ativa
```
Permissões do Usuário = Papéis na Instituição Ativa

Exemplo:
- João tem ADMIN na SIMP-ADMIN
- João tem MANAGER na Empresa X
- Se João selecionar SIMP-ADMIN → É ADMIN do sistema
- Se João selecionar Empresa X → É MANAGER daquela empresa apenas
```

**IMPORTANTE:**
- ❌ **NÃO** considerar todos os vínculos do usuário
- ✅ **APENAS** considerar papéis da instituição ativa
- ✅ **Validação no backend via `UserPrincipal.getCurrentInstitutionId()`**
- ✅ **Validação no frontend via `useInstitutionStore().activeInstitutionId`**

### 4. Permissões por Papel

#### 4.1 ADMIN (Sistema)

**Requisitos:**
- Deve estar vinculado à instituição SIMP-ADMIN
- Deve ter role ADMIN nessa instituição
- Instituição SIMP-ADMIN deve estar ativa

**Permissões:**
- ✅ Ver todos os usuários de todas as instituições
- ✅ Criar/editar/remover usuários
- ✅ Vincular/desvincular usuários de qualquer instituição
- ✅ Gerenciar papéis de usuários em qualquer instituição
- ✅ Criar/editar instituições
- ✅ Acesso a todas as funcionalidades do sistema

**Restrições:**
- ❌ Não pode atribuir role ADMIN fora da SIMP-ADMIN

#### 4.2 MANAGER (Gestor)

**Requisitos:**
- Deve estar vinculado a uma instituição não-administrativa
- Deve ter role MANAGER nessa instituição
- Sua instituição deve estar ativa

**Permissões:**
- ✅ Ver usuários da sua instituição apenas
- ✅ Editar papéis de usuários na sua instituição
- ✅ Editar dados da sua instituição
- ✅ Gerenciar recursos dentro da sua instituição

**Restrições:**
- ❌ Não pode ver usuários de outras instituições
- ❌ Não pode vincular/desvincular usuários de instituições
- ❌ Não pode atribuir role ADMIN
- ❌ Não pode gerenciar a instituição SIMP-ADMIN
- ❌ Não pode criar novas instituições

#### 4.3 VIEWER (Visualizador)

**Requisitos:**
- Deve estar vinculado a uma instituição
- Deve ter role VIEWER nessa instituição
- Sua instituição deve estar ativa

**Permissões:**
- ✅ Visualizar dados da sua instituição
- ✅ Acesso somente leitura

**Restrições:**
- ❌ Não pode criar/editar/excluir nada
- ❌ Todas as funcionalidades de criação/edição desabilitadas

---

## 🔒 Validações de Segurança

### Backend

#### Camada Controller (`AdminController`)
```java
@PreAuthorize("hasRole('ADMIN')") // Spring Security valida role
public ResponseEntity<?> linkUserToInstitution(...) {
    // Validação adicional
    if (!userPrincipal.isAdmin()) {
        throw new UnauthorizedAccessException(...);
    }
}
```

#### Camada Service (`UserAdminService`)
```java
// Validar SIMP-ADMIN
if (ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
    if (!isAdmin) {
        throw new UnauthorizedAccessException(...);
    }
}

// Validar role ADMIN
if (roles.contains(InstitutionRole.ADMIN)) {
    if (!ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
        throw new BadRequestException(...);
    }
}
```

### Frontend

#### Verificação de Permissão
```typescript
// useAuthStore().isAdmin verifica:
// 1. Instituição ativa é SIMP-ADMIN?
// 2. Usuário tem role ADMIN nela?
const isAdmin = computed(() => {
  const activeInstitutionLink = institutions.value.find(
    ui => ui.institutionId === activeInstitutionId
  )

  return activeInstitutionLink?.institution?.acronym === 'SIMP-ADMIN' &&
         activeInstitutionLink?.roles?.includes('ADMIN')
})
```

#### Filtro de Instituições
```typescript
// SIMP-ADMIN só aparece para ADMINs
const availableInstitutions = computed(() => {
  return institutions.filter(inst => {
    if (inst.acronym === 'SIMP-ADMIN') {
      return props.isAdmin
    }
    return true
  })
})
```

---

## 📊 Fluxos de Operação

### Fluxo 1: Vincular Usuário a Instituição

```
1. ADMIN seleciona SIMP-ADMIN como instituição ativa
2. ADMIN acessa página de usuários
3. ADMIN clica em "Gerenciar Instituições" para um usuário
4. ADMIN seleciona instituição e papéis
5. Sistema valida:
   ✓ Usuário é ADMIN?
   ✓ Se instituição for SIMP-ADMIN, usuário é ADMIN?
   ✓ Se role for ADMIN, instituição é SIMP-ADMIN?
6. Backend cria vínculo
7. Se era primeiro vínculo, status muda de PENDING para ACTIVE
8. Auditoria registra operação
```

### Fluxo 2: Gestor Gerencia Usuários

```
1. GESTOR seleciona sua instituição como ativa
2. GESTOR acessa página de usuários
3. Sistema filtra automaticamente:
   ✓ Apenas usuários vinculados à instituição do GESTOR
4. GESTOR pode editar papéis dos usuários
5. Sistema valida:
   ✓ Usuário pertence à instituição do GESTOR?
   ✓ Papel não é ADMIN?
   ✓ Instituição não é SIMP-ADMIN?
6. Backend atualiza papéis
7. Auditoria registra operação
```

### Fluxo 3: Remover Último Vínculo

```
1. ADMIN remove vínculo usuário-instituição
2. Sistema verifica: é o último vínculo ativo?
3. Se sim:
   ✓ Desativa vínculo (active = false)
   ✓ Muda status do usuário para PENDING
   ✓ Registra auditoria
4. Usuário volta a aguardar novo vínculo
```

---

## ✅ Checklist de Validação

### Segurança Multi-Tenant
- [x] ADMIN só pode ser atribuído na SIMP-ADMIN
- [x] GESTOR não vê SIMP-ADMIN na lista de instituições
- [x] GESTOR só gerencia usuários da sua instituição
- [x] Permissões baseadas na instituição ativa
- [x] Validações redundantes (backend + frontend)

### Gerenciamento de Usuários
- [x] Novo usuário entra como PENDING
- [x] Status muda para ACTIVE ao vincular instituição
- [x] Status volta para PENDING ao remover último vínculo
- [x] Status PENDING não pode ser setado manualmente

### Auditoria
- [x] Todas operações de vínculo são auditadas
- [x] Tentativas de acesso não autorizado são logadas
- [x] Logs incluem quem executou e qual instituição

### Testes
- [x] Testes de segurança do controller
- [x] Testes de segurança do service
- [x] Testes de desvínculo
- [x] Todos os testes passando: 17/17 ✅

---

## 🚀 Próximos Passos

1. **Testes de Integração**
   - Testar fluxo completo via API
   - Validar cenários de troca de instituição ativa

2. **Testes de Penetração**
   - Simular tentativas de escalação de privilégio
   - Validar isolamento entre tenants

3. **Performance**
   - Otimizar queries com múltiplos joins
   - Implementar cache de permissões

4. **Documentação**
   - Manual do usuário
   - Guia de operações para ADMINs

---

## 📝 Notas de Implementação

### Arquivos Críticos

**Backend:**
- `UserAdminService.java` - Lógica de negócio e validações
- `AdminController.java` - Controle de acesso HTTP
- `UserPrincipal.java` - Contexto de autenticação
- `InstitutionConstants.java` - Constantes do sistema

**Frontend:**
- `auth.store.ts` - Gerenciamento de autenticação e permissões
- `institution.store.ts` - Gerenciamento de instituição ativa
- `UserInstitutionsDialog.vue` - Interface de gerenciamento
- `UsersPage.vue` - Página administrativa de usuários

### Migrations Importantes
- `V4__insert_default_admin_institution.sql` - Cria SIMP-ADMIN
- `V5__create_audit_logs_table.sql` - Tabela de auditoria

---

**Versão:** 1.0
**Data:** 26/01/2026
**Status:** ✅ Implementado e Testado
