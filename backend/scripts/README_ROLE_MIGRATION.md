# Guia de Migração: Remoção da Coluna `role` de Users

Este guia explica como migrar do modelo antigo (role global) para o novo modelo (roles por instituição).

---

## Contexto

### Modelo Antigo ❌
- Tabela `users` tinha coluna `role` (USER, ADMIN)
- Role global para todo o sistema
- Ambíguo com roles por instituição

### Modelo Novo ✅
- Role existe apenas no contexto de instituições
- Administradores do Sistema = role ADMIN na instituição **SIMP-ADMIN**
- Gestores = role ADMIN em outras instituições
- Multi-tenancy verdadeiro

---

## Pré-requisitos

1. **Backup do banco de dados:**
   ```bash
   pg_dump -h localhost -U simplifica simplifica_db > backup_before_role_migration.sql
   ```

2. **Instituição SIMP-ADMIN deve existir:**
   - Criada pela migration V4
   - Sigla: SIMP-ADMIN
   - Tipo: PRIVADA

3. **Aplicação parada (recomendado para produção):**
   ```bash
   # Se estiver rodando com systemd
   sudo systemctl stop simplifica-backend
   ```

---

## Processo de Migração

### Passo 1: Validar Estado Atual

```bash
psql -h localhost -U simplifica -d simplifica_db
```

```sql
-- 1. Verificar se coluna role existe
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'users' AND column_name = 'role';
-- Deve retornar: role | character varying

-- 2. Listar usuários com role ADMIN
SELECT id, email, name, role, status
FROM users
WHERE role = 'ADMIN';
-- Anote quantos existem

-- 3. Verificar se SIMP-ADMIN existe
SELECT id, name, acronym
FROM institutions
WHERE acronym = 'SIMP-ADMIN';
-- Deve retornar a instituição
```

### Passo 2: Migrar Admins Antigos (SE HOUVER)

**Importante:** Execute este script **ANTES** da migration V6!

```bash
# Via psql
psql -h localhost -U simplifica -d simplifica_db -f /home/breno/dev/claude-agents/backend/scripts/migrate_admins_to_simp_admin.sql
```

**OU via Docker:**
```bash
docker cp /home/breno/dev/claude-agents/backend/scripts/migrate_admins_to_simp_admin.sql postgres_container:/tmp/
docker exec -it postgres_container psql -U simplifica -d simplifica_db -f /tmp/migrate_admins_to_simp_admin.sql
```

**Saída esperada:**
```
========================================
Iniciando migração de admins para SIMP-ADMIN
========================================

Instituição SIMP-ADMIN encontrada: 123e4567-e89b-12d3-a456-426614174000

  ✓ Vínculo criado para: João Silva (joao@example.com)
  ✓ Vínculo criado para: Maria Santos (maria@example.com)

========================================
Migração concluída!
Total de admins migrados: 2
========================================
```

### Passo 3: Validar Migração

```sql
-- Verificar que admins foram vinculados a SIMP-ADMIN
SELECT
    u.email,
    u.name,
    u.role as old_role,
    i.acronym as institution,
    array_agg(DISTINCT uir.role::text) as institution_roles
FROM users u
JOIN user_institutions ui ON ui.user_id = u.id AND ui.active = true
JOIN institutions i ON i.id = ui.institution_id
JOIN user_institution_roles uir ON uir.user_institution_id = ui.id
WHERE u.role = 'ADMIN'
  AND i.acronym = 'SIMP-ADMIN'
GROUP BY u.email, u.name, u.role, i.acronym;

-- Resultado esperado:
--  email              | name         | old_role | institution | institution_roles
-- --------------------+--------------+----------+-------------+-------------------
--  joao@example.com  | João Silva   | ADMIN    | SIMP-ADMIN  | {ADMIN}
--  maria@example.com | Maria Santos | ADMIN    | SIMP-ADMIN  | {ADMIN}
```

### Passo 4: Executar Migration V6

**Opção A: Via Maven (aplicação reinicia automaticamente)**
```bash
cd /home/breno/dev/claude-agents/backend
mvn spring-boot:run
```
Flyway detecta e executa V6 automaticamente.

**Opção B: Via Flyway explicitamente**
```bash
cd /home/breno/dev/claude-agents/backend
mvn flyway:migrate
```

**Opção C: Manualmente via SQL (não recomendado)**
```bash
psql -h localhost -U simplifica -d simplifica_db -f /home/breno/dev/claude-agents/backend/src/main/resources/db/migration/V6__remove_user_role_column.sql
```

### Passo 5: Validar Migration V6

```sql
-- 1. Verificar que coluna role NÃO existe mais
SELECT column_name
FROM information_schema.columns
WHERE table_name = 'users' AND column_name = 'role';
-- Deve retornar VAZIO (0 rows)

-- 2. Verificar estrutura da tabela users
\d users
-- Não deve listar coluna 'role'

-- 3. Verificar que Flyway registrou a migration
SELECT version, description, installed_on, success
FROM flyway_schema_history
WHERE version = '6';
-- Deve mostrar: 6 | remove user role column | [timestamp] | t
```

### Passo 6: Testar Aplicação

```bash
# Iniciar backend
cd /home/breno/dev/claude-agents/backend
mvn spring-boot:run

# Em outro terminal, iniciar frontend
cd /home/breno/dev/claude-agents/frontend
npm run dev
```

**Testes:**
1. Login com usuário admin (vinculado a SIMP-ADMIN)
2. Acessar `/admin/users` - deve funcionar
3. Acessar `/admin/institutions` - deve funcionar
4. Login com usuário regular (sem vínculo a SIMP-ADMIN)
5. Tentar acessar `/admin/users` - deve bloquear (403)

---

## Rollback (Se Necessário)

### Antes da V6 (Tem Backup)

```bash
# Restaurar backup
psql -h localhost -U simplifica -d simplifica_db < backup_before_role_migration.sql
```

### Após a V6 (Sem Backup)

⚠️ **Rollback é mais complexo após V6!**

```sql
-- 1. Recriar coluna role
ALTER TABLE users ADD COLUMN role VARCHAR(50);

-- 2. Popular com valores padrão
UPDATE users SET role = 'USER';

-- 3. Atualizar admins do sistema
UPDATE users u
SET role = 'ADMIN'
FROM user_institutions ui
JOIN institutions i ON i.id = ui.institution_id
WHERE ui.user_id = u.id
  AND i.acronym = 'SIMP-ADMIN'
  AND ui.active = true;

-- 4. Verificar
SELECT email, role FROM users ORDER BY role DESC, email;
```

**Depois:**
- Reverter código do backend para versão anterior
- Reverter código do frontend para versão anterior
- Remover V6 do `flyway_schema_history`:
```sql
DELETE FROM flyway_schema_history WHERE version = '6';
```

---

## Troubleshooting

### Erro: "Instituição SIMP-ADMIN não encontrada"

**Causa:** Migration V4 não foi executada.

**Solução:**
```bash
cd /home/breno/dev/claude-agents/backend
mvn flyway:migrate
# Ou execute manualmente a V4:
psql -h localhost -U simplifica -d simplifica_db -f backend/src/main/resources/db/migration/V4__insert_default_admin_institution.sql
```

### Erro: "column users.role does not exist"

**Causa:** V6 já foi executada, mas aplicação ainda usa código antigo.

**Solução:**
```bash
cd /home/breno/dev/claude-agents/backend
git pull origin main  # Ou branch correto
mvn clean install
mvn spring-boot:run
```

### Usuário não consegue acessar admin após migração

**Causa:** Usuário não foi vinculado a SIMP-ADMIN.

**Solução:**
```bash
# Usar script make_admin.sh
cd /home/breno/dev/claude-agents/backend/scripts
./make_admin.sh

# Ou manualmente:
psql -h localhost -U simplifica -d simplifica_db
```
```sql
-- Ver script migrate_admins_to_simp_admin.sql para detalhes
```

### Frontend mostra erro "user.role is undefined"

**Causa:** Frontend ainda usa código antigo.

**Solução:**
```bash
cd /home/breno/dev/claude-agents/frontend
git pull origin main  # Ou branch correto
npm install
npm run build
npm run dev
```

---

## Checklist Pré-Deploy

### Desenvolvimento
- [ ] Backup do banco criado
- [ ] V4 executada (SIMP-ADMIN existe)
- [ ] Script `migrate_admins_to_simp_admin.sql` executado
- [ ] Admins validados (vinculados a SIMP-ADMIN)
- [ ] V6 executada (coluna role removida)
- [ ] Backend compilado e testado
- [ ] Frontend compilado e testado
- [ ] Testes de autorização funcionando

### Staging
- [ ] Backup do banco criado
- [ ] Testar fluxo completo de migração
- [ ] Validar usuários existentes
- [ ] Testes E2E passando
- [ ] Rollback testado (se possível)

### Produção
- [ ] **BACKUP CRÍTICO DO BANCO**
- [ ] Janela de manutenção agendada
- [ ] Notificação aos usuários
- [ ] Aplicação parada
- [ ] Script de migração executado
- [ ] V6 executada
- [ ] Validação completa
- [ ] Aplicação reiniciada
- [ ] Monitoramento ativo
- [ ] Usuários notificados

---

## Contatos e Suporte

- **Documentação:** `/management/features/feature-002-role-removal-report.md`
- **Scripts:** `/backend/scripts/`
- **Issues:** Criar issue no repositório do projeto

---

**Criado em:** 2026-01-23
**Versão:** 1.0
**Status:** ✅ Pronto para uso
