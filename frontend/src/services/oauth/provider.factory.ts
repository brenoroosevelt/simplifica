import type { IOAuthProvider } from '@/types/auth.types'
import { OAuthProvider } from '@/types/auth.types'
import { GoogleOAuthProvider } from './google.provider'
import { MicrosoftOAuthProvider } from './microsoft.provider'

export class OAuthProviderFactory {
  static getProvider(provider: OAuthProvider): IOAuthProvider {
    switch (provider) {
      case OAuthProvider.GOOGLE:
        return new GoogleOAuthProvider()
      case OAuthProvider.MICROSOFT:
        return new MicrosoftOAuthProvider()
      default:
        throw new Error(`Unsupported OAuth provider: ${provider}`)
    }
  }
}
