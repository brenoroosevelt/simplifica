import type { IOAuthProvider } from '@/types/auth.types'

export abstract class BaseOAuthProvider implements IOAuthProvider {
  protected readonly redirectUri: string
  protected readonly apiBaseUrl: string

  constructor() {
    this.redirectUri = import.meta.env.VITE_OAUTH_REDIRECT_URI
    this.apiBaseUrl = import.meta.env.VITE_API_BASE_URL
  }

  abstract login(): Promise<void>
  abstract logout(): void
  abstract handleCallback(_params: URLSearchParams): Promise<string | null>
}
