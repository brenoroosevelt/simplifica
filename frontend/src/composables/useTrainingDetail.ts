import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { trainingService } from '@/services/training.service'
import type {
  Training,
  TrainingUpdateRequest,
  TrainingVideoCreateRequest,
  TrainingVideoUpdateRequest,
} from '@/types/training.types'

/**
 * Composable para gerenciar detalhes e operações de uma capacitação específica.
 * Usado nas páginas de visualização e edição de capacitações.
 */
export function useTrainingDetail(trainingId: string) {
  const router = useRouter()

  // State
  const training = ref<Training | null>(null)
  const isLoading = ref(false)
  const isSaving = ref(false)
  const error = ref<string | null>(null)

  // Computed
  const hasVideos = computed(() => training.value && training.value.videos.length > 0)
  const videoCount = computed(() => training.value?.videoCount || 0)
  const totalDuration = computed(() => training.value?.totalDurationMinutes || 0)

  // Methods
  async function loadTraining(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      training.value = await trainingService.getById(trainingId)
    } catch (err: any) {
      console.error('Failed to load training:', err)
      error.value = err.response?.data?.message || 'Erro ao carregar capacitação'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function updateTraining(data: TrainingUpdateRequest): Promise<Training> {
    isSaving.value = true
    error.value = null

    try {
      const updated = await trainingService.update(trainingId, data)
      training.value = updated
      return updated
    } catch (err: any) {
      console.error('Failed to update training:', err)
      error.value = err.response?.data?.message || 'Erro ao atualizar capacitação'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function deleteTraining(): Promise<void> {
    isSaving.value = true
    error.value = null

    try {
      await trainingService.delete(trainingId)
      training.value = null
    } catch (err: any) {
      console.error('Failed to delete training:', err)
      error.value = err.response?.data?.message || 'Erro ao excluir capacitação'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function uploadCoverImage(file: File): Promise<Training> {
    isSaving.value = true
    error.value = null

    try {
      const updated = await trainingService.uploadCoverImage(trainingId, file)
      training.value = updated
      return updated
    } catch (err: any) {
      console.error('Failed to upload cover image:', err)
      error.value = err.response?.data?.message || 'Erro ao fazer upload da capa'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function deleteCoverImage(): Promise<Training> {
    isSaving.value = true
    error.value = null

    try {
      const updated = await trainingService.deleteCoverImage(trainingId)
      training.value = updated
      return updated
    } catch (err: any) {
      console.error('Failed to delete cover image:', err)
      error.value = err.response?.data?.message || 'Erro ao remover capa'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function addVideo(data: TrainingVideoCreateRequest): Promise<void> {
    isSaving.value = true
    error.value = null

    try {
      await trainingService.addVideo(trainingId, data)
      // Reload training to get updated video list
      await loadTraining()
    } catch (err: any) {
      console.error('Failed to add video:', err)
      error.value = err.response?.data?.message || 'Erro ao adicionar vídeo'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function updateVideo(videoId: string, data: TrainingVideoUpdateRequest): Promise<void> {
    isSaving.value = true
    error.value = null

    try {
      await trainingService.updateVideo(trainingId, videoId, data)
      // Reload training to get updated video list
      await loadTraining()
    } catch (err: any) {
      console.error('Failed to update video:', err)
      error.value = err.response?.data?.message || 'Erro ao atualizar vídeo'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function deleteVideo(videoId: string): Promise<void> {
    isSaving.value = true
    error.value = null

    try {
      await trainingService.deleteVideo(trainingId, videoId)
      // Reload training to get updated video list
      await loadTraining()
    } catch (err: any) {
      console.error('Failed to delete video:', err)
      error.value = err.response?.data?.message || 'Erro ao remover vídeo'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function reorderVideos(videoIds: string[]): Promise<void> {
    isSaving.value = true
    error.value = null

    try {
      await trainingService.reorderVideos(trainingId, { videoIds })
      // Reload training to get updated video list
      await loadTraining()
    } catch (err: any) {
      console.error('Failed to reorder videos:', err)
      error.value = err.response?.data?.message || 'Erro ao reordenar vídeos'
      throw err
    } finally {
      isSaving.value = false
    }
  }

  function navigateToList(): void {
    router.push({ name: 'trainings' })
  }

  function navigateToEdit(): void {
    router.push({ name: 'training-edit', params: { id: trainingId } })
  }

  function navigateToDetail(): void {
    router.push({ name: 'training-detail', params: { id: trainingId } })
  }

  return {
    training,
    isLoading,
    isSaving,
    error,
    hasVideos,
    videoCount,
    totalDuration,
    loadTraining,
    updateTraining,
    deleteTraining,
    uploadCoverImage,
    deleteCoverImage,
    addVideo,
    updateVideo,
    deleteVideo,
    reorderVideos,
    navigateToList,
    navigateToEdit,
    navigateToDetail,
  }
}
