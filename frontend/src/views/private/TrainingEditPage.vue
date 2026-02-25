<template>
  <div class="training-edit-page">
    <PageHeader :title="pageTitle">
      <template #actions>
        <v-btn
          icon="mdi-arrow-left"
          variant="tonal"
          size="small"
          @click="handleCancel"
        />
      </template>
    </PageHeader>

    <!-- Loading State -->
    <v-card v-if="isLoading" variant="flat" border>
      <v-card-text class="py-12 text-center">
        <v-progress-circular indeterminate color="primary" size="48" />
        <p class="text-body-1 mt-4">Carregando capacitação...</p>
      </v-card-text>
    </v-card>

    <!-- Error State -->
    <v-alert v-else-if="error" type="error" variant="tonal" class="mb-6">
      {{ error }}
    </v-alert>

    <!-- Edit/Create Form -->
    <div v-else>
      <!-- Main Data Form (without card wrapper) -->
      <training-form
        ref="trainingFormRef"
        :training="training || undefined"
        :loading="isSaving"
        @submit="handleFormSubmit"
        @cancel="handleCancel"
      />

      <!-- Divider -->
      <v-divider class="my-6" />

      <!-- Video Manager (without card wrapper) -->
      <training-video-manager
        :videos="localVideos"
        :training-id="isEditMode ? trainingId : undefined"
        @add="handleVideoAdd"
        @update="handleVideoUpdate"
        @delete="handleVideoDelete"
        @reorder="handleVideoReorder"
      />

      <!-- Save Button -->
      <div class="d-flex justify-end mt-6" style="gap: 12px">
        <v-btn
          variant="outlined"
          prepend-icon="mdi-close"
          @click="handleCancel"
        >
          Cancelar
        </v-btn>
        <v-btn
          color="primary"
          variant="flat"
          prepend-icon="mdi-content-save"
          @click="handleSaveClick"
          :loading="isSaving"
          :disabled="isLoading || (isEditMode && !trainingFormRef?.hasChanges)"
        >
          Salvar Alterações
        </v-btn>
      </div>
    </div>

    <!-- Snackbar -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="3000"
      location="top right"
    >
      {{ snackbar.message }}
      <template #actions>
        <v-btn variant="text" @click="snackbar.show = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import TrainingForm from '@/components/training/TrainingForm.vue'
import TrainingVideoManager from '@/components/training/TrainingVideoManager.vue'
import { useTrainingDetail } from '@/composables/useTrainingDetail'
import { useTrainingForm } from '@/composables/useTrainingForm'
import type {
  TrainingUpdateRequest,
  TrainingCreateRequest,
  TrainingVideoCreateRequest,
  TrainingVideoUpdateRequest,
  TrainingVideo,
} from '@/types/training.types'

const route = useRoute()
const router = useRouter()

// Detect mode based on route name instead of params
const isCreationMode = computed(() => route.name === 'training-new')
const isEditMode = computed(() => !isCreationMode.value)

// Page title based on mode
const pageTitle = computed(() =>
  isCreationMode.value ? 'Nova Capacitação' : 'Editar Capacitação'
)

// Local videos state for creation mode (before training is created)
const localVideos = ref<TrainingVideo[]>([])

// Composables must be called at top level of setup (not inside computed/watch)
const editComposable = useTrainingDetail(route.params.id as string || '')
const createComposable = useTrainingForm()

// Extract methods and state based on mode
const training = computed(() => isEditMode.value ? editComposable.training.value : null)
const isLoading = ref(false)
const isSaving = ref(false)
const error = ref<string | null>(null)

const trainingFormRef = ref()
const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

const showSnackbar = (message: string, color = 'success') => {
  snackbar.value = { show: true, message, color }
}

const handleCancel = () => {
  if (isEditMode.value) {
    editComposable.navigateToDetail()
  } else {
    router.push({ name: 'trainings' })
  }
}

const handleSaveClick = () => {
  // Trigger form submit programmatically
  trainingFormRef.value?.handleSubmit()
}

const handleFormSubmit = async (data: TrainingUpdateRequest | TrainingCreateRequest) => {
  try {
    if (isEditMode.value) {
      // Edit mode
      isSaving.value = true

      const { image, removeImage, ...trainingData } = data as TrainingUpdateRequest & { image?: File; removeImage?: boolean }

      await editComposable.updateTraining(trainingData)

      if (removeImage) {
        await editComposable.deleteCoverImage()
      } else if (image) {
        await editComposable.uploadCoverImage(image)
      }

      showSnackbar('Capacitação atualizada com sucesso!')
      setTimeout(() => {
        editComposable.navigateToDetail()
      }, 1000)
    } else if (isCreationMode.value) {
      // Creation mode
      isSaving.value = true

      // Validate at least one video
      if (localVideos.value.length === 0) {
        showSnackbar('É necessário adicionar pelo menos um vídeo', 'error')
        isSaving.value = false
        return
      }

      // Prepare videos for creation
      const videosData: TrainingVideoCreateRequest[] = localVideos.value.map((video, index) => ({
        title: video.title,
        youtubeUrl: video.youtubeUrl,
        content: video.content?.trim() || undefined,
        durationMinutes: video.durationMinutes || 0,
        orderIndex: index,
      }))

      // Create training with videos
      const createData: TrainingCreateRequest = {
        ...data,
        videos: videosData,
      }

      const created = await createComposable.createTraining(createData)
      showSnackbar('Capacitação criada com sucesso!')

      // Navigate to detail page of created training
      setTimeout(() => {
        router.push({ name: 'training-detail', params: { id: created.id } })
      }, 1000)
    }
  } catch (err: any) {
    const message = err.response?.data?.message ||
      (isCreationMode.value ? 'Erro ao criar capacitação' : 'Erro ao atualizar capacitação')
    showSnackbar(message, 'error')
  } finally {
    isSaving.value = false
  }
}

const handleVideoAdd = async (videoData: TrainingVideoCreateRequest) => {
  if (isEditMode.value) {
    // Edit mode: add video to existing training
    try {
      isSaving.value = true
      await editComposable.addVideo(videoData)
      showSnackbar('Vídeo adicionado com sucesso!')
    } catch (err: any) {
      showSnackbar(err.response?.data?.message || 'Erro ao adicionar vídeo', 'error')
    } finally {
      isSaving.value = false
    }
  } else {
    // Creation mode: add video to local state
    const newVideo: TrainingVideo = {
      id: `temp-${Date.now()}`, // Temporary ID for local state
      title: videoData.title,
      youtubeUrl: videoData.youtubeUrl,
      content: videoData.content || '',
      durationMinutes: videoData.durationMinutes || 0,
      orderIndex: localVideos.value.length,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    localVideos.value.push(newVideo)
    showSnackbar('Vídeo adicionado localmente. Salve para confirmar.')
  }
}

const handleVideoUpdate = async (videoId: string, videoData: TrainingVideoUpdateRequest) => {
  if (isEditMode.value) {
    // Edit mode: update video on server
    try {
      isSaving.value = true
      await editComposable.updateVideo(videoId, videoData)
      showSnackbar('Vídeo atualizado com sucesso!')
    } catch (err: any) {
      showSnackbar(err.response?.data?.message || 'Erro ao atualizar vídeo', 'error')
    } finally {
      isSaving.value = false
    }
  } else {
    // Creation mode: update video in local state
    const index = localVideos.value.findIndex(v => v.id === videoId)
    if (index !== -1) {
      const currentVideo = localVideos.value[index]
      if (currentVideo) {
        localVideos.value[index] = {
          ...currentVideo,
          title: videoData.title,
          youtubeUrl: videoData.youtubeUrl,
          content: videoData.content || '',
          durationMinutes: videoData.durationMinutes || 0,
          updatedAt: new Date().toISOString(),
        }
        showSnackbar('Vídeo atualizado localmente. Salve para confirmar.')
      }
    }
  }
}

const handleVideoDelete = async (videoId: string) => {
  if (isEditMode.value) {
    // Edit mode: delete video on server
    try {
      isSaving.value = true
      await editComposable.deleteVideo(videoId)
      showSnackbar('Vídeo removido com sucesso!')
    } catch (err: any) {
      showSnackbar(err.response?.data?.message || 'Erro ao remover vídeo', 'error')
    } finally {
      isSaving.value = false
    }
  } else {
    // Creation mode: remove video from local state
    const index = localVideos.value.findIndex(v => v.id === videoId)
    if (index !== -1) {
      localVideos.value.splice(index, 1)
      showSnackbar('Vídeo removido localmente.')
    }
  }
}

const handleVideoReorder = async (videoIds: string[]) => {
  if (isEditMode.value) {
    // Edit mode: reorder videos on server
    try {
      isSaving.value = true
      await editComposable.reorderVideos(videoIds)
      showSnackbar('Vídeos reordenados com sucesso!')
    } catch (err: any) {
      showSnackbar(err.response?.data?.message || 'Erro ao reordenar vídeos', 'error')
    } finally {
      isSaving.value = false
    }
  } else {
    // Creation mode: reorder videos in local state
    const reordered = videoIds.map(id => localVideos.value.find(v => v.id === id)!).filter(Boolean)
    localVideos.value = reordered.map((video, index) => ({
      ...video,
      orderIndex: index,
    }))
    showSnackbar('Vídeos reordenados localmente. Salve para confirmar.')
  }
}

onMounted(async () => {
  if (isEditMode.value) {
    // Load existing training
    isLoading.value = true
    try {
      await editComposable.loadTraining()
      // Initialize local videos with training videos
      if (editComposable.training.value?.videos) {
        localVideos.value = [...editComposable.training.value.videos]
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erro ao carregar capacitação'
    } finally {
      isLoading.value = false
    }
  } else {
    // Creation mode: initialize empty local videos
    localVideos.value = []
  }
})
</script>

<style scoped>
.training-edit-page {
  /* Custom styles */
}
</style>
