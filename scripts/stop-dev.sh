#!/usr/bin/env bash

##############################################################################
# Script para Parar o Ambiente de Desenvolvimento
#
# Este script para todos os containers Docker do ambiente de desenvolvimento.
#
# Uso: ./scripts/stop-dev.sh [--clean]
#
# Opções:
#   --clean   Remove containers, networks e volumes órfãos (não remove volumes nomeados)
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

# Verificar argumento --clean
CLEAN_MODE=false
if [ "$1" == "--clean" ]; then
    CLEAN_MODE=true
fi

##############################################################################
# Parar containers
##############################################################################

log_info "Parando containers Docker..."

if [ "$CLEAN_MODE" = true ]; then
    log_warning "Modo de limpeza ativado: removendo containers, networks e volumes órfãos..."
    docker-compose down --remove-orphans
    log_success "Containers parados e removidos!"
else
    docker-compose stop
    log_success "Containers parados!"
fi

echo ""

if [ "$CLEAN_MODE" = true ]; then
    log_info "Containers, networks e volumes órfãos foram removidos."
    log_info "Os volumes nomeados (dados do PostgreSQL) foram preservados."
    log_info ""
    log_info "Para remover também os dados do banco, execute:"
    log_info "  ${YELLOW}./scripts/reset-db.sh${NC}"
else
    log_info "Containers foram parados mas não removidos."
    log_info "Para iniciar novamente: ${GREEN}./scripts/start-dev.sh${NC}"
    log_info ""
    log_info "Para parar e remover containers: ${YELLOW}./scripts/stop-dev.sh --clean${NC}"
fi

echo ""

exit 0
