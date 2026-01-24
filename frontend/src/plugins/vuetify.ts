import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import { pt } from 'vuetify/locale'
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

// Tema Original (vibrante)
const originalTheme = {
  dark: false,
  colors: {
    primary: '#1976D2',
    secondary: '#424242',
    accent: '#82B1FF',
    error: '#FF5252',
    info: '#2196F3',
    success: '#4CAF50',
    warning: '#FFC107',
    background: '#F5F5F5',
    surface: '#FFFFFF',
  },
}

// Tema Moderno (cores pastel e suaves - baseado em Material Design 3 e Tailwind)
const modernTheme = {
  dark: false,
  colors: {
    // Azul suave e moderno (slate-blue)
    primary: '#64748B', // slate-500
    // Cinza neutro
    secondary: '#71717A', // zinc-500
    // Accent roxo suave
    accent: '#A78BFA', // violet-400
    // Vermelho pastel
    error: '#F87171', // red-400
    // Azul céu suave
    info: '#38BDF8', // sky-400
    // Verde menta pastel
    success: '#4ADE80', // green-400
    // Âmbar suave
    warning: '#FBBF24', // amber-400
    // Background cinza muito claro
    background: '#F8FAFC', // slate-50
    // Surface branco puro
    surface: '#FFFFFF',
  },
}

// Tema Institucional (paleta profissional para landing page e sistema BPM)
const institutionalTheme = {
  dark: false,
  colors: {
    // Azul institucional suave e moderno (menos saturado que #2563EB)
    primary: '#3B82F6', // blue-500 - tom mais suave e tonal
    // Cinza neutro escuro
    secondary: '#475569', // slate-600
    // Accent violeta corporativo
    accent: '#8B5CF6', // violet-500
    // Vermelho para erros e alertas
    error: '#DC2626', // red-600
    // Azul céu para informações
    info: '#0EA5E9', // sky-500
    // Verde corporativo para sucesso
    success: '#10B981', // emerald-500
    // Âmbar para avisos
    warning: '#F59E0B', // amber-500
    // Background cinza muito claro
    background: '#F8FAFC', // slate-50
    // Surface branco puro
    surface: '#FFFFFF',
  },
}

const defaultDensity = 'compact'

export default createVuetify({
  components,
  directives,
  locale: {
    locale: 'pt',
    messages: { pt },
  },
  theme: {
    // Altere entre 'institutionalTheme', 'modernTheme' e 'originalTheme' aqui
    defaultTheme: 'institutionalTheme',
    themes: {
      originalTheme,
      modernTheme,
      institutionalTheme,
    },
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
  defaults: {
    VTextField: {
      variant: 'outlined',
      density: defaultDensity,
    },
    VTextarea: {
      variant: 'outlined',
      density: defaultDensity,
    },
    VSelect: {
      variant: 'outlined',
      density: defaultDensity,
    },
    VAutocomplete: {
      variant: 'outlined',
      density: defaultDensity,
    },
  },
  display: {
    mobileBreakpoint: 'md',
    thresholds: {
      xs: 0,
      sm: 600,
      md: 1024,
      lg: 1440,
      xl: 1920,
    },
  },
})
