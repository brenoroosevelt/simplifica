import apiClient from './api'
import type {
  Training,
  TrainingCreateRequest,
  TrainingUpdateRequest,
  TrainingVideoCreateRequest,
  TrainingVideoUpdateRequest,
  ReorderVideosRequest,
  TrainingListParams,
  PageResponse,
  TrainingVideo,
} from '@/types/training.types'

/**
 * Service para gerenciamento de capacitações (treinamentos).
 * Responsável por todas as operações CRUD de trainings e vídeos.
 */
class TrainingService {
  private readonly BASE_PATH = '/trainings'

  /**
   * Lista capacitações com filtros e paginação.
   * Automaticamente filtrado pela instituição ativa.
   * @param params Parâmetros de listagem (page, size, sort, search, active)
   * @returns Promise com página de capacitações
   */
  async list(params: TrainingListParams): Promise<PageResponse<Training>> {
    const response = await apiClient.get<PageResponse<Training>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'desc'}` : undefined,
        search: params.search,
        active: params.active,
      },
    })
    return response.data
  }

  /**
   * Busca capacitação por ID.
   * @param id ID da capacitação
   * @returns Promise com dados da capacitação
   */
  async getById(id: string): Promise<Training> {
    const response = await apiClient.get<Training>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria nova capacitação.
   * @param data Dados da nova capacitação
   * @returns Promise com capacitação criada
   */
  async create(data: TrainingCreateRequest): Promise<Training> {
    const response = await apiClient.post<Training>(this.BASE_PATH, data)
    return response.data
  }

  /**
   * Atualiza capacitação existente.
   * @param id ID da capacitação
   * @param data Dados a serem atualizados
   * @returns Promise com capacitação atualizada
   */
  async update(id: string, data: TrainingUpdateRequest): Promise<Training> {
    const response = await apiClient.put<Training>(`${this.BASE_PATH}/${id}`, data)
    return response.data
  }

  /**
   * Remove capacitação (soft delete).
   * @param id ID da capacitação
   * @returns Promise<void>
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }

  /**
   * Upload de imagem de capa para capacitação.
   * @param id ID da capacitação
   * @param file Arquivo de imagem
   * @returns Promise com capacitação atualizada
   */
  async uploadCoverImage(id: string, file: File): Promise<Training> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post<Training>(
      `${this.BASE_PATH}/${id}/cover-image`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    )
    return response.data
  }

  /**
   * Remove imagem de capa da capacitação.
   * @param id ID da capacitação
   * @returns Promise com capacitação atualizada
   */
  async deleteCoverImage(id: string): Promise<Training> {
    const response = await apiClient.delete<Training>(`${this.BASE_PATH}/${id}/cover-image`)
    return response.data
  }

  /**
   * Adiciona vídeo à capacitação.
   * @param trainingId ID da capacitação
   * @param data Dados do vídeo
   * @returns Promise com vídeo criado
   */
  async addVideo(trainingId: string, data: TrainingVideoCreateRequest): Promise<TrainingVideo> {
    const response = await apiClient.post<TrainingVideo>(
      `${this.BASE_PATH}/${trainingId}/videos`,
      data
    )
    return response.data
  }

  /**
   * Atualiza vídeo existente.
   * @param trainingId ID da capacitação
   * @param videoId ID do vídeo
   * @param data Dados a serem atualizados
   * @returns Promise com vídeo atualizado
   */
  async updateVideo(
    trainingId: string,
    videoId: string,
    data: TrainingVideoUpdateRequest
  ): Promise<TrainingVideo> {
    const response = await apiClient.put<TrainingVideo>(
      `${this.BASE_PATH}/${trainingId}/videos/${videoId}`,
      data
    )
    return response.data
  }

  /**
   * Remove vídeo da capacitação.
   * @param trainingId ID da capacitação
   * @param videoId ID do vídeo
   * @returns Promise<void>
   */
  async deleteVideo(trainingId: string, videoId: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${trainingId}/videos/${videoId}`)
  }

  /**
   * Reordena vídeos da capacitação.
   * @param trainingId ID da capacitação
   * @param data Lista ordenada de IDs dos vídeos
   * @returns Promise com lista de vídeos reordenados
   */
  async reorderVideos(trainingId: string, data: ReorderVideosRequest): Promise<TrainingVideo[]> {
    const response = await apiClient.put<TrainingVideo[]>(
      `${this.BASE_PATH}/${trainingId}/videos/reorder`,
      data
    )
    return response.data
  }
}

export const trainingService = new TrainingService()
