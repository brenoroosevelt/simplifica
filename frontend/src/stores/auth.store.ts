import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/auth.types'
import { OAuthProvider } from '@/types/auth.types'
import { OAuthProviderFactory } from '@/services/oauth/provider.factory'
import { authService } from '@/services/auth.service'
import { userService } from '@/services/user.service'
import { useInstitutionStore } from './institution.store'
import type { UserInstitution, UserInstitutionRole } from '@/types/institution.types'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // Garantir que institutions seja sempre um array
  const _institutions = ref<UserInstitution[]>([])
  const institutions = computed({
    get: () => Array.isArray(_institutions.value) ? _institutions.value : [],
    set: (value) => {
      _institutions.value = Array.isArray(value) ? value : []
    }
  })

  // Getters
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const currentUser = computed(() => user.value)
  const isPending = computed(() => user.value?.status === 'PENDING')
  const isActive = computed(() => user.value?.status === 'ACTIVE')
  const isInactive = computed(() => user.value?.status === 'INACTIVE')

  // Verifica se o usuário tem role ADMIN na instituição ATIVA
  // IMPORTANTE: As permissões de admin devem respeitar a instituição atualmente selecionada
  // Apenas quando a instituição ATIVA for SIMP-ADMIN e o usuário tiver role ADMIN nela,
  // o usuário é considerado admin do sistema
  const isAdmin = computed(() => {
    const institutionStore = useInstitutionStore()

    console.log('[DEBUG isAdmin] Checking admin status...')
    console.log('[DEBUG isAdmin] Active institution:', institutionStore.activeInstitution?.acronym)
    console.log('[DEBUG isAdmin] Active institution ID:', institutionStore.activeInstitutionId)
    console.log('[DEBUG isAdmin] institutions.value:', institutions.value)

    if (!institutions.value || !Array.isArray(institutions.value) || institutions.value.length === 0) {
      console.log('[DEBUG isAdmin] No institutions found, returning false')
      return false
    }

    // Se não há instituição ativa, não é admin
    if (!institutionStore.activeInstitutionId) {
      console.log('[DEBUG isAdmin] No active institution, returning false')
      return false
    }

    // Verificar se a instituição ATIVA é SIMP-ADMIN e o usuário tem role ADMIN nela
    const activeInstitutionLink = institutions.value.find(
      ui => ui.institutionId === institutionStore.activeInstitutionId
    )

    if (!activeInstitutionLink) {
      console.log('[DEBUG isAdmin] User not linked to active institution, returning false')
      return false
    }

    const isSimpAdmin = activeInstitutionLink.institution?.acronym === 'SIMP-ADMIN'
    const hasAdminRole = activeInstitutionLink.roles &&
                        activeInstitutionLink.roles.includes('ADMIN' as UserInstitutionRole)

    console.log('[DEBUG isAdmin] Active institution check:', {
      institutionId: activeInstitutionLink.institutionId,
      acronym: activeInstitutionLink.institution?.acronym,
      isSimpAdmin,
      roles: activeInstitutionLink.roles,
      hasAdminRole
    })

    const result = isSimpAdmin && hasAdminRole
    console.log('[DEBUG isAdmin] Final result:', result)
    return result
  })

  // Verifica se o usuário tem role MANAGER (Gestor) na instituição ATIVA
  // IMPORTANTE: As permissões de gestor devem respeitar a instituição atualmente selecionada
  const isManager = computed(() => {
    const institutionStore = useInstitutionStore()

    if (!institutions.value || !Array.isArray(institutions.value) || institutions.value.length === 0) {
      return false
    }

    // Se não há instituição ativa, não é gestor
    if (!institutionStore.activeInstitutionId) {
      return false
    }

    // Verificar se o usuário tem role MANAGER na instituição ATIVA
    const activeInstitutionLink = institutions.value.find(
      ui => ui.institutionId === institutionStore.activeInstitutionId
    )

    if (!activeInstitutionLink) {
      return false
    }

    return activeInstitutionLink.roles &&
           activeInstitutionLink.roles.includes('MANAGER' as UserInstitutionRole)
  })

  // Verifica se o usuário pode gerenciar usuários (ADMIN ou MANAGER na instituição ativa)
  const canManageUsers = computed(() => isAdmin.value || isManager.value)

  // Actions
  async function loginWithProvider(provider: OAuthProvider): Promise<void> {
    const oauthProvider = OAuthProviderFactory.getProvider(provider)
    await oauthProvider.login()
  }

  async function handleCallback(tokenFromUrl: string): Promise<void> {
    if (!tokenFromUrl) {
      throw new Error('Token not found in callback')
    }

    // Salvar token
    token.value = tokenFromUrl
    localStorage.setItem('auth_token', tokenFromUrl)

    // Buscar dados do usuário
    await fetchUser()
  }

  async function fetchUser(): Promise<void> {
    try {
      const userData = await authService.getMe()
      user.value = userData

      // Buscar instituições do usuário automaticamente
      await fetchUserInstitutions()
    } catch (err) {
      console.error('Failed to fetch user:', err)
      logout()
      throw err
    }
  }

  async function fetchUserInstitutions(): Promise<void> {
    try {
      const userInstitutions = await userService.getUserInstitutions()
      // Garantir que sempre seja um array
      _institutions.value = Array.isArray(userInstitutions) ? userInstitutions : []
    } catch (err) {
      console.error('Failed to fetch user institutions:', err)
      _institutions.value = []
    }
  }

  async function checkAuth(): Promise<boolean> {
    const storedToken = localStorage.getItem('auth_token')

    if (!storedToken) {
      return false
    }

    token.value = storedToken

    try {
      await fetchUser()
      return true
    } catch {
      logout()
      return false
    }
  }

  function hasRoleInInstitution(institutionId: string, role: UserInstitutionRole): boolean {
    if (!Array.isArray(institutions.value)) return false

    const userInstitution = institutions.value.find((ui) => ui.institutionId === institutionId)
    if (!userInstitution || !Array.isArray(userInstitution.roles)) return false

    return userInstitution.roles.includes(role)
  }

  function logout(): void {
    user.value = null
    token.value = null
    _institutions.value = []
    localStorage.removeItem('auth_token')

    // Reset institution store
    const institutionStore = useInstitutionStore()
    institutionStore.reset()
  }

  return {
    // State
    user,
    token,
    institutions,
    // Getters
    isAuthenticated,
    currentUser,
    isPending,
    isActive,
    isInactive,
    isAdmin,
    isManager,
    canManageUsers,
    // Actions
    loginWithProvider,
    handleCallback,
    fetchUser,
    fetchUserInstitutions,
    checkAuth,
    hasRoleInInstitution,
    logout,
  }
})
