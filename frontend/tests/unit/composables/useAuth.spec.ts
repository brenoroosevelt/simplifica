import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuth } from '@/composables/useAuth'
import { useAuthStore } from '@/stores/auth.store'
import { OAuthProvider } from '@/types/auth.types'

vi.mock('@/services/auth.service', () => ({
  authService: {
    getMe: vi.fn(),
    validateToken: vi.fn(),
  },
}))

vi.mock('@/services/oauth/provider.factory', () => ({
  OAuthProviderFactory: {
    getProvider: vi.fn().mockReturnValue({
      login: vi.fn(),
      logout: vi.fn(),
      handleCallback: vi.fn(),
    }),
  },
}))

describe('useAuth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('should return auth composable with correct properties', () => {
    const auth = useAuth()

    expect(auth).toHaveProperty('login')
    expect(auth).toHaveProperty('logout')
    expect(auth).toHaveProperty('isAuthenticated')
    expect(auth).toHaveProperty('user')
    expect(auth).toHaveProperty('isPending')
    expect(auth).toHaveProperty('isAdmin')
  })

  it('should return isAuthenticated as false initially', () => {
    const { isAuthenticated } = useAuth()

    expect(isAuthenticated.value).toBe(false)
  })

  it('should call store login method when login is called', async () => {
    const authStore = useAuthStore()
    const loginSpy = vi.spyOn(authStore, 'loginWithProvider')

    const { login } = useAuth()
    await login(OAuthProvider.GOOGLE)

    expect(loginSpy).toHaveBeenCalledWith(OAuthProvider.GOOGLE)
  })

  it('should call store logout method when logout is called', () => {
    const authStore = useAuthStore()
    const logoutSpy = vi.spyOn(authStore, 'logout')

    const { logout } = useAuth()
    logout()

    expect(logoutSpy).toHaveBeenCalled()
  })

  it('should return user from store', () => {
    const authStore = useAuthStore()
    const mockUser = {
      id: '123',
      email: 'test@example.com',
      name: 'Test User',
      provider: OAuthProvider.GOOGLE,
      providerId: 'google-123',
      role: 'USER' as const,
      status: 'ACTIVE' as const,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }

    authStore.user = mockUser

    const { user } = useAuth()

    expect(user.value).toEqual(mockUser)
  })
})
