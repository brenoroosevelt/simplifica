import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

export function setupGuards(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore()

    // Verificar se a rota requer autenticação
    const requiresAuth = to.meta.requiresAuth === true

    // Verificar se há token no localStorage
    const token = localStorage.getItem('auth_token')

    // Se há token mas o store não está autenticado, tentar validar
    if (token && !authStore.isAuthenticated) {
      try {
        await authStore.checkAuth()
      } catch {
        // Token inválido, limpar localStorage
        localStorage.removeItem('auth_token')
      }
    }

    // Se a rota requer autenticação e usuário não está autenticado
    if (requiresAuth && !authStore.isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    // Se usuário autenticado tenta acessar login, redirecionar para dashboard
    if (to.name === 'login' && authStore.isAuthenticated) {
      next({ name: 'dashboard' })
      return
    }

    next()
  })
}
