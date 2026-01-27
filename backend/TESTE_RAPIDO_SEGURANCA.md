# TESTE RÁPIDO DE SEGURANÇA - Controle de Acesso Admin

## Checklist Rápido (5 minutos)

### ✅ Teste 1: Usuário Comum NÃO pode acessar rotas admin

**Passos:**
1. Login como usuário comum (sem ADMIN em SIMP-ADMIN)
2. Abrir console do navegador (F12)
3. Tentar acessar: `http://localhost:5173/admin/institutions`

**✅ SUCESSO se:**
- Redireciona para `/dashboard`
- Console mostra: `[SECURITY] User is not admin, redirecting to dashboard`

**❌ FALHA se:**
- Consegue acessar a página
- Não há redirecionamento

---

### ✅ Teste 2: Botões administrativos ocultos para usuário comum

**Passos:**
1. Já logado como usuário comum
2. Se conseguir ver alguma página com lista de instituições

**✅ SUCESSO se:**
- Botão "Nova Instituição" NÃO aparece
- Coluna "Ações" mostra "Sem permissão"

**❌ FALHA se:**
- Botões de editar/deletar aparecem

---

### ✅ Teste 3: Usuário Admin TEM acesso completo

**Passos:**
1. Login como admin (role ADMIN em SIMP-ADMIN)
2. Abrir console do navegador (F12)
3. Acessar: `http://localhost:5173/admin/institutions`

**✅ SUCESSO se:**
- Acessa a página normalmente
- Console mostra: `[SECURITY] Admin check passed for route: admin-institutions`
- Botão "Nova Instituição" aparece
- Botões de ações (editar/deletar) aparecem

**❌ FALHA se:**
- Não consegue acessar
- Botões não aparecem

---

### ✅ Teste 4: Backend bloqueia requisições não autorizadas

**Teste via CURL (se tiver acesso):**

```bash
# Obter token de usuário comum do localStorage do navegador
TOKEN="cole_aqui_o_token_do_usuario_comum"

# Tentar criar instituição
curl -X POST http://localhost:8080/institutions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Hack","acronym":"HACK","type":"PRIVADA"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

**✅ SUCESSO se:**
- Retorna: `HTTP Status: 403`

**❌ FALHA se:**
- Retorna: `HTTP Status: 201` ou `200`

---

## Teste Completo de Race Condition

### ✅ Teste 5: Refresh rápido não permite bypass

**Passos:**
1. Login como usuário comum
2. Acesse: `http://localhost:5173/admin/institutions` (vai redirecionar)
3. No console, veja os logs
4. Pressione F5 rapidamente 10x seguidas
5. Abra DevTools > Network > marque "Disable cache"
6. Pressione Ctrl+Shift+R (hard refresh) 5x

**✅ SUCESSO se:**
- SEMPRE redireciona para /dashboard
- NUNCA mostra conteúdo da página admin

**❌ FALHA se:**
- Em algum momento mostra a página admin
- Consegue ver botões administrativos

---

## Validação dos Logs (Importante!)

### Usuário Comum - Console deve mostrar:

```
[DEBUG isAdmin] Checking admin status...
[DEBUG isAdmin] institutions.value: [...]
[DEBUG isAdmin] Checking institution: { acronym: "UFMG", isSimpAdmin: false, ... }
[DEBUG isAdmin] Final result: false
[SECURITY] User is not admin, redirecting to dashboard
```

### Usuário Admin - Console deve mostrar:

```
[DEBUG isAdmin] Checking admin status...
[DEBUG isAdmin] institutions.value: [...]
[DEBUG isAdmin] Checking institution: { acronym: "SIMP-ADMIN", isSimpAdmin: true, ... }
[DEBUG isAdmin] Final result: true
[SECURITY] Admin check passed for route: admin-institutions
```

---

## Se Algum Teste FALHAR

🚨 **NÃO FAZER DEPLOY!**

Entre em contato imediatamente e reporte:
1. Qual teste falhou
2. Screenshot do console
3. Screenshot da tela
4. Tipo de usuário (admin ou comum)
5. Instituições vinculadas ao usuário

---

## Checklist Final

Antes de aprovar o deploy, confirme:

- [ ] Teste 1 passou ✅
- [ ] Teste 2 passou ✅
- [ ] Teste 3 passou ✅
- [ ] Teste 4 passou ✅ (se possível)
- [ ] Teste 5 passou ✅
- [ ] Logs aparecem corretamente no console
- [ ] Testado com pelo menos 2 usuários diferentes
- [ ] Testado em navegadores diferentes (Chrome, Firefox)

---

## Após Aprovação

**LEMBRAR DE:**
- [ ] Remover logs `[DEBUG isAdmin]` de `auth.store.ts`
- [ ] Manter logs `[SECURITY]` em `guards.ts`
- [ ] Documentar no changelog
