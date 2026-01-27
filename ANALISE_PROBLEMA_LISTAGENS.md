# Análise: Problema nas Listagens

## 🔴 Problema Reportado

As listagens de usuários e instituições pararam de funcionar corretamente após as alterações de segurança.

---

## 🔍 Investigação

### Alterações Recentes que Podem Ter Causado Impacto

#### 1. AdminController - Métodos Alterados

**ANTES:**
```java
public void unlinkUserFromInstitution(UUID userId, UUID institutionId)
```

**DEPOIS:**
```java
public void unlinkUserFromInstitution(UUID userId, UUID institutionId,
                                      UUID requestingUserId, boolean isAdmin)
```

**Impacto:** ❌ NENHUM nos endpoints de listagem (GET)
- Apenas métodos POST/DELETE foram alterados
- GET /admin/users NÃO foi modificado
- GET /institutions NÃO foi modificado

#### 2. Validações Adicionadas no Controller

**Novo código:**
```java
// Em linkUserToInstitution e unlinkUserFromInstitution
if (!userPrincipal.isAdmin()) {
    throw new UnauthorizedAccessException(...);
}
```

**Impacto:** ❌ NENHUM nos endpoints de listagem
- Validações apenas em endpoints de vínculo/desvínculo
- Listagens não foram afetadas

#### 3. UserAdminService - Validações SIMP-ADMIN

**Novo código em linkUserToInstitution:**
```java
if (InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
    if (!isAdmin) {
        throw new UnauthorizedAccessException(...);
    }
}
```

**Impacto:** ❌ NENHUM nos métodos de listagem
- Validações apenas em métodos de link/unlink
- `listUsers()` NÃO foi modificado
- `getUserById()` NÃO foi modificado

---

## 📊 Endpoints de Listagem - Status Atual

### GET /admin/users
**Código:**
```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public ResponseEntity<PagedResponseDTO<UserListDTO>> listUsers(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false) UUID institutionId,
        @RequestParam(required = false) InstitutionRole role,
        @PageableDefault(size = 20, sort = "name") Pageable pageable,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Page<UserListDTO> users = userAdminService.listUsers(
        search,
        status,
        institutionId,
        role,
        pageable,
        userPrincipal.getId(),
        userPrincipal.isAdmin(),
        userPrincipal.getCurrentInstitutionId()
    );

    return ResponseEntity.ok(PagedResponseDTO.fromPage(users));
}
```

**Status:** ✅ **NÃO FOI ALTERADO**

### GET /institutions
**Controller:** `InstitutionController`
**Status:** ✅ **NÃO FOI ALTERADO**

---

## 🤔 Hipóteses do Problema

### Hipótese 1: Problema no Frontend
- ❓ Filtro de SIMP-ADMIN pode estar escondendo instituições incorretamente?
- ❓ Lógica de `isAdmin` no frontend pode estar falhando?
- ❓ Instituição ativa não está sendo selecionada?

### Hipótese 2: Problema de Autenticação/Autorização
- ❓ Token JWT pode ter expirado?
- ❓ `UserPrincipal.isAdmin()` pode estar retornando valor incorreto?
- ❓ Instituição ativa não está sendo setada no interceptor?

### Hipótese 3: Dados no Banco
- ❓ Migration pode ter falhado?
- ❓ Dados de instituições/usuários podem ter sido corrompidos?

### Hipótese 4: Erro de Compilação/Build
- ❓ Backend pode não estar com código atualizado?
- ❓ Frontend pode estar cacheado?

---

## ✅ Próximos Passos para Diagnóstico

1. **Verificar Console do Browser**
   - Abrir DevTools (F12)
   - Verificar chamadas de API (Network tab)
   - Ver se há erros 403/500
   - Verificar resposta das APIs

2. **Verificar Logs do Backend**
   - Procurar por erros ou exceptions
   - Verificar se requisições estão chegando
   - Ver se há problemas de autorização

3. **Testar Endpoints Diretamente**
   ```bash
   # Testar listagem de usuários
   curl -H "Authorization: Bearer TOKEN" \
        -H "X-Institution-Id: INSTITUTION_ID" \
        http://localhost:8080/admin/users

   # Testar listagem de instituições
   curl -H "Authorization: Bearer TOKEN" \
        http://localhost:8080/institutions
   ```

4. **Verificar Estado da Aplicação**
   - Login funciona?
   - Token está válido?
   - Instituição está selecionada?
   - Permissões estão corretas?

---

## 🛠️ Possíveis Soluções

### Se problema for no Frontend:

1. **Limpar cache do browser** (Ctrl+Shift+Del)
2. **Rebuild do frontend** (`npm run build`)
3. **Verificar lógica de isAdmin** no `auth.store.ts`
4. **Verificar filtro de instituições** no `UserInstitutionsDialog.vue`

### Se problema for no Backend:

1. **Rebuild do backend** (`mvn clean install`)
2. **Reiniciar aplicação**
3. **Verificar migrations** executadas corretamente
4. **Verificar logs** para erros específicos

### Se problema for de Dados:

1. **Verificar instituição SIMP-ADMIN existe** (query no banco)
2. **Verificar vínculos usuário-instituição** estão corretos
3. **Verificar roles** estão atribuídos corretamente

---

## 📝 Checklist de Verificação

- [ ] Console do browser não mostra erros
- [ ] Network tab mostra requisições bem-sucedidas (200 OK)
- [ ] Backend logs não mostram exceptions
- [ ] Token JWT está válido
- [ ] Instituição está selecionada no header X-Institution-Id
- [ ] Usuário tem permissão (ADMIN ou MANAGER)
- [ ] Dados existem no banco
- [ ] Backend e frontend estão atualizados

---

## ⚠️ CONCLUSÃO PRELIMINAR

**As alterações realizadas NÃO deveriam ter afetado as listagens**, pois:
- Apenas endpoints de vínculo (POST/DELETE) foram modificados
- Endpoints de listagem (GET) permaneceram intactos
- Validações novas estão isoladas em métodos específicos

**O problema pode estar em:**
1. Estado da aplicação (cache, build)
2. Contexto de autenticação (token, instituição ativa)
3. Problema não relacionado às alterações recentes

**Recomendação:** Fazer diagnóstico completo seguindo os passos acima antes de modificar mais código.
