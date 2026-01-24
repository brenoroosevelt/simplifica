# Guia Rápido - Landing Page Simplifica

## Iniciar o Projeto

### Instalação

```bash
cd /home/breno/dev/claude-agents/frontend
npm install
```

### Desenvolvimento

```bash
# Iniciar servidor de desenvolvimento
npm run dev

# O frontend estará disponível em: http://localhost:5173
```

### Build de Produção

```bash
# Gerar build otimizado
npm run build

# Preview do build de produção
npm run preview
```

## Acessar a Landing Page

A landing page está configurada na rota raiz:

```
http://localhost:5173/
```

Ou, se usando o script de inicialização completo:

```bash
cd /home/breno/dev/claude-agents
./scripts/start-dev.sh
```

## Estrutura de Navegação

### Rotas Públicas

- `/` - Landing Page (não requer autenticação)
- `/login` - Página de login

### Rotas Privadas (requerem autenticação)

- `/dashboard` - Dashboard principal
- `/profile` - Perfil do usuário
- `/institution-selection` - Seleção de instituição
- `/admin/institutions` - Gestão de instituições (admin)
- `/admin/users` - Gestão de usuários (admin)
- `/admin/settings` - Configurações (admin)

## Navegação na Landing Page

A landing page possui scroll suave entre as seções. Clique nos itens do menu para navegar:

1. **Início** - Hero section
2. **Soluções** - Cards de soluções BPM
3. **Funcionalidades** - Recursos detalhados
4. **Benefícios** - Valor agregado
5. **Casos de Uso** - Segmentos atendidos
6. **Contato** - Formulário de captura de leads

## Temas Disponíveis

O Vuetify está configurado com 3 temas:

### 1. Institucional (Padrão - Recomendado)
```typescript
// Em /frontend/src/plugins/vuetify.ts
defaultTheme: 'institutionalTheme'
```

Paleta profissional com azul institucional (#2563EB), ideal para BPM corporativo.

### 2. Moderno
```typescript
defaultTheme: 'modernTheme'
```

Paleta pastel suave, cores mais claras e modernas.

### 3. Original
```typescript
defaultTheme: 'originalTheme'
```

Paleta vibrante do Material Design, cores saturadas.

### Como Trocar o Tema

Edite o arquivo `/frontend/src/plugins/vuetify.ts`:

```typescript
export default createVuetify({
  // ...
  theme: {
    defaultTheme: 'institutionalTheme', // Altere aqui
    themes: {
      originalTheme,
      modernTheme,
      institutionalTheme,
    },
  },
  // ...
})
```

## Personalização Rápida

### Alterar Cores do Tema

Edite `/frontend/src/plugins/vuetify.ts`:

```typescript
const institutionalTheme = {
  dark: false,
  colors: {
    primary: '#2563EB',    // Altere para sua cor primária
    secondary: '#475569',   // Cor secundária
    success: '#10B981',     // Verde de sucesso
    // ... outras cores
  },
}
```

### Modificar Textos da Landing

Cada seção tem seu conteúdo em arrays/objetos no `<script setup>`:

**Exemplo - Soluções** (`/frontend/src/components/landing/LandingSolutions.vue`):

```typescript
const solutions = [
  {
    icon: 'mdi-file-tree',
    title: 'Modelagem BPM', // Altere aqui
    description: 'Sua descrição personalizada',
    features: [
      'Feature 1',
      'Feature 2',
      // ...
    ],
  },
  // ... outras soluções
]
```

### Trocar Logo

Atualmente usa ícone temporário do Material Design:

```vue
<!-- Em LandingHeader.vue e LandingFooter.vue -->
<v-icon icon="mdi-chart-timeline-variant-shimmer" size="32" color="primary" />
```

Para usar logo personalizado:

1. Adicione o logo em `/frontend/public/logo.svg`
2. Substitua o `v-icon` por:

```vue
<img src="/logo.svg" alt="Simplifica" height="32" />
```

## Integração do Formulário de Contato

O formulário em `LandingFinalCTA.vue` está pronto para integração com backend.

### Configuração Atual (Mock)

```typescript
const handleSubmit = async () => {
  const { valid } = await contactForm.value.validate()

  if (valid) {
    // TODO: Integrar com API real
    setTimeout(() => {
      alert('Solicitação enviada!')
    }, 1500)
  }
}
```

### Integração com Backend

```typescript
import { api } from '@/services/api'

const handleSubmit = async () => {
  const { valid } = await contactForm.value.validate()

  if (valid) {
    isSubmitting.value = true

    try {
      await api.post('/api/leads', formData)
      showSnackbar('Solicitação enviada com sucesso!', 'success')
      contactForm.value.reset()
    } catch (error) {
      showSnackbar('Erro ao enviar solicitação', 'error')
    } finally {
      isSubmitting.value = false
    }
  }
}
```

## Arquivos Principais

### Componentes da Landing Page

```
/frontend/src/components/landing/
├── LandingHeader.vue        - Header fixo com menu
├── LandingHero.vue          - Hero section com CTAs
├── LandingSolutions.vue     - Cards de soluções
├── LandingBenefits.vue      - Benefícios e métricas
├── LandingFeatures.vue      - Funcionalidades detalhadas
├── LandingUseCases.vue      - Casos de uso por segmento
├── LandingSocialProof.vue   - Estatísticas e depoimentos
├── LandingFinalCTA.vue      - CTA final + formulário
└── LandingFooter.vue        - Footer completo
```

### View Principal

```
/frontend/src/views/public/
└── LandingPage.vue          - Orquestra todos os componentes
```

### Layout

```
/frontend/src/layouts/
└── PublicLayout.vue         - Layout para páginas públicas
```

### Configurações

```
/frontend/src/
├── plugins/vuetify.ts       - Temas e configurações Vuetify
├── router/index.ts          - Rotas da aplicação
└── App.vue                  - Componente raiz (tipografia)
```

## Responsividade

A landing page é totalmente responsiva:

- **Mobile**: < 600px
- **Tablet**: 600px - 1024px
- **Desktop**: > 1024px

Teste em diferentes dispositivos ou use as ferramentas de desenvolvedor do navegador.

## Dicas de Desenvolvimento

### Hot Reload

O Vite oferece hot reload instantâneo. Qualquer alteração nos componentes será refletida automaticamente no navegador.

### DevTools Vue

Instale a extensão Vue DevTools para Chrome/Firefox para melhor debug:
- Chrome: https://chrome.google.com/webstore/detail/vuejs-devtools/
- Firefox: https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/

### Vuetify Inspector

Para inspecionar componentes Vuetify, use:

```vue
<v-app v-model="$vuetify.theme">
  <!-- Habilita inspector de temas -->
</v-app>
```

### TypeScript Support

O projeto usa TypeScript. Para verificar tipos:

```bash
npm run type-check
# ou
vue-tsc --noEmit
```

## Próximos Passos

### 1. Integração Backend

- Criar endpoint `/api/leads` para captura de contatos
- Configurar serviço de e-mail (notificações)
- Implementar sistema de gestão de leads

### 2. Analytics

- Adicionar Google Analytics
- Configurar eventos de conversão
- Implementar tracking de formulários

### 3. SEO

- Adicionar meta tags (title, description, OG tags)
- Criar sitemap.xml
- Configurar robots.txt
- Implementar Schema.org markup

### 4. Otimizações

- Lazy loading de imagens
- Code splitting adicional
- PWA (Progressive Web App)
- Caching estratégico

### 5. Testes

- Testes unitários (Vitest)
- Testes E2E (Playwright/Cypress)
- Testes de acessibilidade (axe)
- Performance testing (Lighthouse)

## Troubleshooting

### Porta já em uso

Se a porta 5173 estiver em uso:

```bash
# Use outra porta
npm run dev -- --port 5174
```

### Erros de Build

Limpe o cache e node_modules:

```bash
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Problemas com Vuetify

Verifique se todas as dependências estão instaladas:

```bash
npm list vuetify
npm list @mdi/font
```

### TypeScript Errors

Execute o type-check:

```bash
npm run type-check
```

## Suporte

Para mais informações, consulte:

- **Documentação completa**: `/frontend/LANDING_PAGE_DOCUMENTATION.md`
- **Vue 3**: https://vuejs.org/
- **Vuetify 3**: https://vuetifyjs.com/
- **Vite**: https://vitejs.dev/

## Checklist de Deploy

Antes de fazer deploy para produção:

- [ ] Trocar logo temporário por logo real
- [ ] Configurar variáveis de ambiente (.env.production)
- [ ] Integrar formulário com backend
- [ ] Adicionar Google Analytics
- [ ] Configurar meta tags SEO
- [ ] Testar em múltiplos navegadores
- [ ] Validar responsividade
- [ ] Testar acessibilidade (WAVE, axe)
- [ ] Otimizar performance (Lighthouse score 90+)
- [ ] Configurar domínio customizado
- [ ] Habilitar HTTPS
- [ ] Configurar CDN (opcional)
- [ ] Testar formulário de captura de leads

---

**Desenvolvido com alta qualidade por especialista sênior em UI/UX**
