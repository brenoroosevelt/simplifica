# Correção Crítica de Segurança Multi-Tenant

## 🔴 Vulnerabilidade Identificada

### Problema
Usuários GESTOR de instituições normais podiam:
1. Vincular usuários à instituição SIMP-ADMIN (administradora do sistema)
2. Atualizar papéis de usuários na instituição SIMP-ADMIN
3. Desvincular usuários da instituição SIMP-ADMIN
4. Ver a instituição SIMP-ADMIN na lista de instituições disponíveis

**Impacto:** Qualquer GESTOR poderia elevar privilégios e obter acesso de ADMIN do sistema inteiro.

---

## ✅ Correções Implementadas

### Backend

#### 1. `UserAdminService.linkUserToInstitution()` (linhas 323-362)
**Antes:**
```java
public void linkUserToInstitution(UUID userId, LinkUserInstitutionRequest request,
                                  UUID linkedByUserId)
```

**Depois:**
```java
public void linkUserToInstitution(UUID userId, LinkUserInstitutionRequest request,
                                  UUID linkedByUserId, boolean isAdmin)
```

**Validação adicionada:**
```java
// SECURITY: Only ADMIN can link users to SIMP-ADMIN institution
if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
    if (!isAdmin) {
        throw new UnauthorizedAccessException(
            "Only system administrators can link users to the SIMP-ADMIN institution");
    }
}
```

#### 2. `UserAdminService.updateUserRoles()` (linhas 263-321)
**Validação adicionada:**
```java
// SECURITY: Only ADMIN can update roles in SIMP-ADMIN institution
if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
    if (!isAdmin) {
        throw new UnauthorizedAccessException(
            "Only system administrators can update roles in the SIMP-ADMIN institution");
    }
}
```

#### 3. `UserAdminService.unlinkUserFromInstitution()` (linhas 402-438)
**Antes:**
```java
public void unlinkUserFromInstitution(UUID userId, UUID institutionId)
```

**Depois:**
```java
public void unlinkUserFromInstitution(UUID userId, UUID institutionId,
                                      UUID requestingUserId, boolean isAdmin)
```

**Validação adicionada:**
```java
// SECURITY: Only ADMIN can unlink users from SIMP-ADMIN institution
if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
    if (!isAdmin) {
        throw new UnauthorizedAccessException(
            "Only system administrators can unlink users from the SIMP-ADMIN institution");
    }
}
```

#### 4. `AdminController` - Atualizado para passar `isAdmin`
- `linkUserToInstitution()` - linha 205-210
- `unlinkUserFromInstitution()` - linha 240-247

### Frontend

#### 1. `UserInstitutionsDialog.vue`
**Filtro de instituições:**
```typescript
const availableInstitutions = computed(() => {
  return allInstitutions.value
    .filter((inst) => !linkedInstitutionIds.value.includes(inst.id))
    .filter((inst) => {
      // SECURITY: SIMP-ADMIN only visible to system administrators
      if (inst.acronym === 'SIMP-ADMIN') {
        return props.isAdmin
      }
      return true
    })
    .map((inst) => ({
      title: inst.name,
      value: inst.id,
    }))
})
```

**Props adicionados:**
- `isAdmin?: boolean` - indica se o usuário logado é ADMIN do sistema

#### 2. `UsersPage.vue`
**Passagem da prop:**
```vue
<UserInstitutionsDialog
  :is-admin="isAdmin"
  ...
/>
```

---

## 🔒 Regras de Segurança Implementadas

### 1. Controle de Acesso à Instituição SIMP-ADMIN
| Operação | ADMIN | GESTOR |
|----------|-------|--------|
| Ver SIMP-ADMIN na lista | ✅ Sim | ❌ Não |
| Vincular usuário à SIMP-ADMIN | ✅ Sim | ❌ Não |
| Atualizar papéis na SIMP-ADMIN | ✅ Sim | ❌ Não |
| Desvincular da SIMP-ADMIN | ✅ Sim | ❌ Não |

### 2. Atribuição de Role ADMIN
- Role ADMIN **só pode** ser atribuído na instituição SIMP-ADMIN
- Validação no backend impede atribuição de ADMIN em outras instituições
- Frontend oculta opção ADMIN para instituições que não sejam SIMP-ADMIN

### 3. Auditoria
- Todas as operações de vínculo/desvínculo são auditadas
- Log inclui quem executou a ação e qual instituição foi afetada

---

## 🧪 Cenários de Teste

### Cenário 1: GESTOR tenta vincular usuário à SIMP-ADMIN
**Dado:** Usuário logado com role MANAGER em instituição "Empresa X"
**Quando:** Tenta chamar POST `/admin/users/{userId}/institutions` com `institutionId` = SIMP-ADMIN
**Então:** Deve retornar **403 Forbidden** com mensagem de erro

### Cenário 2: GESTOR não vê SIMP-ADMIN na lista
**Dado:** Usuário logado com role MANAGER em instituição "Empresa X"
**Quando:** Abre o diálogo de gerenciar instituições
**Então:** SIMP-ADMIN **não aparece** na lista de instituições disponíveis

### Cenário 3: ADMIN pode gerenciar SIMP-ADMIN
**Dado:** Usuário logado com role ADMIN na instituição SIMP-ADMIN
**Quando:** Abre o diálogo de gerenciar instituições
**Então:** SIMP-ADMIN **aparece** na lista e todas as operações são permitidas

### Cenário 4: Tentativa de atualizar roles na SIMP-ADMIN por GESTOR
**Dado:** Usuário logado com role MANAGER em instituição "Empresa X"
**Quando:** Tenta chamar PUT `/admin/users/{userId}/roles` com `institutionId` = SIMP-ADMIN
**Então:** Deve retornar **403 Forbidden** com mensagem de erro

---

## 📝 Checklist de Validação

- [x] Backend valida permissão ADMIN para vincular à SIMP-ADMIN
- [x] Backend valida permissão ADMIN para atualizar papéis na SIMP-ADMIN
- [x] Backend valida permissão ADMIN para desvincular da SIMP-ADMIN
- [x] Frontend oculta SIMP-ADMIN de usuários não-ADMIN
- [x] Frontend passa flag `isAdmin` corretamente
- [x] Auditoria registra todas as operações
- [x] Mensagens de erro são claras e informativas
- [ ] Testes automatizados criados para validar cenários
- [ ] Documentação atualizada

---

## 🔍 Arquivos Alterados

### Backend
- `backend/src/main/java/com/simplifica/application/service/UserAdminService.java`
- `backend/src/main/java/com/simplifica/presentation/controller/AdminController.java`

### Frontend
- `frontend/src/components/user/UserInstitutionsDialog.vue`
- `frontend/src/views/private/admin/UsersPage.vue`

---

## ⚠️ Notas Importantes

1. **Instituição SIMP-ADMIN é especial**: Criada pela migration V4, é a única que pode ter usuários com role ADMIN do sistema.

2. **Multi-Tenancy robusto**: Agora GESTORs só podem gerenciar suas próprias instituições, sem risco de escalação de privilégios.

3. **Defense in Depth**: Validações tanto no frontend (UX) quanto no backend (segurança).

4. **Auditoria completa**: Todas as tentativas de acesso não autorizado são logadas.

---

## 🚀 Próximos Passos

1. Criar testes automatizados (JUnit) para validar cenários de segurança
2. Executar testes de penetração simulando tentativas de escalação de privilégio
3. Revisar outros endpoints para garantir que seguem o mesmo padrão de segurança
4. Documentar fluxo de permissões para novos desenvolvedores
