import apiClient from './api'
import type {
  Unit,
  UnitCreateRequest,
  UnitUpdateRequest,
  UnitListParams,
  PageResponse,
  UnitImportResult,
} from '@/types/unit.types'

/**
 * Service para gerenciamento de unidades organizacionais.
 * Responsável por todas as operações CRUD de unidades.
 */
class UnitService {
  private readonly BASE_PATH = '/units'

  /**
   * Lista unidades com filtros e paginação.
   * Automaticamente filtrado pela instituição ativa.
   * @param params Parâmetros de listagem (page, size, sort, search, active)
   * @returns Promise com página de unidades
   */
  async list(params: UnitListParams): Promise<PageResponse<Unit>> {
    const response = await apiClient.get<PageResponse<Unit>>(this.BASE_PATH, {
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
   * Busca unidade por ID.
   * @param id ID da unidade
   * @returns Promise com dados da unidade
   */
  async getById(id: string): Promise<Unit> {
    const response = await apiClient.get<Unit>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria nova unidade.
   * @param data Dados da nova unidade
   * @returns Promise com unidade criada
   */
  async create(data: UnitCreateRequest): Promise<Unit> {
    const response = await apiClient.post<Unit>(this.BASE_PATH, data)
    return response.data
  }

  /**
   * Atualiza unidade existente.
   * Suporta atualização parcial dos campos.
   * Nota: sigla não pode ser alterada após criação.
   * @param id ID da unidade
   * @param data Dados a serem atualizados
   * @returns Promise com unidade atualizada
   */
  async update(id: string, data: UnitUpdateRequest): Promise<Unit> {
    const response = await apiClient.put<Unit>(`${this.BASE_PATH}/${id}`, data)
    return response.data
  }

  /**
   * Remove unidade (soft delete).
   * @param id ID da unidade
   * @returns Promise<void>
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  /**
   * Importa unidades a partir de arquivo CSV.
   * @param file Arquivo CSV a ser importado
   * @returns Promise com resultado da importação
   */
  async importUnitsFromCsv(file: File): Promise<UnitImportResult> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post<UnitImportResult>(
      `${this.BASE_PATH}/import-csv`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )

    return response.data
  }
}

export const unitService = new UnitService()
