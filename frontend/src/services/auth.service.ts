import apiClient from './api'
import type { User } from '@/types/auth.types'

class AuthService {
  async getMe(): Promise<User> {
    const response = await apiClient.get<User>('/auth/me')
    return response.data
  }

  async validateToken(token: string): Promise<boolean> {
    try {
      const response = await apiClient.get('/auth/me', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      return response.status === 200
    } catch {
      return false
    }
  }
}

export const authService = new AuthService()
