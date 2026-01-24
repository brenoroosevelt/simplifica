import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useInstitutionStore } from '@/stores/institution.store'

export function setupGuards(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore()
    const institutionStore = useInstitutionStore()

    // Verificar se a rota requer autenticação
    const requiresAuth = to.meta.requiresAuth === true
    const requiresInstitution = to.meta.requiresInstitution === true
    const requiresAdmin = to.meta.requiresAdmin === true

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

    // Verificar requisito de instituição
    if (requiresAuth && authStore.isAuthenticated && requiresInstitution) {
      // Buscar instituições do usuário se ainda não foram carregadas
      if (institutionStore.userInstitutions.length === 0 && !institutionStore.isLoading) {
        try {
          await institutionStore.fetchUserInstitutions()
        } catch (err) {
          console.error('Failed to fetch user institutions:', err)
        }
      }

      // Se usuário tem múltiplas instituições e não tem instituição ativa
      if (
        institutionStore.hasMultipleInstitutions &&
        !institutionStore.hasActiveInstitution &&
        to.name !== 'institution-selection'
      ) {
        next({ name: 'institution-selection', query: { redirect: to.fullPath } })
        return
      }

      // Se usuário não tem instituições
      if (
        institutionStore.userInstitutions.length === 0 &&
        !institutionStore.isLoading
      ) {
        // TODO: Redirecionar para página de "sem acesso"
        console.warn('User has no institutions assigned')
      }
    }

    // Verificar requisito de admin
    if (requiresAdmin && !authStore.isAdmin) {
      console.warn('User is not admin, redirecting to dashboard')
      next({ name: 'dashboard' })
      return
    }

    next()
  })
}
