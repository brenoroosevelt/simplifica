import { config } from '@vue/test-utils'

// Mock CSS.supports para Vuetify
if (typeof window !== 'undefined') {
  window.CSS = window.CSS || {}
  window.CSS.supports = window.CSS.supports || (() => false)
}

// Configurações globais do Vue Test Utils
config.global.stubs = {
  teleport: true,
}
