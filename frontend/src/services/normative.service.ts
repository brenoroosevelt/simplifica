import apiClient from './api'
import type {
  Normative,
  NormativeCreateRequest,
  NormativeUpdateRequest,
  NormativeListParams,
  PageResponse,
} from '@/types/normative.types'

/**
 * Service para gerenciamento de normativos institucionais.
 * Responsável por todas as operações CRUD de normativos.
 */
class NormativeService {
  private readonly BASE_PATH = '/normatives'

  /**
   * Lista normativos com filtros e paginação.
   * Automaticamente filtrado pela instituição ativa.
   * @param params Parâmetros de listagem (page, size, sort, search)
   * @returns Promise com página de normativos
   */
  async list(params: NormativeListParams): Promise<PageResponse<Normative>> {
    const response = await apiClient.get<PageResponse<Normative>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'asc'}` : undefined,
        search: params.search || undefined,
      },
    })
    return response.data
  }

  /**
   * Busca normativo por ID.
   * @param id ID do normativo
   * @returns Promise com dados do normativo
   */
  async getById(id: string): Promise<Normative> {
    const response = await apiClient.get<Normative>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria novo normativo.
   * @param data Dados do novo normativo
   * @returns Promise com normativo criado
   */
  async create(data: NormativeCreateRequest): Promise<Normative> {
    const formData = this.buildFormData(data)
    const response = await apiClient.post<Normative>(this.BASE_PATH, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  /**
   * Atualiza normativo existente.
   * @param id ID do normativo
   * @param data Dados a serem atualizados
   * @returns Promise com normativo atualizado
   */
  async update(id: string, data: NormativeUpdateRequest): Promise<Normative> {
    const formData = this.buildFormData(data)
    const response = await apiClient.put<Normative>(`${this.BASE_PATH}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  /**
   * Remove o arquivo associado ao normativo.
   * @param id ID do normativo
   * @returns Promise com normativo atualizado (sem arquivo)
   */
  async deleteFile(id: string): Promise<Normative> {
    const response = await apiClient.delete<Normative>(`${this.BASE_PATH}/${id}/file`)
    return response.data
  }

  /**
   * Remove normativo.
   * @param id ID do normativo
   * @returns Promise<void>
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  private buildFormData(data: NormativeCreateRequest | NormativeUpdateRequest): FormData {
    const formData = new FormData()
    Object.entries(data).forEach(([key, value]) => {
      if (key === 'file' && value instanceof File) {
        formData.append('file', value)
      } else if (key !== 'file' && key !== 'removeFile' && value !== undefined && value !== null) {
        formData.append(key, String(value))
      }
    })
    return formData
  }
}

export const normativeService = new NormativeService()
