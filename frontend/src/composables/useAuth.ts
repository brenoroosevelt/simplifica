import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import type { OAuthProvider } from '@/types/auth.types'

export function useAuth() {
  const authStore = useAuthStore()

  const login = async (provider: OAuthProvider) => {
    await authStore.loginWithProvider(provider)
  }

  const logout = () => {
    authStore.logout()
  }

  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const user = computed(() => authStore.currentUser)
  const isPending = computed(() => authStore.isPending)
  const isAdmin = computed(() => authStore.isAdmin)
  const isManager = computed(() => authStore.isManager)
  const canManageUsers = computed(() => authStore.canManageUsers)

  return {
    login,
    logout,
    isAuthenticated,
    user,
    isPending,
    isAdmin,
    isManager,
    canManageUsers,
  }
}
