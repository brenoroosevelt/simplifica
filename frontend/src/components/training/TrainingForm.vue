<template>
  <v-form ref="formRef" v-model="valid" @submit.prevent="handleSubmit">
    <!-- Card 1: Campos comuns -->
    <v-card variant="flat" border class="mb-4">
      <v-card-text class="pa-6">
        <v-row>
          <!-- Cover Image Upload Section -->
          <v-col cols="12" class="text-center">
            <v-avatar
              size="120"
              rounded="lg"
              :color="imagePreview ? 'transparent' : 'grey-lighten-3'"
              class="mb-4"
            >
              <v-img v-if="imagePreview" :src="imagePreview" alt="Imagem de Capa" cover />
              <v-icon v-else size="64" color="grey-lighten-1">mdi-school</v-icon>
            </v-avatar>

            <input
              ref="fileInputRef"
              type="file"
              accept="image/png,image/jpeg,image/jpg,image/webp"
              hidden
              @change="handleImageSelect"
            >

            <div class="d-flex justify-center" style="gap: 8px;">
              <v-btn color="primary" variant="tonal" prepend-icon="mdi-upload" @click="triggerFileInput">
                {{ imagePreview ? 'Alterar Imagem' : 'Upload Imagem' }}
              </v-btn>
              <v-btn v-if="imagePreview" color="error" variant="text" icon="mdi-delete" @click="handleImageRemove" />
            </div>
            <p class="text-caption text-medium-emphasis mt-2">Tamanho máximo: 5MB. Formatos: PNG, JPG, JPEG, WebP</p>
          </v-col>

          <!-- Title -->
          <v-col cols="12">
            <v-text-field
              v-model="formData.title"
              label="Título *"
              :rules="[rules.required, rules.maxLength(255)]"
              variant="outlined"
              density="compact"
              counter="255"
              prepend-inner-icon="mdi-school"
            />
          </v-col>

          <!-- Description -->
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
              prepend-inner-icon="mdi-text"
            />
          </v-col>

          <!-- Active switch -->
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
      </v-card-text>
    </v-card>

    <!-- Card 2: Tipo de conteúdo -->
    <v-card variant="flat" border>
      <v-card-title class="text-subtitle-1 font-weight-medium pa-4 pb-2">Tipo de Conteúdo</v-card-title>
      <v-divider />
      <v-card-text class="pa-6">
        <v-row>
          <!-- Training Type Selector -->
          <v-col cols="12">
            <v-btn-toggle
              v-model="formData.trainingType"
              mandatory
              color="primary"
              variant="outlined"
              class="mb-4"
            >
              <v-btn value="VIDEO_SEQUENCE" prepend-icon="mdi-play-circle">
                Sequência de Vídeos
              </v-btn>
              <v-btn value="LINK" prepend-icon="mdi-link">
                Link Externo
              </v-btn>
            </v-btn-toggle>
          </v-col>

          <!-- External Link (LINK type only) -->
          <v-col v-if="formData.trainingType === 'LINK'" cols="12">
            <v-text-field
              v-model="formData.externalLink"
              label="Link Externo *"
              placeholder="https://..."
              :rules="[rules.required, rules.urlFormat]"
              variant="outlined"
              density="compact"
              prepend-inner-icon="mdi-link"
              hint="URL do conteúdo externo (site, documento, vídeo, etc.)"
              persistent-hint
            />
          </v-col>

          <!-- Content (VIDEO_SEQUENCE type only) -->
          <v-col v-if="formData.trainingType === 'VIDEO_SEQUENCE'" cols="12">
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
              prepend-inner-icon="mdi-text-box"
            />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-form>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { Training, TrainingCreateRequest, TrainingUpdateRequest, TrainingType } from '@/types/training.types'

interface Props {
  training?: Training
  loading?: boolean
}

interface Emits {
  (e: 'submit', data: TrainingCreateRequest | TrainingUpdateRequest): void
  (e: 'cancel'): void
  (e: 'update:trainingType', type: TrainingType): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref()
const fileInputRef = ref<HTMLInputElement | null>(null)
const valid = ref(false)
const imagePreview = ref<string | null>(null)
const imageFile = ref<File | null>(null)
const imageRemoved = ref(false)

const isEditMode = computed(() => !!props.training)

const hasChanges = computed(() => {
  if (!isEditMode.value) return true
  if (imageFile.value || imageRemoved.value) return true
  const t = props.training!
  return (
    formData.value.title !== t.title ||
    (formData.value.description || '') !== (t.description || '') ||
    (formData.value.content || '') !== (t.content || '') ||
    (formData.value.externalLink || '') !== (t.externalLink || '') ||
    formData.value.trainingType !== t.trainingType ||
    formData.value.active !== t.active
  )
})

const formData = ref<{
  title: string
  description: string
  content: string
  externalLink: string
  trainingType: TrainingType
  active: boolean
}>({
  title: '',
  description: '',
  content: '',
  externalLink: '',
  trainingType: 'VIDEO_SEQUENCE',
  active: true,
})

const rules = {
  required: (v: any) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => !v || v.length <= max || `Máximo ${max} caracteres`,
  urlFormat: (v: string) => {
    if (!v) return true
    try { new URL(v); return true } catch { return 'URL inválida' }
  },
}

const triggerFileInput = () => fileInputRef.value?.click()

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) { alert('Arquivo muito grande. Tamanho máximo: 5MB'); return }

  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!validTypes.includes(file.type)) { alert('Formato inválido. Use PNG, JPG, JPEG ou WebP'); return }

  imageFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => { imagePreview.value = e.target?.result as string }
  reader.readAsDataURL(file)
}

const handleImageRemove = () => {
  if (props.training?.coverImageUrl && !imageFile.value) imageRemoved.value = true
  imageFile.value = null
  imagePreview.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const handleSubmit = () => {
  if (!valid.value) return

  if (isEditMode.value) {
    const updateData: any = {
      title: formData.value.title,
      description: formData.value.description?.trim() || undefined,
      content: formData.value.content?.trim() || undefined,
      trainingType: formData.value.trainingType,
      externalLink: formData.value.trainingType === 'LINK' ? formData.value.externalLink : undefined,
      active: formData.value.active,
    }
    if (imageFile.value) updateData.image = imageFile.value
    if (imageRemoved.value) updateData.removeImage = true
    emit('submit', updateData)
  } else {
    const createData: any = {
      title: formData.value.title,
      description: formData.value.description?.trim() || undefined,
      content: formData.value.content?.trim() || undefined,
      trainingType: formData.value.trainingType,
      externalLink: formData.value.trainingType === 'LINK' ? formData.value.externalLink : undefined,
      active: formData.value.active,
      image: imageFile.value || undefined,
    }
    emit('submit', createData)
  }
}

const reset = () => {
  formData.value = { title: '', description: '', content: '', externalLink: '', trainingType: 'VIDEO_SEQUENCE', active: true }
  imageFile.value = null
  imageRemoved.value = false
  imagePreview.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  formRef.value?.resetValidation()
}

onMounted(() => {
  if (props.training) {
    formData.value = {
      title: props.training.title,
      description: props.training.description || '',
      content: props.training.content || '',
      externalLink: props.training.externalLink || '',
      trainingType: props.training.trainingType || 'VIDEO_SEQUENCE',
      active: props.training.active,
    }
    if (props.training.coverImageUrl) imagePreview.value = props.training.coverImageUrl
  }
})

watch(() => props.training, (training) => {
  if (training) {
    formData.value = {
      title: training.title,
      description: training.description || '',
      content: training.content || '',
      externalLink: training.externalLink || '',
      trainingType: training.trainingType || 'VIDEO_SEQUENCE',
      active: training.active,
    }
    imagePreview.value = training.coverImageUrl || null
    imageFile.value = null
    imageRemoved.value = false
  } else {
    reset()
  }
})

watch(() => formData.value.trainingType, (type) => {
  emit('update:trainingType', type)
})

defineExpose({ reset, handleSubmit, hasChanges })
</script>

<style scoped>
.v-card { border-radius: 8px; }
</style>
