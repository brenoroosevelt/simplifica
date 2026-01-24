# Documentação da Landing Page - Simplifica

## Visão Geral

Landing page profissional e institucional criada para o sistema BPM Simplifica, focada em gerar leads qualificados através de demonstração do produto e captura de contatos.

## Estrutura da Landing Page

A landing page está organizada nas seguintes seções:

### 1. Header (LandingHeader.vue)
- **Posição**: Fixo no topo
- **Elementos**:
  - Logo do Simplifica (ícone + texto)
  - Menu de navegação (Desktop): Início, Soluções, Funcionalidades, Benefícios, Casos de Uso, Contato
  - Botões de ação: "Entrar" (outline) e "Agendar demonstração" (primary)
  - Menu mobile responsivo (hambúrguer)
- **Comportamento**: Header com elevação ao scroll, scroll suave entre seções

### 2. Hero Section (LandingHero.vue)
- **Headline**: "Transforme a gestão de processos da sua instituição"
- **Subheadline**: Descrição sobre BPM, riscos e maturidade
- **CTAs**:
  - Primary: "Agendar demonstração"
  - Secondary: "Falar com especialista"
- **Estatísticas**: 500+ processos, 98% satisfação, 24/7 suporte
- **Visual**: Mockup animado do dashboard com cards de soluções
- **Decoração**: Elementos de fundo com blur e gradiente

### 3. Seção de Soluções (LandingSolutions.vue)
- **Cards de 4 soluções**:
  1. **Modelagem BPM**: Editor visual, biblioteca de templates, validação BPMN
  2. **Gestão de Riscos**: Matriz de riscos, análise de impacto, planos de mitigação
  3. **Maturidade Institucional**: Diagnóstico, métricas personalizadas, planos de evolução
  4. **Governança**: Trilhas de auditoria, relatórios gerenciais, controles de acesso
- **Características**:
  - Cards com hover effect (elevação)
  - Ícones gradiente únicos por solução
  - Lista de features com checkmarks

### 4. Seção de Benefícios (LandingBenefits.vue)
- **6 benefícios principais**:
  1. Padronização de Processos (60% redução no tempo)
  2. Redução de Riscos (85% menos incidentes)
  3. Transparência Total (100% rastreabilidade)
  4. Decisões Baseadas em Dados (3x mais velocidade)
  5. Conformidade e Auditoria (100% aprovação)
  6. Melhoria Contínua (+40% maturidade/ano)
- **Value Props**: ISO 9001, LGPD Compliant, Cloud & On-premise, Suporte dedicado
- **Métricas**: Cada benefício inclui métrica quantificável

### 5. Seção de Funcionalidades (LandingFeatures.vue)
- **Layout alternado** (imagem + texto / texto + imagem)
- **5 funcionalidades principais**:
  1. **Editor Visual de Processos BPMN**: Drag-and-drop, validação, templates
  2. **Mapa de Riscos Interativo**: Matriz personalizável, análise de impacto, alertas
  3. **Indicadores e KPIs em Tempo Real**: Dashboards personalizáveis, alertas de metas
  4. **Relatórios Gerenciais Avançados**: Executivos, auditorias, exportação múltiplos formatos
  5. **Trilha de Auditoria Completa**: Registro automático, histórico, rastreabilidade
- **Visual**: Cards animados com barras de progresso

### 6. Seção de Casos de Uso (LandingUseCases.vue)
- **4 segmentos principais**:
  1. **Instituições Públicas**: Governo, Autarquias, Fundações
  2. **Empresas Privadas**: Corporativo, Indústria, Serviços
  3. **Escritórios de Processos**: BPM Office, PMO, Gestão
  4. **Compliance e Auditoria**: GRC, Auditoria, Controles
- **Industry Logos**: Ícones de segmentos atendidos (Prefeituras, Tribunais, Autarquias, Empresas, Financeiras, Saúde)

### 7. Prova Social (LandingSocialProof.vue)
- **Estatísticas**:
  - 150+ Instituições atendidas
  - 2.500+ Processos mapeados
  - 5.000+ Usuários ativos
  - 4.8/5 Avaliação média
- **Depoimentos**: 3 testemunhos de gestores (Tribunal, Prefeitura, Empresa)
- **Certificações**: ISO 9001, LGPD, SSL/TLS, Backup Diário

### 8. CTA Final (LandingFinalCTA.vue)
- **Headline**: "Leve sua instituição para o próximo nível de maturidade"
- **Layout**: Grid 2 colunas (conteúdo + formulário)
- **Formulário de contato**:
  - Nome completo
  - E-mail institucional
  - Telefone
  - Instituição
  - Segmento (select)
  - Mensagem (opcional)
- **Validação**: Campos obrigatórios com regras
- **Features**: Demonstração gratuita, Implementação assistida, Suporte dedicado

### 9. Footer (LandingFooter.vue)
- **Estrutura em grid**:
  - Coluna 1: Logo, descrição, redes sociais
  - Coluna 2: Links de Soluções
  - Coluna 3: Links da Empresa
  - Coluna 4: Links de Suporte
  - Coluna 5: Informações de Contato
- **Footer Bottom**: Copyright, links legais, badges de certificação

## Design System

### Paleta de Cores (Tema Institucional)

```scss
primary: #2563EB    // Azul institucional (blue-600)
secondary: #475569  // Cinza slate (slate-600)
accent: #8B5CF6     // Violeta corporativo (violet-500)
error: #DC2626      // Vermelho (red-600)
info: #0EA5E9       // Azul céu (sky-500)
success: #10B981    // Verde esmeralda (emerald-500)
warning: #F59E0B    // Âmbar (amber-500)
background: #F8FAFC // Slate 50
surface: #FFFFFF    // Branco puro
```

### Tipografia

- **Fonte principal**: Inter (Google Fonts)
- **Pesos utilizados**: 400 (Regular), 500 (Medium), 600 (SemiBold), 700 (Bold), 800 (ExtraBold)
- **Features OpenType**: cv02, cv03, cv04, cv11 (para melhor legibilidade)

### Hierarquia Tipográfica

```scss
// Títulos de seção
.section-title: 32-44px, weight: 800, letter-spacing: -0.5px

// Subtítulos de seção
.section-subtitle: 18px, weight: 400, line-height: 1.6

// Hero title
.hero-title: 36-56px, weight: 800, letter-spacing: -1px

// Card titles
.card-title: 20-22px, weight: 700

// Body text
body: 15-16px, weight: 400-500, line-height: 1.6
```

### Espaçamento

- **Sistema de 8px**: Todos os espaçamentos são múltiplos de 8
- **Section padding**: 120px (desktop), 80px (tablet), 40px (mobile)
- **Cards gap**: 24px
- **Content gap**: 16px, 24px, 32px

### Sombras

```scss
// Sutil (cards)
box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04)

// Média (hover)
box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08)

// Intensa (modals, CTAs)
box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12)
```

### Border Radius

```scss
// Pequeno (tags, badges)
border-radius: 8px, 12px

// Médio (cards, buttons)
border-radius: 16px

// Grande (containers principais)
border-radius: 20px, 24px

// Circular (avatares, dots)
border-radius: 50%
```

## Animações e Micro-interações

### Transições

```scss
// Padrão suave
transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1)

// Rápida
transition: all 0.2s ease
```

### Hover Effects

- **Cards**: `translateY(-4px)` ou `translateY(-8px)` + aumento de sombra
- **Botões**: `translateY(-2px)` + aumento de sombra
- **Links**: Mudança de cor para primary
- **Ícones sociais**: Mudança de cor + background

### Animações Keyframes

```scss
// Float (mockup hero)
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

// GrowBar (funcionalidades)
@keyframes growBar {
  from { width: 0 !important; opacity: 0; }
  to { opacity: 1; }
}
```

## Responsividade

### Breakpoints (Vuetify)

```javascript
xs: 0-600px       // Mobile
sm: 600-1024px    // Tablet
md: 1024-1440px   // Desktop pequeno
lg: 1440-1920px   // Desktop médio
xl: 1920px+       // Desktop grande
```

### Ajustes por Breakpoint

#### Mobile (< 600px)
- Hero: Layout coluna única, CTAs full width
- Stats: Grid 2 colunas ou coluna única
- Cards: Coluna única
- Forms: Campos e botões full width
- Footer: Coluna única, elementos centralizados

#### Tablet (600-1024px)
- Hero: Layout coluna única ou 2 colunas
- Solutions: Grid 2 colunas
- Benefits: Grid 2 colunas
- Features: Layout coluna única (sem alternância)
- Footer: Grid 2-3 colunas

#### Desktop (> 1024px)
- Layout completo conforme design
- Grid systems em múltiplas colunas
- Layout alternado em funcionalidades
- Footer em 5 colunas

## Acessibilidade

### Boas Práticas Implementadas

- **HTML Semântico**: `<section>`, `<nav>`, `<footer>`, `<header>`
- **ARIA Labels**: Botões de ícones com `aria-label`
- **Contraste**: Ratio mínimo 4.5:1 para textos
- **Navegação por teclado**: Todos os elementos interativos acessíveis
- **Alt text**: Ícones decorativos sem alt, funcionais com label
- **Focus states**: Visible focus indicators

### Tamanhos Mínimos

- **Botões**: 48px altura mínima
- **Texto body**: 15-16px mínimo
- **Targets touch**: 44x44px mínimo

## Performance

### Otimizações Implementadas

- **Lazy loading**: Componentes carregados sob demanda
- **Imagens otimizadas**: Uso de SVG para ícones
- **CSS modular**: Estilos scoped por componente
- **Animações CSS**: Uso de `transform` e `opacity` (GPU-accelerated)
- **Scroll suave**: Implementação via JavaScript nativo

## Como Personalizar

### Trocar o Tema

Edite `/frontend/src/plugins/vuetify.ts`:

```typescript
defaultTheme: 'institutionalTheme', // ou 'modernTheme', 'originalTheme'
```

### Alterar Cores

No arquivo `vuetify.ts`, ajuste o objeto `institutionalTheme`:

```typescript
institutionalTheme: {
  dark: false,
  colors: {
    primary: '#SUA_COR',
    // ... outras cores
  },
}
```

### Modificar Textos

Cada componente tem seus textos definidos em arrays/objetos no `<script setup>`:

```typescript
// Exemplo em LandingSolutions.vue
const solutions = [
  {
    title: 'Seu Título',
    description: 'Sua descrição',
    // ...
  }
]
```

### Adicionar/Remover Seções

Edite `/frontend/src/views/public/LandingPage.vue`:

```vue
<template>
  <div class="landing-page">
    <LandingHero />
    <LandingSolutions />
    <!-- Adicione ou remova componentes aqui -->
  </div>
</template>
```

## Integração com Backend

### Formulário de Contato

O formulário em `LandingFinalCTA.vue` está preparado para integração:

```typescript
const handleSubmit = async () => {
  // TODO: Substituir pelo endpoint real
  // await api.post('/api/leads', formData)

  // Implementação atual: Simulação
  setTimeout(() => {
    alert('Solicitação enviada!')
  }, 1500)
}
```

### Próximos Passos

1. Criar endpoint `/api/leads` no backend
2. Configurar serviço de e-mail (notificações)
3. Implementar CRM ou sistema de gestão de leads
4. Adicionar tracking analytics (Google Analytics, Hotjar, etc.)

## Testes e Validação

### Checklist de Qualidade

- [x] Responsividade testada (mobile, tablet, desktop)
- [x] Navegação por teclado funcional
- [x] Scroll suave entre seções
- [x] Hover states em todos os elementos interativos
- [x] Validação de formulário implementada
- [x] Contraste de cores adequado (WCAG AA)
- [x] HTML semântico
- [x] Performance otimizada (animações GPU)

### Navegadores Suportados

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Opera 76+

## Arquivos Criados/Modificados

### Novos Componentes

- `/frontend/src/components/landing/LandingUseCases.vue`
- `/frontend/src/components/landing/LandingSocialProof.vue`
- `/frontend/src/components/landing/LandingFinalCTA.vue`
- `/frontend/src/components/landing/LandingFooter.vue`

### Componentes Existentes (não modificados)

- `/frontend/src/components/landing/LandingHeader.vue`
- `/frontend/src/components/landing/LandingHero.vue`
- `/frontend/src/components/landing/LandingSolutions.vue`
- `/frontend/src/components/landing/LandingBenefits.vue`
- `/frontend/src/components/landing/LandingFeatures.vue`

### Arquivos Modificados

- `/frontend/src/plugins/vuetify.ts` - Adicionado tema institucional
- `/frontend/src/App.vue` - Adicionada fonte Inter e otimizações tipográficas

### View Principal

- `/frontend/src/views/public/LandingPage.vue` - Orquestra todos os componentes

## Considerações Finais

A landing page foi desenvolvida seguindo os mais altos padrões de design UI/UX, com foco em:

1. **Profissionalismo**: Visual institucional e confiável
2. **Conversão**: CTAs estratégicos e formulário de captura
3. **Experiência**: Navegação fluida e micro-interações sutis
4. **Acessibilidade**: WCAG 2.1 nível AA
5. **Performance**: Animações otimizadas e código modular
6. **Responsividade**: Mobile-first, adaptável a todos os dispositivos
7. **Manutenibilidade**: Código limpo, componentizado e documentado

A landing page está pronta para uso e pode ser facilmente personalizada através dos arquivos de configuração e componentes modulares.
