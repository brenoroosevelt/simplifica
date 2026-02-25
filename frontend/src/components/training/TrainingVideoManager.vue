<template>
  <div class="video-manager">
    <v-card variant="flat" border>
      <v-card-title class="d-flex align-center justify-space-between pa-5">
        <span>Vídeos da Capacitação</span>
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          variant="flat"
          @click="openAddDialog"
        >
          Adicionar Vídeo
        </v-btn>
      </v-card-title>

      <v-divider />

      <v-card-text class="pa-6">
        <v-alert
          v-if="videos.length === 0"
          type="info"
          variant="tonal"
          class="mb-4"
        >
          Nenhum vídeo adicionado. Clique em "Adicionar Vídeo" para começar.
        </v-alert>

        <draggable
          v-model="localVideos"
          item-key="id"
          handle=".drag-handle"
          @end="handleReorder"
        >
          <template #item="{ element: video, index }">
            <v-card
              :key="video.id"
              class="mb-3"
              variant="outlined"
            >
              <v-card-text class="d-flex align-center gap-3">
                <v-icon class="drag-handle" style="cursor: move">
                  mdi-drag
                </v-icon>

                <div class="flex-grow-1">
                  <div class="d-flex align-center gap-2 mb-1">
                    <v-chip size="small" color="primary">
                      #{{ index + 1 }}
                    </v-chip>
                    <strong>{{ video.title }}</strong>
                  </div>
                  <div class="text-caption text-medium-emphasis">
                    {{ video.youtubeUrl }}
                  </div>
                  <div v-if="video.content" class="text-caption text-medium-emphasis mt-1">
                    {{ video.content.substring(0, 100) }}{{ video.content.length > 100 ? '...' : '' }}
                  </div>
                  <div v-if="video.durationMinutes" class="text-caption">
                    Duração: {{ video.durationMinutes }} min
                  </div>
                </div>

                <div class="d-flex gap-2">
                  <v-btn
                    icon="mdi-pencil"
                    size="small"
                    variant="text"
                    @click="openEditDialog(video)"
                  />
                  <v-btn
                    icon="mdi-delete"
                    size="small"
                    variant="text"
                    color="error"
                    @click="confirmDelete(video)"
                    :disabled="videos.length === 1"
                  />
                </div>
              </v-card-text>
            </v-card>
          </template>
        </draggable>
      </v-card-text>
    </v-card>

    <!-- Add/Edit Video Dialog -->
    <v-dialog v-model="dialog" max-width="600px" persistent scrollable>
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          <div class="d-flex align-center justify-space-between w-100">
            <span>{{ editingVideo ? 'Editar Vídeo' : 'Adicionar Vídeo' }}</span>
            <v-btn
              icon="mdi-close"
              variant="text"
              @click="closeDialog"
            />
          </div>
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <training-video-form
            ref="videoFormRef"
            :video="editingVideo || undefined"
            :loading="submitting"
            @submit="handleVideoSubmit"
            @cancel="closeDialog"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">Confirmar Exclusão</v-card-title>
        <v-card-text class="pa-6">
          Tem certeza que deseja remover este vídeo?
          <div class="mt-2">
            <strong>{{ videoToDelete?.title }}</strong>
          </div>
        </v-card-text>
        <v-card-actions class="pa-4 d-flex" style="gap: 12px;">
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">
            Cancelar
          </v-btn>
          <v-btn
            color="error"
            variant="flat"
            :loading="deleting"
            @click="handleDelete"
          >
            Excluir
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import draggable from 'vuedraggable'
import TrainingVideoForm from './TrainingVideoForm.vue'
import type {
  TrainingVideo,
  TrainingVideoCreateRequest,
  TrainingVideoUpdateRequest,
} from '@/types/training.types'

interface Props {
  videos: TrainingVideo[]
  trainingId?: string
}

interface Emits {
  (e: 'add', video: TrainingVideoCreateRequest): void
  (e: 'update', videoId: string, video: TrainingVideoUpdateRequest): void
  (e: 'delete', videoId: string): void
  (e: 'reorder', videoIds: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const localVideos = ref<TrainingVideo[]>([...props.videos])
const dialog = ref(false)
const deleteDialog = ref(false)
const editingVideo = ref<TrainingVideo | null>(null)
const videoToDelete = ref<TrainingVideo | null>(null)
const submitting = ref(false)
const deleting = ref(false)
const videoFormRef = ref()

// Sync local videos with props
const syncLocalVideos = () => {
  localVideos.value = [...props.videos].sort((a, b) => a.orderIndex - b.orderIndex)
}

// Watch for changes in props.videos
watch(() => props.videos, syncLocalVideos, { immediate: true, deep: true })

const openAddDialog = () => {
  editingVideo.value = null
  dialog.value = true
}

const openEditDialog = (video: TrainingVideo) => {
  editingVideo.value = { ...video }
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
  editingVideo.value = null
  videoFormRef.value?.reset()
}

const handleVideoSubmit = async (data: TrainingVideoCreateRequest | TrainingVideoUpdateRequest) => {
  submitting.value = true
  try {
    if (editingVideo.value) {
      // Edit mode
      emit('update', editingVideo.value.id, data as TrainingVideoUpdateRequest)
    } else {
      // Add mode
      emit('add', data as TrainingVideoCreateRequest)
    }
    closeDialog()
  } finally {
    submitting.value = false
  }
}

const confirmDelete = (video: TrainingVideo) => {
  videoToDelete.value = video
  deleteDialog.value = true
}

const handleDelete = async () => {
  if (!videoToDelete.value) return

  deleting.value = true
  try {
    emit('delete', videoToDelete.value.id)
    deleteDialog.value = false
    videoToDelete.value = null
  } finally {
    deleting.value = false
  }
}

const handleReorder = () => {
  const videoIds = localVideos.value.map(v => v.id)
  emit('reorder', videoIds)
}
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}

.gap-3 {
  gap: 12px;
}

.drag-handle {
  color: rgba(var(--v-theme-on-surface), 0.38);
}

.drag-handle:hover {
  color: rgba(var(--v-theme-on-surface), 0.6);
}
</style>
