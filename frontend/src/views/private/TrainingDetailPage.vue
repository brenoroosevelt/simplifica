<template>
  <div class="training-detail-page">
    <PageHeader :title="training?.title || 'Carregando...'" :subtitle="training?.description">
      <template #actions>
        <div class="d-flex gap-2">
          <v-btn
            icon="mdi-arrow-left"
            variant="tonal"
            size="small"
            @click="handleBack"
          />
          <v-btn
            icon="mdi-pencil"
            color="primary"
            variant="tonal"
            size="small"
            @click="handleEdit"
            :disabled="isLoading"
          />
          <v-btn
            icon="mdi-delete"
            color="error"
            variant="tonal"
            size="small"
            @click="openDeleteDialog"
            :disabled="isLoading"
          />
        </div>
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

    <!-- Content -->
    <div v-else-if="training">
      <!-- Cover Image -->
      <v-card v-if="training.coverImageUrl" variant="flat" border class="mb-6">
        <v-img :src="training.coverImageUrl" height="400" cover />
      </v-card>

      <!-- Main Information -->
      <v-card variant="flat" border class="mb-6">
        <v-card-text class="pa-6">
          <div class="text-caption text-medium-emphasis mb-3">
            <div>
              <strong>Instituição:</strong> {{ training.institutionName }}
              ({{ training.institutionAcronym }})
            </div>
            <div class="mt-1">
              <strong>Criado em:</strong>
              {{ formatDate(training.createdAt) }}
            </div>
            <div class="mt-1">
              <strong>Atualizado em:</strong>
              {{ formatDate(training.updatedAt) }}
            </div>
          </div>

          <div class="d-flex justify-end align-center gap-2 mb-4">
            <v-chip :color="training.active ? 'success' : 'error'" size="small">
              {{ training.active ? 'Ativa' : 'Inativa' }}
            </v-chip>
            <v-chip prepend-icon="mdi-play-circle" color="primary" size="small">
              {{ videoCount }} vídeo{{ videoCount !== 1 ? 's' : '' }}
            </v-chip>
            <v-chip v-if="totalDuration > 0" prepend-icon="mdi-clock-outline" size="small">
              {{ totalDuration }} minutos
            </v-chip>
          </div>

          <v-divider v-if="training.content" class="my-4" />

          <div v-if="training.content">
            <p class="text-body-1">{{ training.content }}</p>
          </div>
        </v-card-text>
      </v-card>

      <!-- Videos List -->
      <v-alert v-if="!hasVideos" type="info" variant="tonal">
        Nenhum vídeo adicionado a esta capacitação.
      </v-alert>

      <div v-else>
        <div
          v-for="(video, index) in training.videos"
          :key="video.id"
          class="mb-6"
        >
          <v-card variant="flat" border>
            <v-card-text class="pa-6">
              <div class="d-flex align-center justify-space-between gap-2 mb-3">
                <div class="d-flex align-center gap-2">
                  <v-chip size="small" color="primary">
                    #{{ index + 1 }}
                  </v-chip>
                  <h3 class="text-h6">{{ video.title }}</h3>
                </div>
                <v-chip v-if="video.durationMinutes" size="small" variant="tonal">
                  {{ video.durationMinutes }} min
                </v-chip>
              </div>

              <p v-if="video.content" class="text-body-2 text-medium-emphasis mb-3">
                {{ video.content }}
              </p>

              <you-tube-player v-if="video.videoId" :video-id="video.videoId" :title="video.title" />
            </v-card-text>
          </v-card>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          Confirmar Exclusão
        </v-card-title>
        <v-card-text class="pa-6">
          <p class="text-body-1 mb-4">
            Tem certeza que deseja excluir a capacitação
            <strong>{{ training?.title }}</strong>?
          </p>
          <p class="text-body-2 text-medium-emphasis">
            Esta ação irá desativar a capacitação. Os dados serão preservados no sistema.
          </p>
        </v-card-text>
        <v-card-actions class="pa-4 d-flex" style="gap: 12px">
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancelar</v-btn>
          <v-btn color="error" variant="flat" :loading="isSaving" @click="handleDelete">
            Excluir
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import YouTubePlayer from '@/components/training/YouTubePlayer.vue'
import { useTrainingDetail } from '@/composables/useTrainingDetail'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'

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
const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

const formatDate = (date: string): string => {
  return format(new Date(date), "dd 'de' MMMM 'de' yyyy 'às' HH:mm", { locale: ptBR })
}

const showSnackbar = (message: string, color = 'success') => {
  snackbar.value = { show: true, message, color }
}

const handleBack = () => {
  navigateToList()
}

const handleEdit = () => {
  navigateToEdit()
}

const openDeleteDialog = () => {
  deleteDialog.value = true
}

const handleDelete = async () => {
  try {
    await deleteTraining()
    showSnackbar('Capacitação excluída com sucesso!')
    deleteDialog.value = false
    setTimeout(() => {
      navigateToList()
    }, 1000)
  } catch (err: any) {
    showSnackbar(err.response?.data?.message || 'Erro ao excluir capacitação', 'error')
  }
}

onMounted(() => {
  loadTraining()
})
</script>

<style scoped>
.training-detail-page {
  /* Custom styles */
}

.gap-2 {
  gap: 8px;
}

.border-b {
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}
</style>
