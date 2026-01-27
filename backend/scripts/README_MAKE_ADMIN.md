# Como Tornar um Usuário ADMIN

Este guia explica como executar o script para tornar o usuário `breno.roosevelt@gmail.com` admin do sistema.

## Pré-requisitos

1. **O usuário DEVE ter feito login no sistema pelo menos uma vez** via OAuth (Google/Microsoft)
   - Isso cria o registro na tabela `users`
   - Se o usuário ainda não existe, o script não funcionará

2. **A instituição "Administração Simplifica" deve existir** no banco
   - Criada pela migration V4
   - Sigla: SIMP-ADMIN

3. **Acesso ao banco de dados PostgreSQL**

---

## Método 1: Via psql (Linha de Comando)

```bash
# 1. Conectar ao banco de dados
psql -h localhost -U simplifica -d simplifica_db

# 2. Executar o script
\i /home/breno/dev/claude-agents/backend/scripts/make_user_admin.sql

# Ou copiar e colar o conteúdo do arquivo diretamente no psql
```

---

## Método 2: Via Docker (se o banco estiver em container)

```bash
# 1. Copiar script para dentro do container
docker cp /home/breno/dev/claude-agents/backend/scripts/make_user_admin.sql postgres_container:/tmp/

# 2. Executar script dentro do container
docker exec -it postgres_container psql -U simplifica -d simplifica_db -f /tmp/make_user_admin.sql
```

---

## Método 3: Via DBeaver, pgAdmin ou outra ferramenta GUI

1. Abrir o arquivo `make_user_admin.sql`
2. Copiar todo o conteúdo
3. Colar na janela de query da ferramenta
4. Executar (F5 ou botão "Run")

---

## Verificação

Após executar o script, você verá mensagens como:

```
NOTICE:  Usuário encontrado: 550e8400-e29b-41d4-a716-446655440000
NOTICE:  Instituição encontrada: 123e4567-e89b-12d3-a456-426614174000
NOTICE:  Usuário atualizado: role=ADMIN, status=ACTIVE
NOTICE:  Vínculo criado com instituição: 789e0123-e89b-12d3-a456-426614174000
NOTICE:  Papel ADMIN adicionado à instituição
NOTICE:
NOTICE:  ========================================
NOTICE:  SUCESSO! Usuário breno.roosevelt@gmail.com agora é:
NOTICE:  - Role Global: ADMIN
NOTICE:  - Status: ACTIVE
NOTICE:  - Instituição: Administração Simplifica
NOTICE:  - Papel na Instituição: ADMIN
NOTICE:  ========================================
```

---

## Verificação no Banco

Para confirmar que deu certo, execute estas queries:

```sql
-- 1. Verificar role global do usuário
SELECT id, email, role, status
FROM users
WHERE email = 'breno.roosevelt@gmail.com';

-- Resultado esperado:
-- role: ADMIN
-- status: ACTIVE

-- 2. Verificar vínculo com instituição
SELECT
    u.email,
    i.name as institution_name,
    ui.active,
    array_agg(uir.role) as roles
FROM user_institutions ui
JOIN users u ON ui.user_id = u.id
JOIN institutions i ON ui.institution_id = i.id
LEFT JOIN user_institution_roles uir ON uir.user_institution_id = ui.id
WHERE u.email = 'breno.roosevelt@gmail.com'
GROUP BY u.email, i.name, ui.active;

-- Resultado esperado:
-- institution_name: Administração Simplifica
-- active: true
-- roles: {ADMIN}
```

---

## Verificação na Interface

1. Fazer logout e login novamente no sistema
2. O header deve mostrar "Admin" ou badge indicando permissões de admin
3. O menu deve exibir opções de administração:
   - Usuários
   - Instituições
   - Configurações
4. Ao acessar `/admin/users`, você deve ver todos os usuários do sistema

---

## Troubleshooting

### Erro: "Usuário não encontrado"

**Causa:** O usuário ainda não fez login via OAuth.

**Solução:**
1. Acesse http://localhost:3000/login (ou URL do frontend)
2. Clique em "Entrar com Google" ou "Microsoft"
3. Faça login com breno.roosevelt@gmail.com
4. Após login bem-sucedido, execute o script novamente

### Erro: "Instituição SIMP-ADMIN não encontrada"

**Causa:** A migration V4 não foi executada.

**Solução:**
```bash
# Executar migrations pendentes
cd /home/breno/dev/claude-agents/backend
mvn flyway:migrate

# Ou reiniciar a aplicação Spring Boot (Flyway roda automaticamente)
mvn spring-boot:run
```

### Erro: "permission denied"

**Causa:** Usuário do banco não tem permissões.

**Solução:**
```bash
# Conectar como superuser (postgres)
psql -h localhost -U postgres -d simplifica_db -f make_user_admin.sql
```

---

## Reverter (Remover Admin)

Se precisar remover as permissões de admin:

```sql
DO $$
DECLARE
    v_user_id UUID;
BEGIN
    SELECT id INTO v_user_id
    FROM users
    WHERE email = 'breno.roosevelt@gmail.com';

    -- Remover role global ADMIN
    UPDATE users
    SET role = 'USER', updated_at = CURRENT_TIMESTAMP
    WHERE id = v_user_id;

    -- Remover papel ADMIN da instituição
    DELETE FROM user_institution_roles uir
    USING user_institutions ui
    WHERE uir.user_institution_id = ui.id
      AND ui.user_id = v_user_id
      AND uir.role = 'ADMIN';

    RAISE NOTICE 'Admin removido de breno.roosevelt@gmail.com';
END $$;
```

---

## Notas Importantes

1. **Role Global vs Papel na Instituição:**
   - `users.role = 'ADMIN'` → Admin do sistema (acesso total)
   - `user_institution_roles.role = 'ADMIN'` → Admin da instituição específica

2. **Este script é seguro para re-execução:**
   - Usa `ON CONFLICT DO NOTHING` para papéis
   - Verifica existência antes de criar vínculos
   - Não duplica dados

3. **Auditoria:**
   - O script define `linked_by = v_user_id` (self-linked)
   - Em produção, considere registrar quem executou o script

---

## Contato

Se tiver problemas, verifique:
1. Logs do PostgreSQL: `/var/log/postgresql/`
2. Logs do Spring Boot: `backend/logs/`
3. Console do navegador (F12) para erros de frontend

---

**Criado em:** 2026-01-23
**Feature:** 002 - Vincular Usuário a Instituição
