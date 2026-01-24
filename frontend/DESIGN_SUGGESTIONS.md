# Sugestões de Design

## Paletas de Cores Disponíveis

O projeto possui duas paletas de cores configuradas no Vuetify. Para alternar entre elas, edite o arquivo:
`/frontend/src/plugins/vuetify.ts`

### Como Alternar

Na linha `defaultTheme`, altere entre:
```typescript
defaultTheme: 'modernTheme',  // Tema moderno (ATUAL)
// ou
defaultTheme: 'originalTheme', // Tema original vibrante
```

### 1. Tema Moderno (modernTheme) - ATIVO

Paleta pastel e suave, baseada em Material Design 3 e Tailwind CSS:

- **Primary**: `#64748B` (slate-500) - Azul acinzentado suave
- **Secondary**: `#71717A` (zinc-500) - Cinza neutro
- **Accent**: `#A78BFA` (violet-400) - Roxo pastel
- **Error**: `#F87171` (red-400) - Vermelho suave
- **Info**: `#38BDF8` (sky-400) - Azul céu
- **Success**: `#4ADE80` (green-400) - Verde menta
- **Warning**: `#FBBF24` (amber-400) - Âmbar suave
- **Background**: `#F8FAFC` (slate-50) - Cinza muito claro
- **Surface**: `#FFFFFF` - Branco puro

**Características**:
- Cores menos vibrantes e mais profissionais
- Ótimo para aplicações corporativas
- Melhor legibilidade e menos cansaço visual
- Segue padrões modernos de UI/UX

### 2. Tema Original (originalTheme)

Paleta vibrante do Material Design clássico:

- **Primary**: `#1976D2` - Azul vibrante
- **Secondary**: `#424242` - Cinza escuro
- **Accent**: `#82B1FF` - Azul claro brilhante
- **Error**: `#FF5252` - Vermelho vibrante
- **Info**: `#2196F3` - Azul médio
- **Success**: `#4CAF50` - Verde vibrante
- **Warning**: `#FFC107` - Amarelo âmbar
- **Background**: `#F5F5F5` - Cinza claro
- **Surface**: `#FFFFFF` - Branco

**Características**:
- Cores mais fortes e chamativas
- Material Design clássico
- Boa para aplicações que precisam destaque

---

# Header do App

## Cores Azuis Modernas para o Header

Aqui estão algumas opções de cores azuis modernas e elegantes para o header do app (menos grosseiras que o primary padrão):

### 1. **Slate Blue (ATUAL)** - Recomendado
- **Background**: `#F8FAFC` (slate-50 muito claro)
- **Text**: `#334155` (slate-700)
- **Border**: `#E2E8F0` (slate-200)
- **Estilo**: Muito sutil, clean e profissional

### 2. **Sky Blue** - Moderno e vibrante
```vue
<v-app-bar elevation="0" color="#E0F2FE" style="border-bottom: 1px solid #BAE6FD;">
  <v-toolbar-title class="font-weight-medium" style="color: #075985;">
```
- **Background**: `#E0F2FE` (sky-100)
- **Text**: `#075985` (sky-800)
- **Border**: `#BAE6FD` (sky-200)

### 3. **Indigo** - Elegante e corporativo
```vue
<v-app-bar elevation="0" color="#EEF2FF" style="border-bottom: 1px solid #C7D2FE;">
  <v-toolbar-title class="font-weight-medium" style="color: #4338CA;">
```
- **Background**: `#EEF2FF` (indigo-50)
- **Text**: `#4338CA` (indigo-700)
- **Border**: `#C7D2FE` (indigo-200)

### 4. **Blue Suave** - Equilibrado
```vue
<v-app-bar elevation="0" color="#EFF6FF" style="border-bottom: 1px solid #BFDBFE;">
  <v-toolbar-title class="font-weight-medium" style="color: #1E40AF;">
```
- **Background**: `#EFF6FF` (blue-50)
- **Text**: `#1E40AF` (blue-700)
- **Border**: `#BFDBFE` (blue-200)

### 5. **Cyan Tech** - Tecnológico e moderno
```vue
<v-app-bar elevation="0" color="#ECFEFF" style="border-bottom: 1px solid #A5F3FC;">
  <v-toolbar-title class="font-weight-medium" style="color: #0E7490;">
```
- **Background**: `#ECFEFF` (cyan-50)
- **Text**: `#0E7490` (cyan-700)
- **Border**: `#A5F3FC` (cyan-200)

### 6. **Gradient Moderno** - Premium
```vue
<v-app-bar
  elevation="0"
  style="background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%); border-bottom: none;"
>
  <v-toolbar-title class="font-weight-medium text-white">
```
- Gradiente roxo-azul moderno
- Texto branco

## Como Aplicar

Edite o arquivo: `/frontend/src/components/navigation/AppHeader.vue`

Substitua a linha do `<v-app-bar>` por uma das opções acima.

## Opção Atual

A opção atual é **Slate Blue** - um design muito clean e minimalista com fundo quase branco e borda sutil.

Para algo mais colorido mas ainda moderno, recomendo o **Sky Blue** ou **Indigo**.
