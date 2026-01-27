#!/bin/bash

# ====================================
# Script: Migração Completa - Remoção de role
# ====================================
# Este script automatiza o processo completo de migração
# da coluna role de users para o novo modelo baseado em instituições.
# ====================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações do banco (ajustar conforme necessário)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-simplifica_db}"
DB_USER="${DB_USER:-simplifica}"
DB_PASSWORD="${DB_PASSWORD:-simplifica}"

# Diretório do script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="$SCRIPT_DIR/backups"

# Criar diretório de backups se não existir
mkdir -p "$BACKUP_DIR"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Migração: Remoção da Coluna role${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Configurações:"
echo "  Host: $DB_HOST:$DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo "  Backup Dir: $BACKUP_DIR"
echo ""

# Verificar se psql está instalado
if ! command -v psql &> /dev/null; then
    echo -e "${RED}ERRO: psql não encontrado!${NC}"
    echo "Instale o PostgreSQL client primeiro."
    exit 1
fi

# Verificar se pg_dump está instalado
if ! command -v pg_dump &> /dev/null; then
    echo -e "${RED}ERRO: pg_dump não encontrado!${NC}"
    echo "Instale o PostgreSQL client tools primeiro."
    exit 1
fi

# Função para executar SQL
execute_sql() {
    local sql="$1"
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "$sql"
}

# Função para executar arquivo SQL
execute_sql_file() {
    local file="$1"
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$file"
}

# ====================================
# Passo 0: Validações Iniciais
# ====================================
echo -e "${YELLOW}[0/5] Validando pré-requisitos...${NC}"

# Verificar se coluna role existe
ROLE_COLUMN_EXISTS=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c \
    "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'role';" | xargs)

if [ "$ROLE_COLUMN_EXISTS" -eq "0" ]; then
    echo -e "${GREEN}✓ Migração já foi executada! Coluna role não existe.${NC}"
    exit 0
fi

# Verificar se SIMP-ADMIN existe
SIMP_ADMIN_EXISTS=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c \
    "SELECT COUNT(*) FROM institutions WHERE acronym = 'SIMP-ADMIN';" | xargs)

if [ "$SIMP_ADMIN_EXISTS" -eq "0" ]; then
    echo -e "${RED}✗ Instituição SIMP-ADMIN não encontrada!${NC}"
    echo "Execute a migration V4 primeiro:"
    echo "  cd backend && mvn flyway:migrate"
    exit 1
fi

echo -e "${GREEN}✓ Pré-requisitos validados${NC}"
echo ""

# ====================================
# Passo 1: Backup
# ====================================
echo -e "${YELLOW}[1/5] Criando backup do banco de dados...${NC}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_before_role_migration_$TIMESTAMP.sql"

pg_dump -h $DB_HOST -p $DB_PORT -U $DB_USER $DB_NAME > "$BACKUP_FILE" 2>/dev/null || {
    echo -e "${RED}✗ Erro ao criar backup!${NC}"
    exit 1
}

BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
echo -e "${GREEN}✓ Backup criado: $BACKUP_FILE ($BACKUP_SIZE)${NC}"
echo ""

# ====================================
# Passo 2: Listar Admins
# ====================================
echo -e "${YELLOW}[2/5] Listando usuários com role ADMIN...${NC}"

ADMIN_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c \
    "SELECT COUNT(*) FROM users WHERE role = 'ADMIN';" | xargs)

if [ "$ADMIN_COUNT" -gt "0" ]; then
    echo -e "Encontrados ${BLUE}$ADMIN_COUNT${NC} usuários com role ADMIN:"
    echo ""
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c \
        "SELECT email, name, status FROM users WHERE role = 'ADMIN' ORDER BY email;"
    echo ""
else
    echo -e "${GREEN}✓ Nenhum usuário com role ADMIN encontrado.${NC}"
    echo ""
fi

# Confirmar execução
if [ "$ADMIN_COUNT" -gt "0" ]; then
    echo -e "${YELLOW}Estes usuários serão vinculados à instituição SIMP-ADMIN.${NC}"
fi
echo ""
read -p "Deseja continuar com a migração? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Migração cancelada."
    exit 0
fi
echo ""

# ====================================
# Passo 3: Migrar Admins
# ====================================
if [ "$ADMIN_COUNT" -gt "0" ]; then
    echo -e "${YELLOW}[3/5] Migrando admins para SIMP-ADMIN...${NC}"

    execute_sql_file "$SCRIPT_DIR/migrate_admins_to_simp_admin.sql" || {
        echo -e "${RED}✗ Erro ao migrar admins!${NC}"
        echo "Restaurar backup com:"
        echo "  psql -h $DB_HOST -U $DB_USER -d $DB_NAME < $BACKUP_FILE"
        exit 1
    }

    echo -e "${GREEN}✓ Admins migrados com sucesso${NC}"
    echo ""
else
    echo -e "${YELLOW}[3/5] Pulando migração de admins (nenhum encontrado)${NC}"
    echo ""
fi

# ====================================
# Passo 4: Executar Migration V6
# ====================================
echo -e "${YELLOW}[4/5] Removendo coluna role da tabela users...${NC}"

execute_sql "ALTER TABLE users DROP COLUMN IF EXISTS role;" || {
    echo -e "${RED}✗ Erro ao remover coluna role!${NC}"
    echo "Restaurar backup com:"
    echo "  psql -h $DB_HOST -U $DB_USER -d $DB_NAME < $BACKUP_FILE"
    exit 1
}

execute_sql "COMMENT ON TABLE users IS 'Users table - roles are now managed per institution in user_institution_roles';" || true

echo -e "${GREEN}✓ Coluna role removida${NC}"
echo ""

# ====================================
# Passo 5: Validação Final
# ====================================
echo -e "${YELLOW}[5/5] Validando migração...${NC}"

# Verificar que coluna role não existe mais
ROLE_COLUMN_EXISTS=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c \
    "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'role';" | xargs)

if [ "$ROLE_COLUMN_EXISTS" -eq "0" ]; then
    echo -e "${GREEN}✓ Coluna role removida com sucesso${NC}"
else
    echo -e "${RED}✗ Erro: Coluna role ainda existe!${NC}"
    exit 1
fi

# Verificar admins migrados
if [ "$ADMIN_COUNT" -gt "0" ]; then
    echo ""
    echo "Usuários vinculados a SIMP-ADMIN:"
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c \
        "SELECT u.email, u.name, array_agg(DISTINCT uir.role::text) as roles
         FROM users u
         JOIN user_institutions ui ON ui.user_id = u.id
         JOIN institutions i ON i.id = ui.institution_id
         LEFT JOIN user_institution_roles uir ON uir.user_institution_id = ui.id
         WHERE i.acronym = 'SIMP-ADMIN' AND ui.active = true
         GROUP BY u.email, u.name
         ORDER BY u.email;"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Migração concluída com sucesso!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Resumo:"
echo "  • Backup: $BACKUP_FILE"
echo "  • Admins migrados: $ADMIN_COUNT"
echo "  • Coluna role: REMOVIDA"
echo ""
echo "Próximos passos:"
echo "  1. Reiniciar a aplicação backend"
echo "  2. Reiniciar a aplicação frontend"
echo "  3. Testar login e autorização"
echo "  4. Validar que admins têm acesso total"
echo ""
echo "Para rollback (se necessário):"
echo "  psql -h $DB_HOST -U $DB_USER -d $DB_NAME < $BACKUP_FILE"
echo ""

exit 0
