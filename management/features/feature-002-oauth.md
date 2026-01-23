# Feature 001 - OAuth Login

## Contexto
Usuários precisam autenticar no sistema usando contas Google e Microsoft, sem criar senha local.

## Objetivo
Permitir login seguro e rápido usando OAuth2, criando usuário automaticamente no primeiro acesso.

## Usuários impactados
- Usuários finais do sistema web
- Administradores

## Escopo
Inclui:
- perfis: USER, GESTOR e ADMIN
- Login via Google
- Login via Microsoft
- Persistência do usuário no banco
- Tela de login e callback no frontend
- Profile do usuário
- O vínculo de um usuário com uma instituição deve poder escolher um ou vários papeis
- Capacidade de plugar outros serviços de login como Facebook
- Atribuir perfil ao usuário (somente admin, ou gestor pode)
- Frontend deve validar as rotas e telas usando os perfis (roles do usuário)
- endpoint da api deve validar roles

Não inclui:
- Login com email/senha
- Recuperação de senha
- MFA

---

## Fluxo do usuário
1. Usuário acessa /login
2. Clica em "Entrar com Google" ou "Microsoft"
3. É redirecionado para provedor
4. Autoriza acesso
5. Retorna para app autenticado
6. Se for primeiro login, usuário é criado
7. O usuário fica pendente até que um administrador vincule a uma instituição.
8. O vínculo pode acontecer automaticamente com base em no seu e-mail recuperado do oauth, caso o domínio seja o mesmo domínio da instituição.
9. Usuário pendente tem acesso ao sitema, mas não consegue executar nada, um alerta deve ser mostrado.
---

## Regras de negócio
- Email é identificador único
- Usuário criado com role USER
- Se email já existir, apenas autentica
- Token deve expirar conforme provedor

---

## Tarefas

### Frontend
- [ ] ...

### Backend
- [ ] ... 

### Banco de dados
- [ ] ... 

### Infra
- [ ] ...

### QA
- [ ] Login Google funciona
- [ ] Login Microsoft funciona
- [ ] Usuário salvo no banco
- [ ] Token inválido retorna erro
- [ ] Logout funciona

---

## Critérios de aceite
- Usuário consegue logar sem erro
- Usuário é persistido corretamente
- Sessão expira corretamente
- Sistema funciona em desktop e mobile

---

## Riscos
- Bloqueio por configuração errada dos providers
- Problemas de CORS

## Dependências
- Feature 000 - Bootstrap do projeto
- Postgres rodando