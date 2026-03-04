# Sistema Simplifica

Sistema integrado de gestão de portfólio de processos e mapeamento de riscos.

## Índice

- [Descrição](#descrição)
- [Tech Stack](#tech-stack)
- [Pré-requisitos](#pré-requisitos)
- [Setup Inicial](#setup-inicial)
- [Configuração OAuth](#configuração-oauth)
- [Executando o Projeto](#executando-o-projeto)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Comandos Úteis](#comandos-úteis)
- [Desenvolvimento](#desenvolvimento)
- [Testes](#testes)
- [Troubleshooting](#troubleshooting)
- [Contribuindo](#contribuindo)

## Descrição

Simplifica é uma plataforma institucional completa para gestão estratégica e operacional. O sistema oferece:

- **Gestão de Portfólio de Processos**: Mapeamento, documentação e otimização de processos de negócio
- **Mapeamento de Riscos**: Identificação, análise e mitigação de riscos operacionais, financeiros e estratégicos
- **Cadeia de Valor**: Visualização e análise da cadeia de valor organizacional
- Autenticação OAuth2 segura (Google e Microsoft)
- Sistema de JWT para gerenciamento de sessões
- Interface moderna e responsiva com Vuetify 3
- API REST segura e bem documentada
- Arquitetura limpa e escalável
- Containerização completa com Docker

## Tech Stack

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.2** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 15** - Banco de dados relacional
- **Flyway** - Migrations de banco de dados
- **JWT (JJWT)** - Tokens de autenticação
- **Maven** - Gerenciamento de dependências
- **Docker** - Containerização

### Frontend
- **Vue 3** - Framework JavaScript progressivo
- **TypeScript** - Superset tipado do JavaScript
- **Vite** - Build tool e dev server
- **Vuetify 3** - Framework de componentes Material Design
- **Pinia** - State management
- **Vue Router 4** - Roteamento SPA
- **Axios** - Cliente HTTP
- **Vitest** - Framework de testes
- **ESLint + Prettier** - Linting e formatação

### DevOps
- **Docker Compose** - Orquestração de containers
- **GitHub Actions** - CI/CD (futuro)

## Pré-requisitos

Antes de começar, você precisa ter instalado em sua máquina:

- **Docker Desktop** (versão 20.10 ou superior)
  - [Download para Windows/Mac](https://www.docker.com/products/docker-desktop)
  - Linux: Instale Docker Engine + Docker Compose

- **Node.js 20+** (opcional, apenas para desenvolvimento local fora do Docker)
  - [Download](https://nodejs.org/)

- **Java 21+** (opcional, apenas para desenvolvimento local fora do Docker)
  - [Download OpenJDK](https://adoptium.net/)

- **Git**
  - [Download](https://git-scm.com/)

## Setup Inicial

### 1. Clone o repositório

```bash
git clone <repository-url>
cd simplifica
```

### 2. Execute o script de setup

```bash
./scripts/setup-dev.sh
```

Este script irá:
- Verificar se você tem Docker instalado
- Criar arquivos `.env` a partir dos `.env.example`
- Construir todas as imagens Docker
- Exibir próximos passos

### 3. Configure as variáveis de ambiente

Edite os arquivos `.env` criados:

#### Backend (.env)

```env
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=simplifica
DB_USER=admin
DB_PASSWORD=sua-senha-segura

# OAuth2 - Google
OAUTH_GOOGLE_CLIENT_ID=seu-google-client-id
OAUTH_GOOGLE_CLIENT_SECRET=seu-google-client-secret

# OAuth2 - Microsoft
OAUTH_MICROSOFT_CLIENT_ID=seu-microsoft-client-id
OAUTH_MICROSOFT_CLIENT_SECRET=seu-microsoft-client-secret

# Redirect URL base
OAUTH_REDIRECT_BASE_URL=http://localhost:8080/api

# JWT
JWT_SECRET=gere-uma-chave-de-256-bits-aqui
JWT_EXPIRATION_MS=86400000

# Spring
SPRING_PROFILES_ACTIVE=dev
```

#### Frontend (.env)

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_OAUTH_GOOGLE_CLIENT_ID=seu-google-client-id
VITE_OAUTH_MICROSOFT_CLIENT_ID=seu-microsoft-client-id
VITE_OAUTH_REDIRECT_URI=http://localhost:5173/auth/callback
VITE_APP_NAME=Simplifica
```

### 4. Gere um JWT Secret

Execute um dos comandos abaixo para gerar uma chave segura de 256 bits:

```bash
# Usando Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# Ou usando OpenSSL
openssl rand -hex 32
```

Copie o resultado para a variável `JWT_SECRET` no arquivo `backend/.env`.

## Configuração OAuth

### Google Cloud Console

1. Acesse [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Crie um novo projeto ou selecione um existente
3. Vá em "Credentials" > "Create Credentials" > "OAuth 2.0 Client ID"
4. Configure a tela de consentimento OAuth se solicitado
5. Tipo de aplicativo: **Web application**
6. Nome: `Simplifica - Dev`
7. **Authorized JavaScript origins**:
   - `http://localhost:5173`
8. **Authorized redirect URIs**:
   - `http://localhost:8080/api/oauth2/callback/google`
   - `http://localhost:5173/auth/callback`
9. Clique em "Create"
10. Copie o **Client ID** e **Client Secret** para os arquivos `.env`

### Microsoft Azure Portal

1. Acesse [Azure Portal - App Registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps)
2. Clique em "New registration"
3. Nome: `Simplifica - Dev`
4. Supported account types: **Accounts in any organizational directory and personal Microsoft accounts**
5. Redirect URI:
   - Platform: **Web**
   - URI: `http://localhost:8080/api/oauth2/callback/microsoft`
6. Clique em "Register"
7. Na página do app, copie o **Application (client) ID** para `OAUTH_MICROSOFT_CLIENT_ID`
8. Vá em "Certificates & secrets" > "New client secret"
9. Descrição: `Simplifica Dev Secret`
10. Expiration: 24 months (ou conforme preferência)
11. Copie o **Value** do secret para `OAUTH_MICROSOFT_CLIENT_SECRET`
12. Vá em "Authentication" e adicione mais uma redirect URI:
    - `http://localhost:5173/auth/callback`
13. Em "Implicit grant and hybrid flows", marque:
    - ✅ **ID tokens**

## Executando o Projeto

### Iniciar todos os serviços

```bash
./scripts/start-dev.sh
```

Aguarde alguns segundos para os serviços ficarem prontos. Acesse:

- **Frontend**: [http://localhost:5173](http://localhost:5173)
- **Backend API**: [http://localhost:8080/api/public/health](http://localhost:8080/api/public/health)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Parar os serviços

```bash
./scripts/stop-dev.sh
```

### Parar e remover containers

```bash
./scripts/stop-dev.sh --clean
```

### Resetar banco de dados

```bash
./scripts/reset-db.sh
```

**ATENÇÃO**: Este comando remove TODOS os dados do banco!

### Verificar saúde dos serviços

```bash
./scripts/health-check.sh
```

Este script verifica se todos os serviços estão rodando e respondendo corretamente.

## Estrutura do Projeto

```
simplifica/
├── backend/                      # Aplicação Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/simplifica/
│   │   │   │   ├── config/           # Configurações (Security, JWT, OAuth)
│   │   │   │   ├── domain/           # Entidades e enums
│   │   │   │   ├── infrastructure/   # Repositories
│   │   │   │   ├── application/      # Services e DTOs
│   │   │   │   └── presentation/     # Controllers e Exception Handlers
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/     # Scripts Flyway
│   │   └── test/                     # Testes unitários e integração
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                     # Aplicação Vue 3
│   ├── src/
│   │   ├── assets/               # Imagens, estilos globais
│   │   ├── components/           # Componentes Vue reutilizáveis
│   │   │   ├── auth/             # Componentes de autenticação
│   │   │   ├── common/           # Componentes genéricos
│   │   │   └── navigation/       # Header, Sidebar, Menu
│   │   ├── composables/          # Composables (useAuth, useDrawer)
│   │   ├── layouts/              # Layouts (Public, Private)
│   │   ├── plugins/              # Plugins (Vuetify, Router, Pinia)
│   │   ├── router/               # Configuração de rotas
│   │   ├── services/             # Services (API, OAuth)
│   │   ├── stores/               # Stores Pinia (auth, ui)
│   │   ├── types/                # TypeScript interfaces
│   │   ├── views/                # Páginas da aplicação
│   │   │   ├── public/           # Landing, Login
│   │   │   ├── auth/             # Callback
│   │   │   └── private/          # Dashboard, Profile
│   │   ├── App.vue
│   │   └── main.ts
│   ├── tests/                    # Testes Vitest
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
│
├── docker/                       # Orquestração Docker
│   ├── docker-compose.yml        # Desenvolvimento
│   ├── docker-compose.prod.yml   # Produção
│   └── postgres/
│       └── init.sql              # Script inicial do PostgreSQL
│
├── scripts/                      # Scripts de automação
│   ├── setup-dev.sh              # Setup inicial
│   ├── start-dev.sh              # Iniciar ambiente
│   ├── stop-dev.sh               # Parar ambiente
│   └── reset-db.sh               # Resetar banco
│
├── management/                   # Documentação de features
│   └── features/
│       └── feature-000-bootstrap.md
│
├── README.md                     # Este arquivo
├── CONTRIBUTING.md               # Guia de contribuição
└── .gitignore
```

## Comandos Úteis

### Docker Compose

```bash
# Ver logs de todos os serviços
docker-compose -f docker/docker-compose.yml logs -f

# Ver logs de um serviço específico
docker-compose -f docker/docker-compose.yml logs -f backend
docker-compose -f docker/docker-compose.yml logs -f frontend
docker-compose -f docker/docker-compose.yml logs -f postgres

# Reconstruir containers após mudanças no Dockerfile
docker-compose -f docker/docker-compose.yml up -d --build

# Executar comando no container
docker-compose -f docker/docker-compose.yml exec backend bash
docker-compose -f docker/docker-compose.yml exec frontend sh

# Ver status dos containers
docker-compose -f docker/docker-compose.yml ps
```

### Backend (dentro do container ou localmente)

```bash
# Executar testes
mvn test

# Executar testes de integração
mvn verify

# Verificar checkstyle
mvn checkstyle:check

# Compilar
mvn clean compile

# Build JAR
mvn clean package

# Executar migrations manualmente
mvn flyway:migrate
```

### Frontend (dentro do container ou localmente)

```bash
# Instalar dependências
npm install

# Executar em modo desenvolvimento
npm run dev

# Build para produção
npm run build

# Preview build de produção
npm run preview

# Executar testes
npm test

# Executar testes com UI
npm run test:ui

# Verificar lint
npm run lint

# Corrigir problemas de lint
npm run lint:fix

# Formatar código
npm run format
```

### PostgreSQL

```bash
# Conectar ao banco via psql
docker-compose -f docker/docker-compose.yml exec postgres psql -U admin -d simplifica

# Listar tabelas
\dt

# Ver schema de uma tabela
\d users

# Ver migrations aplicadas
SELECT * FROM flyway_schema_history;

# Sair do psql
\q
```

## Desenvolvimento

### Hot Reload

O ambiente de desenvolvimento está configurado com hot reload completo:

- **Frontend**: Vite com HMR (Hot Module Replacement) - mudanças refletem instantaneamente
- **Backend**: Spring DevTools - aplicação reinicia automaticamente ao detectar mudanças

### Adicionando um novo OAuth Provider

A arquitetura foi desenhada para ser extensível. Para adicionar um novo provider (ex: GitHub):

1. **Backend**:
   - Adicione configuração OAuth no `application.yml`
   - Crie `GitHubOAuth2UserInfo.java` em `config/security/oauth/providers/`
   - Atualize `OAuth2UserInfoFactory.java` com o novo case
   - Adicione novo valor no enum `OAuth2Provider`

2. **Frontend**:
   - Crie `github.provider.ts` em `services/oauth/`
   - Implemente a interface `IOAuthProvider`
   - Atualize a auth store para suportar o novo provider
   - Crie componente `GitHubLoginButton.vue`

### Padrões de Código

#### Backend
- Seguir princípios SOLID
- Usar Lombok para reduzir boilerplate
- Checkstyle configurado (max 120 caracteres por linha)
- Testes unitários com JUnit 5 e Mockito
- Testes de integração com Testcontainers

#### Frontend
- Composition API do Vue 3
- TypeScript strict mode
- ESLint + Prettier
- Componentes Single File Component (.vue)
- Testes com Vitest + Vue Test Utils

## Testes

### Backend

```bash
# Testes unitários (rápidos)
mvn test

# Testes de integração (com banco H2 ou Testcontainers)
mvn verify

# Executar um teste específico
mvn test -Dtest=JwtTokenProviderTest

# Gerar relatório de cobertura
mvn test jacoco:report
```

### Frontend

```bash
# Executar todos os testes
npm test

# Modo watch (re-executa ao alterar arquivos)
npm test -- --watch

# Interface gráfica
npm run test:ui

# Cobertura de código
npm test -- --coverage

# Executar um teste específico
npm test -- useAuth.spec.ts
```

## Troubleshooting

### Frontend não carrega / "Cannot connect to backend"

1. Verifique se o backend está rodando:
   ```bash
   curl http://localhost:8080/api/public/health
   ```

2. Verifique os logs do backend:
   ```bash
   docker-compose -f docker/docker-compose.yml logs backend
   ```

3. Verifique se a variável `VITE_API_BASE_URL` está correta no `frontend/.env`

### Hot reload do frontend não funciona

1. Certifique-se que `watch.usePolling: true` está configurado no `vite.config.ts`
2. Verifique se o volume está montado corretamente no `docker-compose.yml`:
   ```yaml
   volumes:
     - ../frontend:/app
     - /app/node_modules
   ```

### Backend não conecta no PostgreSQL

1. Verifique se o PostgreSQL está rodando:
   ```bash
   docker-compose -f docker/docker-compose.yml ps postgres
   ```

2. Verifique as credenciais no `backend/.env`:
   - `DB_HOST=postgres` (nome do serviço no docker-compose)
   - `DB_USER=admin`
   - `DB_PASSWORD=<sua-senha>`

3. Teste a conexão manualmente:
   ```bash
   docker-compose -f docker/docker-compose.yml exec postgres psql -U admin -d simplifica
   ```

### Migrations não executam

1. Verifique os logs do Flyway:
   ```bash
   docker-compose -f docker/docker-compose.yml logs backend | grep -i flyway
   ```

2. Verifique a tabela de controle:
   ```sql
   SELECT * FROM flyway_schema_history;
   ```

3. Se necessário, reset o banco:
   ```bash
   ./scripts/reset-db.sh
   ```

### OAuth não funciona / "Invalid redirect URI"

1. Verifique se as redirect URIs estão configuradas corretamente nos consoles:
   - Google: `http://localhost:8080/api/oauth2/callback/google`
   - Microsoft: `http://localhost:8080/api/oauth2/callback/microsoft`

2. Verifique se os Client IDs e Secrets estão corretos nos arquivos `.env`

3. Certifique-se que está usando `http://localhost` (não `127.0.0.1`)

### Port 5173 ou 8080 já está em uso

1. Identifique o processo usando a porta:
   ```bash
   # Linux/Mac
   lsof -i :5173
   lsof -i :8080

   # Windows
   netstat -ano | findstr :5173
   netstat -ano | findstr :8080
   ```

2. Pare o processo ou altere a porta no `docker-compose.yml` e arquivos `.env`

### Permissão negada ao executar scripts

```bash
chmod +x scripts/*.sh
```

## Contribuindo

Veja [CONTRIBUTING.md](CONTRIBUTING.md) para detalhes sobre como contribuir com o projeto.

## Licença

Este projeto é proprietário e confidencial.

## Contato

Para dúvidas ou suporte, entre em contato com a equipe de desenvolvimento.

---

**Desenvolvido com ❤️ usando Spring Boot, Vue 3 e Docker**
