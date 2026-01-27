import apiClient from './api'
import type {
  Institution,
  InstitutionCreateRequest,
  InstitutionUpdateRequest,
  InstitutionListParams,
  PageResponse,
  UserInstitution,
  AssignUserToInstitutionRequest,
  UserInstitutionRole,
} from '@/types/institution.types'

/**
 * Service para gerenciamento de instituições.
 * Responsável por todas as operações CRUD de instituições e
 * vinculação/desvinculação de usuários.
 */
class InstitutionService {
  private readonly BASE_PATH = '/institutions'
  private readonly USER_INSTITUTIONS_PATH = '/user/institutions'

  /**
   * Lista instituições com filtros e paginação.
   * @param params Parâmetros de listagem (page, size, sort, search, type, active)
   * @returns Promise com página de instituições
   */
  async list(params: InstitutionListParams): Promise<PageResponse<Institution>> {
    const response = await apiClient.get<PageResponse<Institution>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'asc'}` : undefined,
        search: params.search,
        type: params.type,
        active: params.active,
      },
    })
    return response.data
  }

  /**
   * Busca instituição por ID.
   * @param id ID da instituição
   * @returns Promise com dados da instituição
   */
  async getById(id: string): Promise<Institution> {
    const response = await apiClient.get<Institution>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria nova instituição.
   * Suporta upload de logo via FormData.
   * @param data Dados da nova instituição
   * @returns Promise com instituição criada
   */
  async create(data: InstitutionCreateRequest): Promise<Institution> {
    const formData = this.buildFormData(data)
    const response = await apiClient.post<Institution>(this.BASE_PATH, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  /**
   * Atualiza instituição existente.
   * Suporta atualização parcial dos campos.
   * @param id ID da instituição
   * @param data Dados a serem atualizados
   * @returns Promise com instituição atualizada
   */
  async update(id: string, data: InstitutionUpdateRequest): Promise<Institution> {
    const formData = this.buildFormData(data)
    const response = await apiClient.put<Institution>(`${this.BASE_PATH}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  /**
   * Remove instituição (soft delete).
   * @param id ID da instituição
   * @returns Promise<void>
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  /**
   * Retorna lista de instituições do usuário logado.
   * @returns Promise com lista de vínculos usuário-instituição
   */
  async getUserInstitutions(): Promise<UserInstitution[]> {
    const response = await apiClient.get<UserInstitution[]>(this.USER_INSTITUTIONS_PATH)
    return response.data
  }

  /**
   * Vincula usuário a uma instituição com roles específicos.
   * @param institutionId ID da instituição
   * @param userId ID do usuário
   * @param roles Roles a serem atribuídos
   * @returns Promise com vínculo criado
   */
  async linkUserToInstitution(
    institutionId: string,
    userId: string,
    roles: UserInstitutionRole[]
  ): Promise<UserInstitution> {
    const payload: AssignUserToInstitutionRequest = {
      userId,
      institutionId,
      roles,
    }

    const response = await apiClient.post<UserInstitution>(
      `${this.BASE_PATH}/${institutionId}/users`,
      payload
    )
    return response.data
  }

  /**
   * Remove vínculo entre usuário e instituição (soft delete).
   * @param institutionId ID da instituição
   * @param userId ID do usuário
   * @returns Promise<void>
   */
  async unlinkUserFromInstitution(institutionId: string, userId: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${institutionId}/users/${userId}`)
  }

  /**
   * Lista usuários vinculados a uma instituição.
   * @param institutionId ID da instituição
   * @returns Promise com lista de usuários vinculados
   */
  async getInstitutionUsers(institutionId: string): Promise<UserInstitution[]> {
    const response = await apiClient.get<UserInstitution[]>(
      `${this.BASE_PATH}/${institutionId}/users`
    )
    return response.data
  }

  /**
   * Valida se o usuário tem acesso a uma instituição.
   * @param institutionId ID da instituição
   * @returns Promise com objeto de validação
   */
  async validateInstitutionAccess(
    institutionId: string
  ): Promise<{ hasAccess: boolean; institutionId: string }> {
    const response = await apiClient.get<{ hasAccess: boolean; institutionId: string }>(
      `${this.USER_INSTITUTIONS_PATH}/${institutionId}/validate`
    )
    return response.data
  }

  /**
   * Lista instituições ativas de forma simplificada (sem paginação completa).
   * Útil para selects e autocompletes.
   * @param params Parâmetros opcionais de listagem
   * @returns Promise com página de instituições
   */
  async listInstitutions(params?: Partial<InstitutionListParams>): Promise<PageResponse<Institution>> {
    return this.list({
      page: params?.page || 0,
      size: params?.size || 1000,
      sort: params?.sort,
      direction: params?.direction,
      search: params?.search,
      type: params?.type,
      active: params?.active !== undefined ? params.active : true,
    })
  }

  /**
   * Constrói FormData a partir de objeto de requisição.
   * Converte campos para formato multipart/form-data necessário
   * para upload de arquivos (logo).
   * @param data Dados da instituição
   * @returns FormData pronto para envio
   */
  private buildFormData(
    data: InstitutionCreateRequest | InstitutionUpdateRequest
  ): FormData {
    const formData = new FormData()

    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (key === 'logo' && value instanceof File) {
          formData.append('logo', value)
        } else if (key !== 'logo') {
          formData.append(key, String(value))
        }
      }
    })

    return formData
  }
}

export const institutionService = new InstitutionService()
