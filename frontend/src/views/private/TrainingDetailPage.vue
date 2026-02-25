<template>
  <div class="training-detail-page">
    <PageHeader title="Capacitações" :subtitle="training?.institutionName">
      <template #actions>
        <div class="d-flex gap-2">
          <v-btn icon="mdi-arrow-left" variant="tonal" size="small" @click="handleBack" />
          <v-btn icon="mdi-pencil" color="primary" variant="tonal" size="small" @click="handleEdit" :disabled="isLoading" />
          <v-btn icon="mdi-delete" color="error" variant="tonal" size="small" @click="openDeleteDialog" :disabled="isLoading" />
        </div>
      </template>
    </PageHeader>

    <!-- Loading -->
    <v-card v-if="isLoading" variant="flat" border>
      <v-card-text class="py-12 text-center">
        <v-progress-circular indeterminate color="primary" size="48" />
        <p class="text-body-1 mt-4">Carregando capacitação...</p>
      </v-card-text>
    </v-card>

    <!-- Error -->
    <v-alert v-else-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <!-- LINK type -->
    <div v-else-if="training && training.trainingType === 'LINK'">
      <!-- Info card: full width -->
      <v-card variant="flat" border class="mb-4">
        <v-card-text class="pa-6">
          <h2 class="text-h5 font-weight-medium mb-2">{{ training.title }}</h2>
          <div class="d-flex align-center gap-2 mb-4">
            <v-chip :color="training.active ? 'success' : 'error'" size="small">
              {{ training.active ? 'Ativa' : 'Inativa' }}
            </v-chip>
            <v-chip prepend-icon="mdi-link" color="secondary" size="small" variant="tonal">Link externo</v-chip>
          </div>
          <p v-if="training.description" class="text-body-1 mb-3">{{ training.description }}</p>
          <p v-if="training.content" class="text-body-2 text-medium-emphasis mb-4">{{ training.content }}</p>
          <v-btn
            color="primary"
            variant="flat"
            prepend-icon="mdi-open-in-new"
            :href="training.externalLink"
            target="_blank"
            rel="noopener noreferrer"
          >
            Acessar Conteúdo
          </v-btn>
        </v-card-text>
      </v-card>
    </div>

    <!-- VIDEO_SEQUENCE type: playlist layout -->
    <div v-else-if="training">
      <!-- Info card: full width -->
      <v-card variant="flat" border class="mb-4">
        <v-card-text class="pa-6">
          <h2 class="text-h5 font-weight-medium mb-2">{{ training.title }}</h2>
          <div class="d-flex align-center gap-2 flex-wrap mb-3">
            <v-chip :color="training.active ? 'success' : 'error'" size="small">
              {{ training.active ? 'Ativa' : 'Inativa' }}
            </v-chip>
            <v-chip prepend-icon="mdi-play-circle" color="primary" size="small" variant="tonal">
              {{ videoCount }} vídeo{{ videoCount !== 1 ? 's' : '' }}
            </v-chip>
            <v-chip v-if="totalDuration > 0" prepend-icon="mdi-clock-outline" size="small" variant="tonal">
              {{ totalDuration }} min
            </v-chip>
          </div>
          <p v-if="training.description" class="text-body-1 mb-2">{{ training.description }}</p>
          <p v-if="training.content" class="text-body-2 text-medium-emphasis">{{ training.content }}</p>
        </v-card-text>
      </v-card>

      <!-- Player + Playlist -->
      <v-row>
        <!-- Main video player -->
        <v-col cols="12" md="8">
          <v-card variant="flat" border>
            <v-card-text class="pa-0">
              <YouTubePlayer
                v-if="selectedVideo?.videoId"
                :video-id="selectedVideo.videoId"
                :title="selectedVideo.title"
                class="video-player"
              />
              <div v-else class="video-placeholder d-flex align-center justify-center">
                <div class="text-center">
                  <v-icon size="64" color="grey-lighten-2">mdi-play-circle-outline</v-icon>
                  <p class="text-body-2 text-medium-emphasis mt-2">Selecione um vídeo</p>
                </div>
              </div>
            </v-card-text>
            <v-card-text v-if="selectedVideo" class="pa-4">
              <div class="d-flex align-center gap-2 mb-2">
                <v-chip size="small" color="primary">#{{ (selectedVideoIndex ?? 0) + 1 }}</v-chip>
                <h3 class="text-h6">{{ selectedVideo.title }}</h3>
                <v-chip v-if="selectedVideo.durationMinutes" size="small" variant="tonal" class="ml-auto">
                  {{ selectedVideo.durationMinutes }} min
                </v-chip>
              </div>
              <p v-if="selectedVideo.content" class="text-body-2 text-medium-emphasis">
                {{ selectedVideo.content }}
              </p>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Playlist sidebar -->
        <v-col cols="12" md="4">
          <v-card variant="flat" border>
            <v-card-title class="text-subtitle-1 font-weight-medium pa-4 pb-2">
              Playlist
              <span class="text-caption text-medium-emphasis ml-2">({{ training.videos?.length || 0 }} vídeos)</span>
            </v-card-title>
            <v-divider />
            <v-list density="compact" class="playlist-list pa-2">
              <v-list-item
                v-for="(video, index) in sortedVideos"
                :key="video.id"
                :active="selectedVideoIndex === index"
                active-color="primary"
                rounded="lg"
                class="mb-1 playlist-item"
                @click="selectVideo(index)"
              >
                <template #prepend>
                  <div class="playlist-index mr-3">
                    <v-icon v-if="selectedVideoIndex === index" color="primary" size="20">mdi-play-circle</v-icon>
                    <span v-else class="text-caption text-medium-emphasis">{{ index + 1 }}</span>
                  </div>
                </template>
                <v-list-item-title class="text-body-2 font-weight-medium text-wrap">
                  {{ video.title }}
                </v-list-item-title>
                <v-list-item-subtitle v-if="video.durationMinutes" class="text-caption">
                  {{ video.durationMinutes }} min
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </v-card>
        </v-col>
      </v-row>
    </div>

    <!-- Delete Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">Confirmar Exclusão</v-card-title>
        <v-card-text class="pa-6">
          <p class="text-body-1 mb-4">
            Tem certeza que deseja excluir a capacitação <strong>{{ training?.title }}</strong>?
          </p>
          <p class="text-body-2 text-medium-emphasis">Esta ação é irreversível. A capacitação e todos os arquivos associados serão excluídos permanentemente.</p>
        </v-card-text>
        <v-card-actions class="pa-4 d-flex" style="gap: 12px">
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancelar</v-btn>
          <v-btn color="error" variant="flat" :loading="isSaving" @click="handleDelete">Excluir</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.message }}
      <template #actions>
        <v-btn variant="text" @click="snackbar.show = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import YouTubePlayer from '@/components/training/YouTubePlayer.vue'
import { useTrainingDetail } from '@/composables/useTrainingDetail'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import type { TrainingVideo } from '@/types/training.types'

const route = useRoute()
const trainingId = route.params.id as string

const {
  training,
  isLoading,
  isSaving,
  error,
  hasVideos,
  videoCount,
  totalDuration,
  loadTraining,
  deleteTraining,
  navigateToList,
  navigateToEdit,
} = useTrainingDetail(trainingId)

const deleteDialog = ref(false)
const snackbar = ref({ show: false, message: '', color: 'success' })
const selectedVideoIndex = ref<number>(0)

const sortedVideos = computed<TrainingVideo[]>(() => {
  if (!training.value?.videos) return []
  return [...training.value.videos].sort((a, b) => a.orderIndex - b.orderIndex)
})

const selectedVideo = computed<TrainingVideo | null>(() => {
  return sortedVideos.value[selectedVideoIndex.value] || null
})

const selectVideo = (index: number) => {
  selectedVideoIndex.value = index
}

const formatDate = (date: string): string =>
  format(new Date(date), "dd 'de' MMMM 'de' yyyy 'às' HH:mm", { locale: ptBR })

const showSnackbar = (message: string, color = 'success') => {
  snackbar.value = { show: true, message, color }
}

const handleBack = () => navigateToList()
const handleEdit = () => navigateToEdit()
const openDeleteDialog = () => { deleteDialog.value = true }

const handleDelete = async () => {
  try {
    await deleteTraining()
    showSnackbar('Capacitação excluída com sucesso!')
    deleteDialog.value = false
    setTimeout(() => navigateToList(), 1000)
  } catch (err: any) {
    showSnackbar(err.response?.data?.message || 'Erro ao excluir capacitação', 'error')
  }
}

onMounted(() => {
  loadTraining()
})
</script>

<style scoped>
.gap-2 { gap: 8px; }

.video-player {
  width: 100%;
  aspect-ratio: 16 / 9;
}

.video-placeholder {
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #f5f5f5;
}

.playlist-list {
  max-height: 500px;
  overflow-y: auto;
}

.playlist-item {
  cursor: pointer;
}

.playlist-index {
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
