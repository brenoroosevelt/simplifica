# Feature 001 - Vincular usuário a instituição

## Contexto
Usuários logan no app via provedor (google, microsoft, etc), o usuário fica pendente até que um administrador o vincule a uma instituição.


## Objetivo
Os papeis são: USUARIO, GESTOR e ADMIN.
Permitir que o ADMIN vincule o usuário a uma ou mais instituições.
 - Deste vínculo de usuário com instituição, temos os papeis associados que vão determinar o perfil do usuário na insituição.
 - Uma instituição chamada Administração Simplifica deve existir com id 1 (migrations) e apenas os usuários desta instituição poderão ser admin.

Listar os usuários cadastrados, permitindo filtrar pos status pendente, instituição, papel/perfil, nome, email.
Permitir também que este vínculo seja modificado, mas somente admin podem alterar a instituição.
Permitir que além do vínculo com insituição (apenas admin), os papeis seja modificados e outras informação não críticas vindas do proivedor de acesso (para esta alteração GESTOR também pode realizar)
Gestores só podem ver usuários de sua instituição (portanto o filtro de insituição não precisa existir para GESTOR)
Admin podem ver todos os usuários.
A tela de intituições tem um botão para usuários vinculados que pode ser removido.
Status de usuário: pendente, ativo e inativo.
Pepeis: Usuário, Gestor, Admin.
Usuário pode alternar a instituição de trabalho selecionada (no header ou na tela de user profile)
O vínculo com uma instituição deve acontecer de forma automática se o dominio do email for o mesmo da instituição e o papel neste caso é USUARIO apenas.

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
- Profile do usuário alterado para mostras as instituições vinculadas e os papeis associados neste vínculo.
- O vínculo de um usuário com uma instituição deve poder escolher um ou vários papeis
- Atribuir perfil ao usuário (somente admin, ou gestor pode)
- Frontend deve validar as rotas e telas usando os perfis (roles do usuário)
- endpoint no backend da api deve validar roles e instituição selecionada

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