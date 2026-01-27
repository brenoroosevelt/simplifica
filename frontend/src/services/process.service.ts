import apiClient from './api'
import type {
  Process,
  ProcessCreateRequest,
  ProcessUpdateRequest,
  ProcessListParams,
  PageResponse,
} from '@/types/process.types'

/**
 * Service para gerenciamento de processos.
 * Responsável por todas as operações CRUD de processos e mappings.
 */
class ProcessService {
  private readonly BASE_PATH = '/processes'

  /**
   * Lista processos com filtros e paginação.
   * Automaticamente filtrado pela instituição ativa.
   * @param params Parâmetros de listagem (page, size, sort, search, active, valueChainId, isCritical)
   * @returns Promise com página de processos
   */
  async list(params: ProcessListParams): Promise<PageResponse<Process>> {
    const response = await apiClient.get<PageResponse<Process>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'asc'}` : undefined,
        search: params.search,
        active: params.active,
        valueChainId: params.valueChainId,
        isCritical: params.isCritical,
      },
    })
    return response.data
  }

  /**
   * Busca processo por ID.
   * @param id ID do processo
   * @returns Promise com dados do processo
   */
  async getById(id: string): Promise<Process> {
    const response = await apiClient.get<Process>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria novo processo.
   * @param data Dados do novo processo
   * @returns Promise com processo criado
   */
  async create(data: ProcessCreateRequest): Promise<Process> {
    const response = await apiClient.post<Process>(this.BASE_PATH, data)
    return response.data
  }

  /**
   * Atualiza processo existente.
   * Suporta atualização parcial dos campos.
   * @param id ID do processo
   * @param data Dados a serem atualizados
   * @returns Promise com processo atualizado
   */
  async update(id: string, data: ProcessUpdateRequest): Promise<Process> {
    const response = await apiClient.put<Process>(`${this.BASE_PATH}/${id}`, data)
    return response.data
  }

  /**
   * Remove processo (soft delete).
   * @param id ID do processo
   * @returns Promise<void>
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  /**
   * Upload de múltiplos arquivos HTML de mapeamento.
   * @param processId ID do processo
   * @param files Array de arquivos HTML
   * @returns Promise com processo atualizado incluindo novos mappings
   */
  async uploadMappings(processId: string, files: File[]): Promise<Process> {
    const formData = new FormData()

    // Adiciona todos os arquivos ao FormData com a mesma chave
    files.forEach(file => {
      formData.append('files', file)
    })

    const response = await apiClient.post<Process>(
      `${this.BASE_PATH}/${processId}/mappings`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    )
    return response.data
  }

  /**
   * Remove um arquivo de mapeamento específico.
   * @param processId ID do processo
   * @param mappingId ID do mapping a ser removido
   * @returns Promise<void>
   */
  async deleteMapping(processId: string, mappingId: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${processId}/mappings/${mappingId}`)
  }
}

export const processService = new ProcessService()
