import { BaseOAuthProvider } from './base.provider'

export class GoogleOAuthProvider extends BaseOAuthProvider {
  constructor() {
    super()
    // Client ID disponível em import.meta.env.VITE_OAUTH_GOOGLE_CLIENT_ID
  }

  async login(): Promise<void> {
    // Redirecionar para endpoint OAuth2 do backend (Google)
    const backendAuthUrl = `${this.apiBaseUrl}/oauth2/authorization/google`
    window.location.href = backendAuthUrl
  }

  logout(): void {
    // Logout específico do Google se necessário
    // Por enquanto, apenas limpeza local
  }

  async handleCallback(params: URLSearchParams): Promise<string | null> {
    // O token já vem do backend na query string
    return params.get('token')
  }
}
