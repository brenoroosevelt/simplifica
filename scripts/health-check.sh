#!/usr/bin/env bash

##############################################################################
# Script de Health Check dos Serviços
#
# Este script verifica o status de todos os serviços do projeto.
#
# Uso: ./scripts/health-check.sh
##############################################################################

set -e

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
    echo -e "${GREEN}[OK]${NC} $1"
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

echo ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "  VERIFICAÇÃO DE SAÚDE DOS SERVIÇOS"
log_info "═══════════════════════════════════════════════════════════════"
echo ""

##############################################################################
# 1. Verificar se containers estão rodando
##############################################################################

log_info "Verificando status dos containers..."
echo ""

# PostgreSQL
if docker-compose ps postgres 2>/dev/null | grep -q "Up"; then
    log_success "PostgreSQL container está rodando"

    # Verificar se está aceitando conexões
    if docker-compose exec -T postgres pg_isready -U admin -d claudeagents &>/dev/null; then
        log_success "PostgreSQL está aceitando conexões"
    else
        log_warning "PostgreSQL está rodando mas não aceitando conexões ainda"
    fi
else
    log_error "PostgreSQL container não está rodando"
fi

echo ""

# Backend
if docker-compose ps backend 2>/dev/null | grep -q "Up"; then
    log_success "Backend container está rodando"

    # Verificar health endpoint
    if curl -sf http://localhost:8080/api/public/health &>/dev/null; then
        log_success "Backend health check respondeu OK"

        # Exibir resposta do health
        HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/public/health)
        echo "  Response: $HEALTH_RESPONSE"
    else
        log_warning "Backend está rodando mas health check não responde ainda"
    fi
else
    log_error "Backend container não está rodando"
fi

echo ""

# Frontend
if docker-compose ps frontend 2>/dev/null | grep -q "Up"; then
    log_success "Frontend container está rodando"

    # Verificar se está respondendo
    if curl -sf http://localhost:5173 &>/dev/null; then
        log_success "Frontend está respondendo em http://localhost:5173"
    else
        log_warning "Frontend está rodando mas não responde ainda"
    fi
else
    log_error "Frontend container não está rodando"
fi

echo ""

##############################################################################
# 2. Verificar endpoints importantes
##############################################################################

log_info "Testando endpoints importantes..."
echo ""

# Backend Health
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/public/health 2>/dev/null || echo "000")
if [ "$HTTP_CODE" = "200" ]; then
    log_success "GET /api/public/health → 200 OK"
else
    log_error "GET /api/public/health → $HTTP_CODE (esperado: 200)"
fi

# Frontend Root
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 2>/dev/null || echo "000")
if [ "$HTTP_CODE" = "200" ]; then
    log_success "GET http://localhost:5173 → 200 OK"
else
    log_error "GET http://localhost:5173 → $HTTP_CODE (esperado: 200)"
fi

echo ""

##############################################################################
# 3. Verificar banco de dados
##############################################################################

log_info "Verificando banco de dados..."
echo ""

if docker-compose exec -T postgres psql -U admin -d claudeagents -c "SELECT 1;" &>/dev/null; then
    log_success "Conexão com banco de dados OK"

    # Verificar se migrations foram executadas
    MIGRATION_COUNT=$(docker-compose exec -T postgres psql -U admin -d claudeagents -t -c "SELECT COUNT(*) FROM flyway_schema_history;" 2>/dev/null | xargs || echo "0")

    if [ "$MIGRATION_COUNT" -gt "0" ]; then
        log_success "Migrations executadas: $MIGRATION_COUNT versão(ões)"
    else
        log_warning "Nenhuma migration encontrada no banco"
    fi

    # Verificar tabelas principais
    TABLE_COUNT=$(docker-compose exec -T postgres psql -U admin -d claudeagents -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';" 2>/dev/null | xargs || echo "0")
    log_info "Tabelas criadas: $TABLE_COUNT"

else
    log_error "Não foi possível conectar ao banco de dados"
fi

echo ""

##############################################################################
# 4. Resumo
##############################################################################

log_info "═══════════════════════════════════════════════════════════════"
log_info "  RESUMO"
log_info "═══════════════════════════════════════════════════════════════"
echo ""

# Contar sucessos e erros
SUCCESS_COUNT=0
ERROR_COUNT=0

docker-compose ps postgres 2>/dev/null | grep -q "Up" && SUCCESS_COUNT=$((SUCCESS_COUNT+1)) || ERROR_COUNT=$((ERROR_COUNT+1))
docker-compose ps backend 2>/dev/null | grep -q "Up" && SUCCESS_COUNT=$((SUCCESS_COUNT+1)) || ERROR_COUNT=$((ERROR_COUNT+1))
docker-compose ps frontend 2>/dev/null | grep -q "Up" && SUCCESS_COUNT=$((SUCCESS_COUNT+1)) || ERROR_COUNT=$((ERROR_COUNT+1))

echo "  Serviços rodando: ${GREEN}$SUCCESS_COUNT${NC} / 3"

if [ $ERROR_COUNT -eq 0 ]; then
    log_success "Todos os serviços estão saudáveis!"
    echo ""
    echo "  Acesse a aplicação:"
    echo "    Frontend: ${GREEN}http://localhost:5173${NC}"
    echo "    Backend:  ${GREEN}http://localhost:8080/api/public/health${NC}"
else
    log_warning "$ERROR_COUNT serviço(s) com problema"
    echo ""
    echo "  Para ver os logs:"
    echo "    ${YELLOW}docker-compose -f docker/docker-compose.yml logs -f${NC}"
    echo ""
    echo "  Para reiniciar os serviços:"
    echo "    ${YELLOW}./scripts/stop-dev.sh && ./scripts/start-dev.sh${NC}"
fi

echo ""
log_info "═══════════════════════════════════════════════════════════════"
echo ""

exit 0
