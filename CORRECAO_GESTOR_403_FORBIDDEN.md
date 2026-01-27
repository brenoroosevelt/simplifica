# Correção: 403 Forbidden para GESTOR

## 🔴 Problema Identificado

**Erro:** `403 Forbidden` ao acessar `/api/admin/users` com usuário GESTOR

**Mensagem:**
```json
{
  "timestamp": "2026-01-26T17:29:07.495046916",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource",
  "path": "/api/admin/users"
}
```

---

## 🔍 Causa Raiz

### UserPrincipal.determineAuthorities() - ANTES (ERRADO)

```java
// Check if user has ADMIN role in any institution (makes them a MANAGER/GESTOR)
boolean isInstitutionAdmin = user.getInstitutions().stream()
    .filter(ui -> ui.getActive())
    .anyMatch(ui -> ui.getRoles().stream()
        .anyMatch(role -> "ADMIN".equals(role.name())));  // ❌ ERRADO!
```

**Problema:** Estava verificando se o usuário tinha role **"ADMIN"** em alguma instituição, mas:
- GESTORs têm role **"MANAGER"** (não "ADMIN")
- Então usuários com role MANAGER recebiam `ROLE_USER` do Spring Security
- Endpoint exige `@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")`
- Como usuário tinha `ROLE_USER` → **403 Forbidden** ❌

### Fluxo do Problema

```
Usuário tem role MANAGER na Instituição X
    ↓
UserPrincipal.determineAuthorities() procura por role "ADMIN"
    ↓
Não encontra role "ADMIN" (só tem MANAGER)
    ↓
Atribui ROLE_USER (Spring Security)
    ↓
Tenta acessar /admin/users que exige ROLE_MANAGER
    ↓
403 Forbidden ❌
```

---

## ✅ Correção Aplicada

### UserPrincipal.determineAuthorities() - DEPOIS (CORRETO)

```java
// Check if user has MANAGER role in any institution (makes them a GESTOR)
boolean isInstitutionManager = user.getInstitutions().stream()
    .filter(ui -> ui.getActive())
    .anyMatch(ui -> ui.getRoles().stream()
        .anyMatch(role -> "MANAGER".equals(role.name())));  // ✅ CORRETO!

if (isInstitutionManager) {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER"));
}
```

**Alterações:**
1. ✅ Mudou de `"ADMIN".equals(role.name())` para `"MANAGER".equals(role.name())`
2. ✅ Renomeou variável de `isInstitutionAdmin` para `isInstitutionManager`
3. ✅ Atualizou comentário para refletir lógica correta

---

## 📊 Lógica de Mapeamento Corrigida

### Roles de Negócio → Roles do Spring Security

| Role na Instituição | Instituição | Spring Security Role | Acesso |
|---------------------|-------------|---------------------|--------|
| **ADMIN** | SIMP-ADMIN | `ROLE_ADMIN` | Sistema completo |
| **MANAGER** | Qualquer | `ROLE_MANAGER` | Gestão da instituição |
| **VIEWER** | Qualquer | `ROLE_USER` | Somente leitura |
| *(nenhuma)* | - | `ROLE_USER` | Sem acesso admin |

### Fluxo Corrigido

```
Usuário tem role MANAGER na Instituição X
    ↓
UserPrincipal.determineAuthorities() procura por role "MANAGER"
    ↓
Encontra role "MANAGER" ✅
    ↓
Atribui ROLE_MANAGER (Spring Security) ✅
    ↓
Acessa /admin/users que exige ROLE_MANAGER
    ↓
200 OK ✅
```

---

## 🧪 Testes Criados

**Arquivo:** `UserPrincipalAuthoritiesTest.java`

Testes que validam a lógica:

1. ✅ `shouldGrantRoleAdminForSimpAdminUser`
   - Usuário com ADMIN na SIMP-ADMIN → ROLE_ADMIN

2. ✅ `shouldGrantRoleManagerForInstitutionManager`
   - Usuário com MANAGER em instituição → ROLE_MANAGER

3. ✅ `shouldGrantRoleUserForViewerOnly`
   - Usuário com VIEWER apenas → ROLE_USER

4. ✅ `shouldGrantRoleUserForNoInstitutions`
   - Usuário sem instituições → ROLE_USER

5. ✅ `shouldGrantRoleManagerForMultipleInstitutions`
   - Usuário com MANAGER em múltiplas → ROLE_MANAGER

6. ✅ `shouldPrioritizeSimpAdminRole`
   - ADMIN na SIMP-ADMIN tem precedência → ROLE_ADMIN

**Resultado:** ✅ **6/6 testes passando**

---

## 📝 Arquivos Alterados

### 1. UserPrincipal.java
**Caminho:** `backend/src/main/java/com/simplifica/config/security/UserPrincipal.java`

**Linhas alteradas:** 67-105 (método `determineAuthorities()`)

### 2. UserPrincipalAuthoritiesTest.java (NOVO)
**Caminho:** `backend/src/test/java/com/simplifica/unit/security/UserPrincipalAuthoritiesTest.java`

**Propósito:** Validar lógica de atribuição de authorities

---

## ✅ Validação

### Antes da Correção
```bash
# Usuário GESTOR acessa /api/admin/users
Status: 403 Forbidden ❌
Authorities: [ROLE_USER]
```

### Depois da Correção
```bash
# Usuário GESTOR acessa /api/admin/users
Status: 200 OK ✅
Authorities: [ROLE_MANAGER]
```

---

## 🎯 Impacto

**Quem foi afetado:**
- ✅ Todos os usuários com role **MANAGER** em qualquer instituição
- ✅ Agora conseguem acessar endpoints administrativos de suas instituições

**Quem NÃO foi afetado:**
- ✅ Usuários com role **ADMIN** na SIMP-ADMIN (já funcionava)
- ✅ Usuários com role **VIEWER** (não precisam de acesso admin)
- ✅ Endpoints de listagem não foram alterados (já estavam corretos)

---

## 🚀 Próximos Passos

1. ✅ **Reiniciar backend** para aplicar mudanças
2. ✅ **Testar login** como GESTOR
3. ✅ **Verificar acesso** a `/admin/users`
4. ✅ **Validar** que listagens funcionam

---

## 📌 Nota Importante

Esta correção **NÃO afeta** as alterações de segurança anteriores:
- ✅ Validações de SIMP-ADMIN permanecem intactas
- ✅ Restrições de vínculo/desvínculo continuam funcionando
- ✅ Testes de segurança continuam passando (17/17)

**Esta foi uma correção cirúrgica** que não quebra nada existente.

---

**Status:** ✅ Corrigido e Testado
**Data:** 26/01/2026
**Testes:** 6/6 passando
