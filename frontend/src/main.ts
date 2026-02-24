import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './plugins/pinia'
import vuetify from './plugins/vuetify'

// Limpar localStorage de valores inválidos ANTES de inicializar a aplicação
const cleanInvalidLocalStorage = () => {
  const institutionId = localStorage.getItem('active_institution_id')
  if (institutionId === 'undefined' || institutionId === 'null') {
    console.warn('Removendo active_institution_id inválido do localStorage:', institutionId)
    localStorage.removeItem('active_institution_id')
  }
}

cleanInvalidLocalStorage()

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(vuetify)

app.mount('#app')
