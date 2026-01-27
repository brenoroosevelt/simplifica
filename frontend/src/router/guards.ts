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

    // Validar status PENDING: bloquear acesso a todas as rotas exceto /profile e /auth/*
    if (
      requiresAuth &&
      authStore.isPending &&
      to.name !== 'profile' &&
      to.name !== 'auth-callback'
    ) {
      console.warn('User is PENDING, redirecting to profile')
      next({ name: 'profile' })
      return
    }

    // Buscar instituições do usuário autenticado se ainda não foram carregadas
    if (requiresAuth && authStore.isAuthenticated) {
      if (institutionStore.userInstitutions.length === 0 && !institutionStore.isLoading) {
        try {
          await institutionStore.fetchUserInstitutions()
        } catch (err) {
          console.error('Failed to fetch user institutions:', err)
        }
      }

      // Verificar requisito de instituição
      if (requiresInstitution) {
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
          /**
           * NOTA: Redirecionamento para página de "sem acesso" será implementado
           * na Trilha 5 junto com o sistema de gerenciamento de usuários.
           * Por ora, mantemos o console.warn para log de debug.
           */
          console.warn('User has no institutions assigned')
        }
      }
    }

    // Verificar requisito de gerenciamento de usuários (ADMIN ou MANAGER)
    const requiresUserManagement = to.meta.requiresUserManagement === true
    if (requiresUserManagement) {
      // CRÍTICO: Garantir que institutions foram carregadas
      if (authStore.institutions.length === 0) {
        console.warn('[SECURITY] User management check: institutions not loaded yet, fetching...')
        try {
          await authStore.fetchUserInstitutions()
        } catch (err) {
          console.error('[SECURITY] Failed to fetch institutions for user management check:', err)
        }
      }

      // Bloquear usuários PENDING
      if (authStore.isPending) {
        console.warn('[SECURITY] Pending user attempting to access user management, redirecting to profile')
        next({ name: 'profile' })
        return
      }

      // Verificar se usuário pode gerenciar usuários (ADMIN ou MANAGER na instituição ativa)
      if (!authStore.canManageUsers) {
        console.warn('[SECURITY] User cannot manage users, redirecting to dashboard', {
          isAdmin: authStore.isAdmin,
          isManager: authStore.isManager,
          institutions: authStore.institutions.map(i => ({
            acronym: i.institution?.acronym,
            roles: i.roles
          }))
        })
        next({ name: 'dashboard' })
        return
      }

      console.log('[SECURITY] User management check passed for route:', to.name)
    }

    // Verificar requisito de admin (apenas SIMP-ADMIN)
    if (requiresAdmin) {
      // CRÍTICO: Garantir que institutions foram carregadas antes de verificar isAdmin
      // Isso previne race condition onde isAdmin é verificado antes de institutions serem carregadas
      if (authStore.institutions.length === 0) {
        console.warn('[SECURITY] Admin check: institutions not loaded yet, fetching...')
        try {
          await authStore.fetchUserInstitutions()
        } catch (err) {
          console.error('[SECURITY] Failed to fetch institutions for admin check:', err)
        }
      }

      // Bloquear usuários PENDING de acessar rotas administrativas
      if (authStore.isPending) {
        console.warn('[SECURITY] Pending user attempting to access admin route, redirecting to profile')
        next({ name: 'profile' })
        return
      }

      // Verificar se usuário é admin (deve ter role ADMIN na instituição SIMP-ADMIN ativa)
      if (!authStore.isAdmin) {
        console.warn('[SECURITY] User is not admin, redirecting to dashboard', {
          institutions: authStore.institutions.map(i => ({
            acronym: i.institution?.acronym,
            roles: i.roles
          }))
        })
        next({ name: 'dashboard' })
        return
      }

      console.log('[SECURITY] Admin check passed for route:', to.name)
    }

    next()
  })
}
