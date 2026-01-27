import type { OAuthProvider, UserStatus } from './auth.types'
import type { Institution, UserInstitutionRole } from './institution.types'

/**
 * Sumário de instituição para listagem
 */
export interface UserInstitutionSummary {
  institutionId: string
  institutionName: string
  institutionAcronym: string
}

/**
 * Usuário na listagem (simplificado)
 */
export interface UserListItem {
  id: string
  name: string
  email: string
  pictureUrl?: string
  provider: OAuthProvider
  status: UserStatus
  createdAt: string
  institutionCount?: number
  institutions?: UserInstitutionSummary[]
}

/**
 * Instituição vinculada ao usuário com roles
 */
export interface UserInstitutionDetail {
  id: string
  institutionId: string
  institution: Institution
  roles: UserInstitutionRole[]
  active: boolean
  linkedAt: string
}

/**
 * Detalhes completos do usuário
 */
export interface UserDetail {
  id: string
  name: string
  email: string
  pictureUrl?: string
  provider: OAuthProvider
  status: UserStatus
  createdAt: string
  updatedAt: string
  institutionCount?: number
  institutions: UserInstitutionDetail[]
}

/**
 * Request para atualizar usuário
 */
export interface UpdateUserRequest {
  name?: string
  status?: UserStatus
}

/**
 * Request para atualizar roles do usuário em uma instituição
 */
export interface UpdateUserRolesRequest {
  institutionId: string
  roles: UserInstitutionRole[]
}

/**
 * Request para vincular usuário a instituição
 */
export interface LinkUserInstitutionRequest {
  institutionId: string
  roles: UserInstitutionRole[]
}

/**
 * Resposta paginada genérica
 */
export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

/**
 * Filtros para listagem de usuários
 */
export interface UserFilters {
  search?: string
  status?: UserStatus | null
  institutionId?: string | null
}

/**
 * Parâmetros de paginação e ordenação
 */
export interface PaginationParams {
  page: number
  size: number
  sort?: string
}
