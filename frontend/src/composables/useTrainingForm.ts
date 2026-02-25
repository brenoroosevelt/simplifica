import { ref } from 'vue'
import { trainingService } from '@/services/training.service'
import type {
  Training,
  TrainingCreateRequest,
  TrainingUpdateRequest,
  TrainingVideo,
  TrainingVideoCreateRequest,
  TrainingVideoUpdateRequest,
} from '@/types/training.types'

export function useTrainingForm() {
  // State
  const training = ref<Training | null>(null)
  const isLoading = ref(false)
  const isSaving = ref(false)

  // Methods
  async function loadTraining(id: string): Promise<void> {
    isLoading.value = true
    try {
      training.value = await trainingService.getById(id)
    } catch (err) {
      console.error('Failed to load training:', err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function createTraining(data: TrainingCreateRequest): Promise<Training> {
    isSaving.value = true
    try {
      const created = await trainingService.create(data)
      training.value = created
      return created
    } catch (err) {
      console.error('Failed to create training:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function updateTraining(id: string, data: TrainingUpdateRequest): Promise<Training> {
    isSaving.value = true
    try {
      const updated = await trainingService.update(id, data)
      training.value = updated
      return updated
    } catch (err) {
      console.error('Failed to update training:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function deleteTraining(id: string): Promise<void> {
    isSaving.value = true
    try {
      await trainingService.delete(id)
      training.value = null
    } catch (err) {
      console.error('Failed to delete training:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function uploadCoverImage(id: string, file: File): Promise<Training> {
    isSaving.value = true
    try {
      const updated = await trainingService.uploadCoverImage(id, file)
      training.value = updated
      return updated
    } catch (err) {
      console.error('Failed to upload cover image:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function deleteCoverImage(id: string): Promise<Training> {
    isSaving.value = true
    try {
      const updated = await trainingService.deleteCoverImage(id)
      training.value = updated
      return updated
    } catch (err) {
      console.error('Failed to delete cover image:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function addVideo(trainingId: string, data: TrainingVideoCreateRequest): Promise<TrainingVideo> {
    isSaving.value = true
    try {
      const video = await trainingService.addVideo(trainingId, data)
      // Reload training to get updated video list
      await loadTraining(trainingId)
      return video
    } catch (err) {
      console.error('Failed to add video:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function updateVideo(
    trainingId: string,
    videoId: string,
    data: TrainingVideoUpdateRequest
  ): Promise<TrainingVideo> {
    isSaving.value = true
    try {
      const video = await trainingService.updateVideo(trainingId, videoId, data)
      // Reload training to get updated video list
      await loadTraining(trainingId)
      return video
    } catch (err) {
      console.error('Failed to update video:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function deleteVideo(trainingId: string, videoId: string): Promise<void> {
    isSaving.value = true
    try {
      await trainingService.deleteVideo(trainingId, videoId)
      // Reload training to get updated video list
      await loadTraining(trainingId)
    } catch (err) {
      console.error('Failed to delete video:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  async function reorderVideos(trainingId: string, videoIds: string[]): Promise<void> {
    isSaving.value = true
    try {
      await trainingService.reorderVideos(trainingId, { videoIds })
      // Reload training to get updated video list
      await loadTraining(trainingId)
    } catch (err) {
      console.error('Failed to reorder videos:', err)
      throw err
    } finally {
      isSaving.value = false
    }
  }

  function resetTraining(): void {
    training.value = null
  }

  return {
    training,
    isLoading,
    isSaving,
    loadTraining,
    createTraining,
    updateTraining,
    deleteTraining,
    uploadCoverImage,
    deleteCoverImage,
    addVideo,
    updateVideo,
    deleteVideo,
    reorderVideos,
    resetTraining,
  }
}
