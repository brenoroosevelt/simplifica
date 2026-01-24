import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/auth.types'
import { OAuthProvider } from '@/types/auth.types'
import { OAuthProviderFactory } from '@/services/oauth/provider.factory'
import { authService } from '@/services/auth.service'
import { useInstitutionStore } from './institution.store'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const currentUser = computed(() => user.value)
  const isPending = computed(() => user.value?.status === 'PENDING')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

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
    } catch (err) {
      console.error('Failed to fetch user:', err)
      logout()
      throw err
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

  function logout(): void {
    user.value = null
    token.value = null
    localStorage.removeItem('auth_token')

    // Reset institution store
    const institutionStore = useInstitutionStore()
    institutionStore.reset()
  }

  return {
    // State
    user,
    token,
    // Getters
    isAuthenticated,
    currentUser,
    isPending,
    isAdmin,
    // Actions
    loginWithProvider,
    handleCallback,
    fetchUser,
    checkAuth,
    logout,
  }
})
