<template>
  <v-form ref="formRef" v-model="isValid" @submit.prevent="handleSubmit">
    <v-row>
      <!-- Title Field -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.title"
          label="Título *"
          placeholder="Ex: Resolução nº 001/2024"
          :rules="[rules.required, rules.maxLength(255)]"
          :counter="255"
          prepend-inner-icon="mdi-file-document-outline"
          required
        />
      </v-col>

      <!-- Description Field -->
      <v-col cols="12">
        <v-textarea
          v-model="formData.description"
          label="Descrição (opcional)"
          placeholder="Descreva o normativo..."
          :rules="[rules.maxLength(5000)]"
          :counter="5000"
          rows="3"
          prepend-inner-icon="mdi-text"
        />
      </v-col>

      <!-- Attachment Type Toggle -->
      <v-col cols="12">
        <div class="text-body-2 font-weight-medium mb-2">Vincular arquivo ou link:</div>
        <v-btn-toggle
          v-model="attachmentType"
          mandatory
          color="primary"
          variant="outlined"
          class="mb-4"
        >
          <v-btn value="file" prepend-icon="mdi-file-upload">
            Arquivo
          </v-btn>
          <v-btn value="link" prepend-icon="mdi-link">
            Link
          </v-btn>
        </v-btn-toggle>

        <!-- File Upload -->
        <div v-if="attachmentType === 'file'">
          <!-- Current file preview -->
          <v-alert
            v-if="currentFileName && !fileRemoved"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-3"
          >
            <div class="d-flex align-center justify-space-between">
              <div class="d-flex align-center" style="gap: 8px;">
                <v-icon>mdi-file-check</v-icon>
                <span class="text-body-2">{{ currentFileName }}</span>
              </div>
              <v-btn
                icon="mdi-close"
                size="x-small"
                variant="text"
                color="error"
                @click="removeFile"
              />
            </div>
          </v-alert>

          <v-file-input
            v-model="formData.file"
            :label="currentFileName && !fileRemoved ? 'Substituir arquivo' : 'Selecionar arquivo'"
            prepend-inner-icon="mdi-file-upload"
            prepend-icon=""
            accept=".pdf,.doc,.docx,.xls,.xlsx,.odt,.ods,.txt"
            :show-size="1000"
            clearable
            hint="PDF, Word, Excel ou texto — máx. 50MB"
            persistent-hint
          />
        </div>

        <!-- External Link -->
        <v-text-field
          v-else
          v-model="formData.externalLink"
          label="URL do documento"
          placeholder="https://..."
          prepend-inner-icon="mdi-link"
          :rules="[rules.urlFormat]"
          hint="Link para o documento externo"
          persistent-hint
        />
      </v-col>

      <!-- Form Actions -->
      <v-col cols="12" class="d-flex justify-end mt-2" style="gap: 12px;">
        <v-btn variant="text" :disabled="loading" @click="$emit('cancel')">
          Cancelar
        </v-btn>
        <v-btn
          type="submit"
          color="primary"
          variant="flat"
          :loading="loading"
          :disabled="!isValid || !hasChanges"
        >
          {{ isEditMode ? 'Salvar' : 'Criar' }}
        </v-btn>
      </v-col>
    </v-row>
  </v-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import type { Normative, NormativeCreateRequest, NormativeUpdateRequest } from '@/types/normative.types'

interface Props {
  normative?: Normative
  loading?: boolean
}

interface Emits {
  (_event: 'submit', _data: NormativeCreateRequest | NormativeUpdateRequest): void
  (_event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  normative: undefined,
  loading: false,
})

const emit = defineEmits<Emits>()

const formRef = ref<HTMLFormElement | null>(null)
const isValid = ref(false)
const fileRemoved = ref(false)
const attachmentType = ref<'file' | 'link'>('file')

const isEditMode = computed(() => !!props.normative)

const currentFileName = computed(() => props.normative?.fileOriginalName || null)

const formData = reactive<{
  title: string
  description: string
  externalLink: string
  file: File | null
}>({
  title: '',
  description: '',
  externalLink: '',
  file: null,
})

const initialData = ref('')

const hasChanges = computed(() => {
  if (!isEditMode.value) {
    if (!formData.title) return false
    if (attachmentType.value === 'file') return !!(formData.file || (currentFileName.value && !fileRemoved.value))
    return !!formData.externalLink
  }
  const current = JSON.stringify({ ...formData, file: formData.file?.name })
  return current !== initialData.value || fileRemoved.value || !!formData.file
})

const rules = {
  required: (v: unknown) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => !v || v.length <= max || `Máximo de ${max} caracteres`,
  urlFormat: (v: string) => {
    if (!v) return true
    try { new URL(v); return true } catch { return 'URL inválida' }
  },
}

const removeFile = () => {
  fileRemoved.value = true
  formData.file = null
}

const handleSubmit = () => {
  if (!isValid.value) return

  if (isEditMode.value) {
    const updateData: NormativeUpdateRequest = {
      title: formData.title,
      description: formData.description || undefined,
      externalLink: attachmentType.value === 'link' ? formData.externalLink || undefined : undefined,
      file: attachmentType.value === 'file' && formData.file ? formData.file : undefined,
      removeFile: fileRemoved.value,
    }
    emit('submit', updateData)
  } else {
    const createData: NormativeCreateRequest = {
      title: formData.title,
      description: formData.description || undefined,
      externalLink: attachmentType.value === 'link' ? formData.externalLink || undefined : undefined,
      file: attachmentType.value === 'file' && formData.file ? formData.file : undefined,
    }
    emit('submit', createData)
  }
}

const resetForm = (normative?: Normative) => {
  if (normative) {
    formData.title = normative.title
    formData.description = normative.description || ''
    formData.externalLink = normative.externalLink || ''
    formData.file = null
    fileRemoved.value = false
    attachmentType.value = normative.externalLink ? 'link' : 'file'
    initialData.value = JSON.stringify({ ...formData, file: null })
  } else {
    formData.title = ''
    formData.description = ''
    formData.externalLink = ''
    formData.file = null
    fileRemoved.value = false
    attachmentType.value = 'file'
  }
}

onMounted(() => resetForm(props.normative))

watch(() => props.normative, (n) => resetForm(n))
</script>
