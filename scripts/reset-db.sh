#!/usr/bin/env bash

##############################################################################
# Script para Resetar o Banco de Dados
#
# Este script para o PostgreSQL, remove o volume de dados e recria o banco
# do zero, executando as migrations novamente.
#
# ATENÇÃO: Este comando remove TODOS os dados do banco de dados!
#
# Uso: ./scripts/reset-db.sh [--force]
#
# Opções:
#   --force   Não pede confirmação antes de deletar os dados
##############################################################################

set -e  # Sai em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Diretório raiz do projeto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT/docker"

# Verificar argumento --force
FORCE_MODE=false
if [ "$1" == "--force" ]; then
    FORCE_MODE=true
fi

##############################################################################
# Confirmação
##############################################################################

if [ "$FORCE_MODE" = false ]; then
    log_warning "═══════════════════════════════════════════════════════════════"
    log_warning "  ATENÇÃO: Esta operação irá DELETAR TODOS os dados do banco!"
    log_warning "═══════════════════════════════════════════════════════════════"
    echo ""
    read -p "Tem certeza que deseja continuar? (digite 'sim' para confirmar): " confirmation

    if [ "$confirmation" != "sim" ]; then
        log_info "Operação cancelada."
        exit 0
    fi
fi

echo ""

##############################################################################
# 1. Parar containers
##############################################################################

log_info "Parando containers..."
docker-compose stop postgres backend
log_success "Containers parados!"

echo ""

##############################################################################
# 2. Remover volume do PostgreSQL
##############################################################################

log_info "Removendo volume do PostgreSQL..."

# Nome do volume (verificar no docker-compose.yml)
VOLUME_NAME="docker_postgres-data"

if docker volume inspect "$VOLUME_NAME" &> /dev/null; then
    docker volume rm "$VOLUME_NAME"
    log_success "Volume removido: $VOLUME_NAME"
else
    log_warning "Volume não encontrado: $VOLUME_NAME"
fi

echo ""

##############################################################################
# 3. Recriar e iniciar PostgreSQL
##############################################################################

log_info "Recriando container do PostgreSQL..."
docker-compose up -d postgres

log_info "Aguardando PostgreSQL ficar pronto..."
sleep 5

# Verificar se está rodando
if docker-compose ps postgres | grep -q "Up"; then
    log_success "PostgreSQL está rodando!"
else
    log_error "PostgreSQL falhou ao iniciar. Verifique os logs:"
    log_error "  docker-compose -f docker/docker-compose.yml logs postgres"
    exit 1
fi

echo ""

##############################################################################
# 4. Iniciar backend (executa migrations)
##############################################################################

log_info "Iniciando backend (executando migrations Flyway)..."
docker-compose up -d backend

log_info "Aguardando backend ficar pronto..."
sleep 10

# Verificar se está rodando
if docker-compose ps backend | grep -q "Up"; then
    log_success "Backend está rodando!"
else
    log_warning "Backend pode não ter iniciado corretamente. Verifique os logs:"
    log_warning "  docker-compose -f docker/docker-compose.yml logs backend"
fi

echo ""

##############################################################################
# 5. Verificar migrations
##############################################################################

log_info "Verificando migrations..."
echo ""

# Tentar consultar a tabela flyway_schema_history
MIGRATION_CHECK=$(docker-compose exec -T postgres psql -U admin -d claudeagents -t -c "SELECT COUNT(*) FROM flyway_schema_history;" 2>/dev/null || echo "0")

if [ "$MIGRATION_CHECK" != "0" ]; then
    log_success "Migrations executadas com sucesso!"
    log_info "Versões aplicadas:"
    docker-compose exec -T postgres psql -U admin -d claudeagents -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
else
    log_warning "Não foi possível verificar as migrations. Verifique os logs do backend."
fi

echo ""

##############################################################################
# Sucesso
##############################################################################

log_success "Banco de dados resetado com sucesso!"
echo ""
log_info "O banco está limpo e as migrations foram executadas novamente."
log_info "Todos os dados anteriores foram removidos."
echo ""
log_info "Para iniciar todos os serviços:"
log_info "  ${GREEN}./scripts/start-dev.sh${NC}"
echo ""

exit 0
