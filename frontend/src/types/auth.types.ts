export enum OAuthProvider {
  // eslint-disable-next-line no-unused-vars
  GOOGLE = 'GOOGLE',
  // eslint-disable-next-line no-unused-vars
  MICROSOFT = 'MICROSOFT',
}

export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE'

export interface User {
  id: string
  email: string
  name: string
  pictureUrl?: string
  provider: OAuthProvider
  providerId: string
  role: UserRole
  status: UserStatus
  createdAt: string
  updatedAt: string
}

export interface AuthState {
  isAuthenticated: boolean
  user: User | null
  token: string | null
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  user: User
}

export interface IOAuthProvider {
  login(): Promise<void>
  logout(): void
  handleCallback(_params: URLSearchParams): Promise<string | null>
}
