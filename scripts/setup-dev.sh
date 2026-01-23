#!/usr/bin/env bash

##############################################################################
# Script de Setup Inicial do Ambiente de Desenvolvimento
#
# Este script prepara o ambiente para desenvolvimento local, verificando
# pré-requisitos e criando arquivos de configuração necessários.
#
# Uso: ./scripts/setup-dev.sh
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
cd "$PROJECT_ROOT"

log_info "Iniciando setup do ambiente de desenvolvimento..."
echo ""

##############################################################################
# 1. Verificar Pré-requisitos
##############################################################################

log_info "Verificando pré-requisitos..."

# Verificar Docker
if ! command -v docker &> /dev/null; then
    log_error "Docker não encontrado. Por favor, instale o Docker Desktop:"
    log_error "  https://www.docker.com/products/docker-desktop"
    exit 1
fi
log_success "Docker encontrado: $(docker --version)"

# Verificar Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    log_error "Docker Compose não encontrado. Por favor, instale o Docker Compose."
    exit 1
fi
log_success "Docker Compose encontrado"

# Verificar Node.js (para desenvolvimento local, opcional)
if command -v node &> /dev/null; then
    log_success "Node.js encontrado: $(node --version)"
else
    log_warning "Node.js não encontrado (opcional para desenvolvimento local)"
fi

# Verificar Java (para desenvolvimento local, opcional)
if command -v java &> /dev/null; then
    log_success "Java encontrado: $(java -version 2>&1 | head -n 1)"
else
    log_warning "Java não encontrado (opcional para desenvolvimento local)"
fi

echo ""

##############################################################################
# 2. Criar Arquivos .env
##############################################################################

log_info "Configurando arquivos de ambiente..."

# Backend .env
if [ ! -f "backend/.env" ]; then
    if [ -f "backend/.env.example" ]; then
        cp backend/.env.example backend/.env
        log_success "Arquivo backend/.env criado a partir do .env.example"
        log_warning "Por favor, configure as credenciais OAuth no arquivo backend/.env"
    else
        log_error "Arquivo backend/.env.example não encontrado!"
        exit 1
    fi
else
    log_info "Arquivo backend/.env já existe (não sobrescrito)"
fi

# Frontend .env
if [ ! -f "frontend/.env" ]; then
    if [ -f "frontend/.env.example" ]; then
        cp frontend/.env.example frontend/.env
        log_success "Arquivo frontend/.env criado a partir do .env.example"
        log_warning "Por favor, configure as credenciais OAuth no arquivo frontend/.env"
    else
        log_error "Arquivo frontend/.env.example não encontrado!"
        exit 1
    fi
else
    log_info "Arquivo frontend/.env já existe (não sobrescrito)"
fi

echo ""

##############################################################################
# 3. Verificar Docker Compose
##############################################################################

log_info "Verificando arquivo docker-compose.yml..."

if [ ! -f "docker/docker-compose.yml" ]; then
    log_error "Arquivo docker/docker-compose.yml não encontrado!"
    exit 1
fi
log_success "Arquivo docker-compose.yml encontrado"

echo ""

##############################################################################
# 4. Build dos Containers
##############################################################################

log_info "Construindo containers Docker..."
log_info "Isso pode levar alguns minutos na primeira vez..."

cd docker
docker-compose build

if [ $? -eq 0 ]; then
    log_success "Containers construídos com sucesso!"
else
    log_error "Erro ao construir containers. Verifique os logs acima."
    exit 1
fi

cd "$PROJECT_ROOT"

echo ""

##############################################################################
# 5. Instruções Finais
##############################################################################

log_success "Setup completo!"
echo ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "  PRÓXIMOS PASSOS"
log_info "═══════════════════════════════════════════════════════════════"
echo ""
echo "1. Configure as credenciais OAuth:"
echo "   ${YELLOW}backend/.env${NC}  - DB_PASSWORD, OAuth Client IDs e Secrets, JWT_SECRET"
echo "   ${YELLOW}frontend/.env${NC} - OAuth Client IDs"
echo ""
echo "2. Para obter as credenciais OAuth:"
echo ""
echo "   ${BLUE}Google Cloud Console:${NC}"
echo "   - Acesse: https://console.cloud.google.com/apis/credentials"
echo "   - Crie um projeto novo ou use existente"
echo "   - Crie credenciais OAuth 2.0 Client ID"
echo "   - Adicione URL de redirecionamento: http://localhost:8080/api/oauth2/callback/google"
echo "   - Copie Client ID e Client Secret para os arquivos .env"
echo ""
echo "   ${BLUE}Microsoft Azure Portal:${NC}"
echo "   - Acesse: https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps"
echo "   - Registre um novo aplicativo"
echo "   - Configure redirect URI: http://localhost:8080/api/oauth2/callback/microsoft"
echo "   - Copie Application (client) ID e crie um Client Secret"
echo "   - Copie os valores para os arquivos .env"
echo ""
echo "3. Gere um JWT Secret seguro (256 bits):"
echo "   ${YELLOW}node -e \"console.log(require('crypto').randomBytes(32).toString('hex'))\"${NC}"
echo "   ou"
echo "   ${YELLOW}openssl rand -hex 32${NC}"
echo ""
echo "4. Inicie o ambiente de desenvolvimento:"
echo "   ${GREEN}./scripts/start-dev.sh${NC}"
echo ""
echo "5. Acesse a aplicação:"
echo "   - Frontend: ${GREEN}http://localhost:5173${NC}"
echo "   - Backend:  ${GREEN}http://localhost:8080/api/public/health${NC}"
echo ""
log_info "═══════════════════════════════════════════════════════════════"
echo ""

exit 0
