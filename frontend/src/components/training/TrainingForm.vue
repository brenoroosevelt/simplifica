<template>
  <v-card variant="flat" border>
    <v-card-text class="pa-6">
      <v-form ref="formRef" v-model="valid" @submit.prevent="handleSubmit">
        <v-row>
          <v-col cols="12">
            <v-text-field
              v-model="formData.title"
              label="Título *"
              :rules="[rules.required, rules.maxLength(255)]"
              variant="outlined"
              density="compact"
              counter="255"
            />
          </v-col>

          <v-col cols="12">
            <v-textarea
              v-model="formData.description"
              label="Descrição"
              :rules="[rules.maxLength(5000)]"
              variant="outlined"
              density="compact"
              rows="4"
              counter="5000"
              hint="Descreva o conteúdo e objetivos da capacitação"
              persistent-hint
            />
          </v-col>

          <v-col cols="12">
            <v-textarea
              v-model="formData.content"
              label="Conteúdo (opcional)"
              :rules="[rules.maxLength(10000)]"
              variant="outlined"
              density="compact"
              rows="4"
              counter="10000"
              hint="Conteúdo adicional da capacitação"
              persistent-hint
            />
          </v-col>

          <!-- Cover Image Upload (only in edit mode) -->
          <v-col v-if="isEditMode && training" cols="12">
            <v-card variant="outlined">
              <v-card-subtitle>Imagem de Capa</v-card-subtitle>
              <v-card-text>
                <div v-if="training.coverImageUrl" class="mb-3">
                  <v-img
                    :src="training.coverImageUrl"
                    max-height="200"
                    contain
                    class="mb-2"
                  />
                  <v-btn
                    color="error"
                    variant="outlined"
                    size="small"
                    prepend-icon="mdi-delete"
                    :loading="deletingCover"
                    @click="handleDeleteCover"
                  >
                    Remover Capa
                  </v-btn>
                </div>
                <v-file-input
                  v-model="coverImageFile"
                  label="Upload de Nova Capa"
                  accept="image/*"
                  prepend-icon="mdi-camera"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.imageFile]"
                  hint="Formatos aceitos: JPG, PNG, GIF, WebP (máx. 5MB)"
                  persistent-hint
                  @change="handleCoverUpload"
                />
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" md="6">
            <v-switch
              v-model="formData.active"
              label="Ativa"
              color="primary"
              density="compact"
              hide-details
            />
          </v-col>
        </v-row>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Training, TrainingCreateRequest, TrainingUpdateRequest } from '@/types/training.types'

interface Props {
  training?: Training
  loading?: boolean
}

interface Emits {
  (e: 'submit', data: TrainingCreateRequest | TrainingUpdateRequest): void
  (e: 'cancel'): void
  (e: 'uploadCover', file: File): void
  (e: 'deleteCover'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref()
const valid = ref(false)
const coverImageFile = ref<File[]>([])
const deletingCover = ref(false)

const isEditMode = computed(() => !!props.training)

const formData = ref<TrainingUpdateRequest>({
  title: '',
  description: '',
  content: '',
  active: true,
})

// Validation rules
const rules = {
  required: (v: any) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => !v || v.length <= max || `Máximo ${max} caracteres`,
  imageFile: (files: File[]) => {
    if (!files || files.length === 0) return true
    const file = files[0]
    if (!file) return true
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    const maxSize = 5 * 1024 * 1024 // 5MB

    if (!validTypes.includes(file.type)) {
      return 'Formato de imagem inválido'
    }
    if (file.size > maxSize) {
      return 'Tamanho máximo: 5MB'
    }
    return true
  },
}

// Watch for training prop changes (edit mode)
watch(
  () => props.training,
  (training) => {
    if (training) {
      console.log('=== TrainingForm - Training Loaded ===')
      console.log('Training:', training.title)
      console.log('Has cover?', !!training.coverImageUrl)
      console.log('Cover URL:', training.coverImageUrl)
      console.log('======================================')

      formData.value = {
        title: training.title,
        description: training.description,
        content: training.content,
        active: training.active,
      }
    }
  },
  { immediate: true }
)

const handleSubmit = () => {
  if (!valid.value) return

  // Clean up empty strings to undefined
  const cleanData = {
    title: formData.value.title,
    description: formData.value.description?.trim() || undefined,
    content: formData.value.content?.trim() || undefined,
    active: formData.value.active,
  }

  emit('submit', cleanData)
}

const handleCoverUpload = () => {
  if (coverImageFile.value && coverImageFile.value.length > 0) {
    const file = coverImageFile.value[0]
    if (file) {
      emit('uploadCover', file)
      coverImageFile.value = []
    }
  }
}

const handleDeleteCover = () => {
  deletingCover.value = true
  emit('deleteCover')
  // Reset flag after operation
  setTimeout(() => {
    deletingCover.value = false
  }, 1000)
}

// Reset form
const reset = () => {
  formData.value = {
    title: '',
    description: '',
    content: '',
    active: true,
  }
  coverImageFile.value = []
  formRef.value?.resetValidation()
}

defineExpose({ reset, handleSubmit })
</script>

<style scoped>
.v-card {
  border-radius: 8px;
}
</style>
