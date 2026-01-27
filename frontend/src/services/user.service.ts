import apiClient from './api'
import type { UserInstitution } from '@/types/institution.types'
import type { User } from '@/types/auth.types'
import type {
  UserListItem,
  UserDetail,
  UpdateUserRequest,
  UpdateUserRolesRequest,
  LinkUserInstitutionRequest,
  PagedResponse,
  UserFilters,
  PaginationParams,
} from '@/types/user.types'

class UserService {
  /**
   * Buscar perfil do usuário
   */
  async getProfile(): Promise<User> {
    const response = await apiClient.get<User>('/user/profile')
    return response.data
  }

  /**
   * Buscar instituições vinculadas ao usuário com roles
   */
  async getUserInstitutions(): Promise<UserInstitution[]> {
    const response = await apiClient.get<UserInstitution[]>('/user/institutions')
    return response.data
  }

  /**
   * Validar vínculo com instituição específica
   */
  async validateInstitution(institutionId: string): Promise<boolean> {
    try {
      const response = await apiClient.get(`/user/institutions/${institutionId}/validate`)
      return response.status === 200
    } catch {
      return false
    }
  }

  /**
   * Listar usuários com filtros e paginação (Admin/Gestor)
   */
  async listUsers(
    filters: UserFilters,
    pagination: PaginationParams
  ): Promise<PagedResponse<UserListItem>> {
    const params = new URLSearchParams()

    // Pagination
    params.append('page', String(pagination.page - 1)) // Backend usa 0-indexed
    params.append('size', String(pagination.size))
    if (pagination.sort) {
      params.append('sort', pagination.sort)
    }

    // Filters
    if (filters.search) {
      params.append('search', filters.search)
    }
    if (filters.status) {
      params.append('status', filters.status)
    }
    if (filters.institutionId) {
      params.append('institutionId', filters.institutionId)
    }

    const response = await apiClient.get<PagedResponse<UserListItem>>(
      `/admin/users?${params.toString()}`
    )

    // Ajustar page para 1-indexed no frontend
    return {
      ...response.data,
      page: response.data.page + 1,
    }
  }

  /**
   * Buscar detalhes de um usuário específico
   */
  async getUserById(id: string): Promise<UserDetail> {
    const response = await apiClient.get<UserDetail>(`/admin/users/${id}`)
    return response.data
  }

  /**
   * Atualizar dados do usuário (nome, status)
   */
  async updateUser(id: string, data: UpdateUserRequest): Promise<UserDetail> {
    const response = await apiClient.put<UserDetail>(`/admin/users/${id}`, data)
    return response.data
  }

  /**
   * Atualizar roles do usuário em uma instituição
   */
  async updateUserRoles(userId: string, data: UpdateUserRolesRequest): Promise<void> {
    await apiClient.put(`/admin/users/${userId}/roles`, data)
  }

  /**
   * Vincular usuário a uma instituição com roles
   */
  async linkUserToInstitution(userId: string, data: LinkUserInstitutionRequest): Promise<void> {
    await apiClient.post(`/admin/users/${userId}/institutions`, data)
  }

  /**
   * Desvincular usuário de uma instituição
   */
  async unlinkUserFromInstitution(userId: string, institutionId: string): Promise<void> {
    await apiClient.delete(`/admin/users/${userId}/institutions/${institutionId}`)
  }
}

export const userService = new UserService()
