<template>
  <v-form ref="formRef" v-model="valid" @submit.prevent="handleSubmit">
    <v-row>
      <v-col cols="12">
        <v-text-field
          v-model="formData.title"
          label="Título do Vídeo *"
          :rules="[rules.required, rules.maxLength(255)]"
          variant="outlined"
          density="compact"
          counter="255"
        />
      </v-col>

      <v-col cols="12">
        <v-text-field
          v-model="formData.youtubeUrl"
          label="URL do YouTube *"
          :rules="[rules.required, rules.youtubeUrl]"
          variant="outlined"
          density="compact"
          placeholder="https://www.youtube.com/watch?v=..."
          hint="Cole a URL completa do vídeo do YouTube"
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
          rows="3"
          counter="10000"
          hint="Descrição ou conteúdo adicional do vídeo"
          persistent-hint
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="formData.durationMinutes"
          label="Duração (minutos)"
          :rules="[rules.minValue(0)]"
          variant="outlined"
          density="compact"
          type="number"
          min="0"
          hint="Opcional: duração aproximada em minutos"
          persistent-hint
        />
      </v-col>

      <v-col v-if="!isEditMode" cols="12" md="6">
        <v-text-field
          v-model.number="formData.orderIndex"
          label="Posição na Playlist *"
          :rules="[rules.required, rules.minValue(0)]"
          variant="outlined"
          density="compact"
          type="number"
          min="0"
          hint="Ordem de exibição (0, 1, 2...)"
          persistent-hint
        />
      </v-col>
    </v-row>

    <v-row class="mt-2">
      <v-col cols="12" class="d-flex gap-2">
        <v-btn
          type="submit"
          color="primary"
          :loading="loading"
          :disabled="!valid"
        >
          {{ isEditMode ? 'Atualizar' : 'Adicionar' }}
        </v-btn>
        <v-btn
          variant="outlined"
          @click="handleCancel"
        >
          Cancelar
        </v-btn>
      </v-col>
    </v-row>
  </v-form>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TrainingVideo, TrainingVideoCreateRequest, TrainingVideoUpdateRequest } from '@/types/training.types'

interface Props {
  video?: TrainingVideo
  loading?: boolean
}

interface Emits {
  (e: 'submit', data: TrainingVideoCreateRequest | TrainingVideoUpdateRequest): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref()
const valid = ref(false)

const isEditMode = computed(() => !!props.video)

const formData = ref<TrainingVideoCreateRequest>({
  title: '',
  youtubeUrl: '',
  content: '',
  durationMinutes: undefined,
  orderIndex: 0,
})

// Validation rules
const rules = {
  required: (v: any) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string | undefined) => !v || v.length <= max || `Máximo ${max} caracteres`,
  minValue: (min: number) => (v: number) => v === undefined || v >= min || `Valor mínimo: ${min}`,
  youtubeUrl: (v: string) => {
    if (!v) return true
    const youtubePattern = /^(https?:\/\/)?(www\.)?(youtube\.com\/(watch\?v=|embed\/)|youtu\.be\/)([a-zA-Z0-9_-]{11})/
    return youtubePattern.test(v) || 'URL do YouTube inválida'
  },
}

// Watch for video prop changes (edit mode)
watch(
  () => props.video,
  (video) => {
    if (video) {
      formData.value = {
        title: video.title,
        youtubeUrl: video.youtubeUrl,
        content: video.content,
        durationMinutes: video.durationMinutes,
        orderIndex: video.orderIndex,
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
    youtubeUrl: formData.value.youtubeUrl,
    content: formData.value.content?.trim() || undefined,
    durationMinutes: formData.value.durationMinutes || undefined,
    orderIndex: formData.value.orderIndex,
  }

  if (isEditMode.value) {
    // Edit mode: don't send orderIndex
    const { orderIndex, ...updateData } = cleanData
    emit('submit', updateData)
  } else {
    // Create mode: send all fields
    emit('submit', cleanData)
  }
}

const handleCancel = () => {
  emit('cancel')
}

// Reset form
const reset = () => {
  formData.value = {
    title: '',
    youtubeUrl: '',
    content: '',
    durationMinutes: undefined,
    orderIndex: 0,
  }
  formRef.value?.resetValidation()
}

defineExpose({ reset })
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}
</style>
