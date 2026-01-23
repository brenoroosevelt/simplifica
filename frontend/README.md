# Simplifica - Frontend

Frontend moderno construído com Vue 3, Vite, Vuetify 3 e TypeScript.

## Tecnologias

- **Vue 3** - Framework JavaScript progressivo
- **Vite** - Build tool e dev server
- **Vuetify 3** - Framework de componentes Material Design
- **TypeScript** - Tipagem estática
- **Pinia** - State management
- **Vue Router** - Roteamento
- **Axios** - Cliente HTTP
- **Vitest** - Framework de testes

## Estrutura do Projeto

```
frontend/
├── src/
│   ├── assets/          # Assets estáticos
│   ├── components/      # Componentes Vue
│   │   ├── auth/        # Componentes de autenticação
│   │   ├── common/      # Componentes comuns
│   │   └── navigation/  # Componentes de navegação
│   ├── composables/     # Composables Vue
│   ├── layouts/         # Layouts (Public, Private)
│   ├── plugins/         # Plugins (Vuetify, Pinia)
│   ├── router/          # Configuração de rotas
│   ├── services/        # Serviços e API
│   │   └── oauth/       # Providers OAuth2
│   ├── stores/          # Pinia stores
│   ├── types/           # TypeScript types
│   ├── views/           # Páginas/Views
│   │   ├── public/      # Páginas públicas
│   │   ├── auth/        # Páginas de autenticação
│   │   └── private/     # Páginas privadas
│   └── utils/           # Utilitários
├── tests/               # Testes unitários
└── public/              # Assets públicos
```

## Scripts Disponíveis

```bash
# Desenvolvimento
npm run dev

# Build de produção
npm run build

# Preview do build
npm run preview

# Testes
npm test
npm run test:ui

# Lint
npm run lint

# Formatação
npm run format
```

## Variáveis de Ambiente

Copie o arquivo `.env.example` para `.env` e configure as variáveis:

```bash
cp .env.example .env
```

Variáveis necessárias:

- `VITE_API_BASE_URL` - URL base da API backend
- `VITE_OAUTH_GOOGLE_CLIENT_ID` - Client ID do Google OAuth
- `VITE_OAUTH_MICROSOFT_CLIENT_ID` - Client ID do Microsoft OAuth
- `VITE_OAUTH_REDIRECT_URI` - URI de callback do OAuth
- `VITE_APP_NAME` - Nome da aplicação

## Autenticação OAuth2

O frontend suporta autenticação via:

- **Google OAuth2**
- **Microsoft OAuth2**

A arquitetura é extensível, permitindo adicionar novos providers facilmente através do padrão Strategy implementado em `src/services/oauth/`.

### Fluxo de Autenticação

1. Usuário clica em "Entrar com Google/Microsoft"
2. Redirecionamento para backend OAuth endpoint
3. Backend processa OAuth e retorna token JWT
4. Frontend recebe token via callback
5. Token é armazenado no localStorage
6. Usuário é redirecionado para dashboard

## Layouts

### Public Layout
- Header simples com botão "Entrar"
- Footer com informações
- Usado em: Landing Page, Login

### Private Layout
- Header com menu de usuário
- Sidebar responsiva (rail no desktop, temporary no mobile)
- Menu de navegação
- Usado em: Dashboard, Profile

## State Management

### Auth Store (`stores/auth.store.ts`)
- Gerencia estado de autenticação
- Armazena dados do usuário
- Persiste token no localStorage

### UI Store (`stores/ui.store.ts`)
- Gerencia estado da interface
- Controla abertura/fechamento do drawer
- Detecta mobile/desktop

## Responsividade

O layout é totalmente responsivo:

- **Mobile (< 1024px)**: Drawer temporary (sobrepõe conteúdo)
- **Desktop (>= 1024px)**: Drawer permanent rail ou expanded

## Testes

Os testes são escritos com Vitest e Vue Test Utils:

```bash
# Executar testes
npm test

# Executar testes com UI
npm run test:ui

# Coverage
npm test -- --coverage
```

## Docker

O frontend pode ser executado via Docker:

```bash
# Build da imagem
docker build -t claude-agents-frontend .

# Executar container
docker run -p 5173:5173 claude-agents-frontend
```

Ou usando Docker Compose (na raiz do projeto):

```bash
cd ../docker
docker-compose up frontend
```

O Docker está configurado com:
- Hot reload funcionando (watch.usePolling)
- Volumes para código fonte
- Porta 5173 exposta

## Contribuindo

1. Sempre execute testes antes de commitar: `npm test`
2. Verifique lint: `npm run lint`
3. Certifique-se que o build funciona: `npm run build`
4. Siga o padrão de código existente

## Troubleshooting

### Hot reload não funciona no Docker
- Certifique-se que `watch.usePolling: true` está configurado no `vite.config.ts`

### Erros de CORS
- Verifique se `VITE_API_BASE_URL` está correta
- Verifique configuração de CORS no backend

### OAuth não funciona
- Verifique se os Client IDs estão configurados
- Certifique-se que o redirect URI está registrado no provider OAuth
- Verifique logs do backend para erros de autenticação
