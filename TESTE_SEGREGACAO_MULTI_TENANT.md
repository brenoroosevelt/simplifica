# 🧪 GUIA DE TESTE: Segregação Multi-Tenant

## ⚡ TESTE RÁPIDO (5 minutos)

### Pré-requisito
Ter um usuário com 2 vínculos:
- **SIMP-ADMIN**: role ADMIN
- **UFMS** (ou outra): role VIEWER ou MANAGER

---

## TESTE 1: Verificar que ADMIN funciona em SIMP-ADMIN

### Passos:
1. Fazer login
2. Selecionar instituição **SIMP-ADMIN** no dropdown do header
3. Abrir console do navegador (F12)
4. Verificar sidebar

### ✅ Resultado Esperado:
- Menu "Administração" VISÍVEL no sidebar
- Submenu "Instituições" visível
- Console mostra:
  ```
  [DEBUG isAdmin] Active institution: SIMP-ADMIN
  [DEBUG isAdmin] Final result: true
  ```

### ❌ Se não funcionar:
- Verificar se realmente tem role ADMIN na SIMP-ADMIN
- Verificar se a instituição ativa está SIMP-ADMIN
- Ver console para logs de erro

---

## TESTE 2: Verificar que ADMIN NÃO funciona em outra instituição

### Passos:
1. Clicar no dropdown de instituição no header
2. Selecionar **UFMS** (ou outra instituição que não seja SIMP-ADMIN)
3. Observar o sidebar

### ✅ Resultado Esperado:
- Menu "Administração" **DESAPARECE** imediatamente
- Console mostra:
  ```
  [DEBUG isAdmin] Active institution: UFMS
  [DEBUG isAdmin] Active institution check: { isSimpAdmin: false, ... }
  [DEBUG isAdmin] Final result: false
  ```
- Se estava em uma página administrativa, é redirecionado para /dashboard

### ❌ Se não funcionar:
- Limpar cache do navegador (Ctrl+Shift+Delete)
- Fazer logout e login novamente
- Verificar se o build foi feito após a correção

---

## TESTE 3: Tentar acessar rota administrativa diretamente

### Passos:
1. Garantir que instituição ativa é **UFMS** (não SIMP-ADMIN)
2. Na barra de endereços, digitar: `http://localhost:5173/admin/institutions`
3. Pressionar Enter

### ✅ Resultado Esperado:
- Redireciona para `/dashboard` imediatamente
- Console mostra:
  ```
  [SECURITY] User is not admin, redirecting to dashboard
  ```
- Não mostra conteúdo da página de instituições

### ❌ Se não funcionar:
- CRÍTICO! Abrir issue de segurança
- Verificar logs completos no console
- Verificar se guards.ts foi atualizado

---

## TESTE 4: Refresh rápido não permite bypass

### Passos:
1. Instituição ativa: **UFMS**
2. Acessar: `http://localhost:5173/admin/institutions`
3. Pressionar F5 rapidamente 10 vezes
4. Pressionar Ctrl+Shift+R (hard refresh) 5 vezes

### ✅ Resultado Esperado:
- **NUNCA** mostra conteúdo administrativo
- **SEMPRE** redireciona para dashboard
- Console mostra múltiplas vezes:
  ```
  [SECURITY] User is not admin, redirecting to dashboard
  ```

### ❌ Se não funcionar:
- CRÍTICO! Race condition ainda existe
- Reportar imediatamente

---

## TESTE 5: Backend retorna 403

### Passos:
1. Instituição ativa: **UFMS** (não SIMP-ADMIN)
2. Abrir DevTools > Network
3. Tentar fazer uma requisição administrativa (exemplo: criar instituição)

### ✅ Resultado Esperado:
- Backend retorna **403 Forbidden**
- Resposta do backend: "Access Denied" ou similar

### Como testar com curl:
```bash
# Obter o token do localStorage (via console do navegador)
localStorage.getItem('auth_token')

# Testar criação de instituição
curl -X POST http://localhost:8080/institutions \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "X-Institution-Id: ID_DA_UFMS" \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste","acronym":"TST","type":"PRIVADA"}'

# Deve retornar: 403 Forbidden
```

---

## 📊 CHECKLIST COMPLETO

Marque cada teste:

- [ ] ✅ TESTE 1: Admin funciona em SIMP-ADMIN
- [ ] ✅ TESTE 2: Admin NÃO funciona em UFMS
- [ ] ✅ TESTE 3: Rota administrativa redireciona
- [ ] ✅ TESTE 4: Refresh não permite bypass
- [ ] ✅ TESTE 5: Backend retorna 403

---

## 🐛 PROBLEMAS COMUNS

### Menu não desaparece ao trocar instituição

**Solução**:
1. Limpar cache: Ctrl+Shift+Delete
2. Fazer logout
3. Fazer login novamente
4. Testar novamente

### Console não mostra logs [DEBUG isAdmin]

**Causa**: Frontend não foi recompilado

**Solução**:
```bash
cd frontend
npm run dev
# ou
npm run build
```

### Erro "institutionStore is not defined"

**Causa**: Build antigo no cache

**Solução**:
```bash
cd frontend
rm -rf node_modules/.vite
rm -rf dist
npm run dev
```

### Backend retorna 200 ao invés de 403

**Causa**: Usuário realmente é admin ou backend não foi atualizado

**Verificar**:
1. Token JWT contém role ADMIN?
2. Backend está rodando versão atualizada?
3. Institution ID no header está correto?

---

## 🚨 CENÁRIOS DE FALHA CRÍTICA

Se QUALQUER um destes cenários acontecer, é CRÍTICO:

1. ❌ Usuário vê menu admin em instituição não-SIMP-ADMIN
2. ❌ Usuário consegue criar instituição sem ser admin
3. ❌ Refresh rápido mostra conteúdo administrativo brevemente
4. ❌ Backend retorna 200 para operação administrativa de não-admin

**Ação**: Reportar IMEDIATAMENTE como bug de segurança.

---

## ✅ TUDO PASSOU?

Se todos os 5 testes passaram:
1. ✅ Marcar correção como validada
2. ✅ Remover logs `[DEBUG isAdmin]` do código
3. ✅ Fazer commit das mudanças
4. ✅ Preparar para deploy

---

## 📝 LOGS ESPERADOS

### Console - Usuário em SIMP-ADMIN (ADMIN):
```
[DEBUG isAdmin] Checking admin status...
[DEBUG isAdmin] Active institution: SIMP-ADMIN
[DEBUG isAdmin] Active institution ID: uuid-da-simp-admin
[DEBUG isAdmin] Active institution check: {
  institutionId: "uuid-da-simp-admin",
  acronym: "SIMP-ADMIN",
  isSimpAdmin: true,
  roles: ["ADMIN"],
  hasAdminRole: true
}
[DEBUG isAdmin] Final result: true
[SECURITY] Admin check passed for route: admin-institutions
```

### Console - Usuário em UFMS (NÃO ADMIN):
```
[DEBUG isAdmin] Checking admin status...
[DEBUG isAdmin] Active institution: UFMS
[DEBUG isAdmin] Active institution ID: uuid-da-ufms
[DEBUG isAdmin] Active institution check: {
  institutionId: "uuid-da-ufms",
  acronym: "UFMS",
  isSimpAdmin: false,
  roles: ["VIEWER"],
  hasAdminRole: false
}
[DEBUG isAdmin] Final result: false
[SECURITY] User is not admin, redirecting to dashboard
```

---

**Tempo estimado**: 5-10 minutos para todos os testes
**Criticidade**: ALTA - Bug de segurança
**Responsável**: Desenvolvedor + QA
