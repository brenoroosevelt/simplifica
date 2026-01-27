import apiClient from './api'
import type {
  ValueChain,
  ValueChainCreateRequest,
  ValueChainUpdateRequest,
  ValueChainListParams,
  PageResponse,
} from '@/types/valueChain.types'

/**
 * Service para gerenciamento de cadeias de valor.
 * Responsável por todas as operações CRUD de cadeias de valor.
 */
class ValueChainService {
  private readonly BASE_PATH = '/value-chains'

  /**
   * Lista cadeias de valor com filtros e paginação.
   * @param params Parâmetros de listagem (page, size, sort, search, active)
   * @returns Promise com página de cadeias de valor
   */
  async list(params: ValueChainListParams): Promise<PageResponse<ValueChain>> {
    const response = await apiClient.get<PageResponse<ValueChain>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'asc'}` : undefined,
        search: params.search,
        active: params.active,
      },
    })
    return response.data
  }

  /**
   * Busca cadeia de valor por ID.
   * @param id ID da cadeia de valor
   * @returns Promise com dados da cadeia de valor
   */
  async getById(id: string): Promise<ValueChain> {
    const response = await apiClient.get<ValueChain>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria nova cadeia de valor.
   * Suporta upload de imagem via FormData.
   * @param data Dados da nova cadeia de valor
   * @returns Promise com cadeia de valor criada
   */
  async create(data: ValueChainCreateRequest): Promise<ValueChain> {
    const formData = this.buildFormData(data)
    const response = await apiClient.post<ValueChain>(this.BASE_PATH, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  /**
   * Atualiza cadeia de valor existente.
   * Suporta atualização parcial dos campos.
   * @param id ID da cadeia de valor
   * @param data Dados a serem atualizados
   * @returns Promise com cadeia de valor atualizada
   */
  async update(id: string, data: ValueChainUpdateRequest): Promise<ValueChain> {
    const formData = this.buildFormData(data)
    const response = await apiClient.put<ValueChain>(`${this.BASE_PATH}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  }

  /**
   * Remove imagem de uma cadeia de valor.
   * @param id ID da cadeia de valor
   * @returns Promise<void>
   */
  async deleteImage(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}/image`)
  }

  /**
   * Remove cadeia de valor (soft delete).
   * @param id ID da cadeia de valor
   * @returns Promise<void>
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  /**
   * Constrói FormData a partir de objeto de requisição.
   * Converte campos para formato multipart/form-data necessário
   * para upload de arquivos (imagem).
   * @param data Dados da cadeia de valor
   * @returns FormData pronto para envio
   */
  private buildFormData(
    data: ValueChainCreateRequest | ValueChainUpdateRequest
  ): FormData {
    const formData = new FormData()

    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (key === 'image' && value instanceof File) {
          formData.append('image', value)
        } else if (key !== 'image') {
          formData.append(key, String(value))
        }
      }
    })

    return formData
  }
}

export const valueChainService = new ValueChainService()
