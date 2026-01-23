#!/usr/bin/env bash

##############################################################################
# Script para Iniciar o Ambiente de Desenvolvimento
#
# Este script inicia todos os containers Docker e exibe informações úteis
# sobre os serviços em execução.
#
# Uso: ./scripts/start-dev.sh
##############################################################################

set -e  # Sai em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
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

log_info "Iniciando ambiente de desenvolvimento..."
echo ""

##############################################################################
# 1. Verificar se .env existem
##############################################################################

if [ ! -f "../backend/.env" ]; then
    log_error "Arquivo backend/.env não encontrado!"
    log_error "Execute primeiro: ./scripts/setup-dev.sh"
    exit 1
fi

if [ ! -f "../frontend/.env" ]; then
    log_error "Arquivo frontend/.env não encontrado!"
    log_error "Execute primeiro: ./scripts/setup-dev.sh"
    exit 1
fi

##############################################################################
# 2. Iniciar containers
##############################################################################

log_info "Iniciando containers Docker..."

docker-compose up -d

if [ $? -eq 0 ]; then
    log_success "Containers iniciados com sucesso!"
else
    log_error "Erro ao iniciar containers. Verifique os logs acima."
    exit 1
fi

echo ""
log_info "Aguardando serviços ficarem prontos..."
sleep 5

##############################################################################
# 3. Verificar status dos serviços
##############################################################################

log_info "Verificando status dos serviços..."
echo ""

# PostgreSQL
if docker-compose ps postgres | grep -q "Up"; then
    log_success "PostgreSQL está rodando"
else
    log_warning "PostgreSQL pode não estar pronto ainda"
fi

# Backend
if docker-compose ps backend | grep -q "Up"; then
    log_success "Backend está rodando"
else
    log_warning "Backend pode não estar pronto ainda"
fi

# Frontend
if docker-compose ps frontend | grep -q "Up"; then
    log_success "Frontend está rodando"
else
    log_warning "Frontend pode não estar pronto ainda"
fi

echo ""

##############################################################################
# 4. Exibir informações dos serviços
##############################################################################

log_success "Ambiente de desenvolvimento iniciado!"
echo ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "  SERVIÇOS DISPONÍVEIS"
log_info "═══════════════════════════════════════════════════════════════"
echo ""
echo "  ${CYAN}Frontend (Vue 3 + Vite):${NC}"
echo "    URL: ${GREEN}http://localhost:5173${NC}"
echo "    Hot Reload: ${GREEN}Habilitado${NC}"
echo ""
echo "  ${CYAN}Backend (Spring Boot):${NC}"
echo "    URL: ${GREEN}http://localhost:8080${NC}"
echo "    Health Check: ${GREEN}http://localhost:8080/api/public/health${NC}"
echo "    Swagger UI: ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo "  ${CYAN}PostgreSQL:${NC}"
echo "    Host: ${GREEN}localhost:5432${NC}"
echo "    Database: ${GREEN}claudeagents${NC}"
echo "    User: ${GREEN}admin${NC}"
echo "    Password: ${YELLOW}(conforme configurado no .env)${NC}"
echo ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "  COMANDOS ÚTEIS"
log_info "═══════════════════════════════════════════════════════════════"
echo ""
echo "  Ver logs de todos os serviços:"
echo "    ${YELLOW}docker-compose -f docker/docker-compose.yml logs -f${NC}"
echo ""
echo "  Ver logs de um serviço específico:"
echo "    ${YELLOW}docker-compose -f docker/docker-compose.yml logs -f frontend${NC}"
echo "    ${YELLOW}docker-compose -f docker/docker-compose.yml logs -f backend${NC}"
echo "    ${YELLOW}docker-compose -f docker/docker-compose.yml logs -f postgres${NC}"
echo ""
echo "  Parar todos os serviços:"
echo "    ${YELLOW}./scripts/stop-dev.sh${NC}"
echo ""
echo "  Resetar banco de dados:"
echo "    ${YELLOW}./scripts/reset-db.sh${NC}"
echo ""
echo "  Reconstruir containers (após mudanças no Dockerfile):"
echo "    ${YELLOW}docker-compose -f docker/docker-compose.yml up -d --build${NC}"
echo ""
log_info "═══════════════════════════════════════════════════════════════"
echo ""

log_info "Aguarde alguns segundos para os serviços ficarem completamente prontos..."
log_info "Verifique os logs se algo não estiver funcionando."
echo ""

exit 0
