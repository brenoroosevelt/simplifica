<template>
  <v-form ref="formRef" v-model="isValid" @submit.prevent="handleSubmit">
    <v-row>
      <!-- Image Upload Section -->
      <v-col cols="12" class="text-center">
        <v-avatar
          size="120"
          rounded="lg"
          :color="imagePreview ? 'transparent' : 'grey-lighten-3'"
          class="mb-4"
        >
          <v-img
            v-if="imagePreview"
            :src="imagePreview"
            alt="Imagem Preview"
            cover
          />
          <v-icon v-else size="64" color="grey-lighten-1">
            mdi-chart-timeline-variant
          </v-icon>
        </v-avatar>

        <input
          ref="fileInputRef"
          type="file"
          accept="image/png,image/jpeg,image/jpg"
          hidden
          @change="handleImageSelect"
        >

        <div class="d-flex justify-center" style="gap: 8px;">
          <v-btn
            color="primary"
            variant="tonal"
            prepend-icon="mdi-upload"
            @click="triggerFileInput"
          >
            {{ imagePreview ? 'Alterar Imagem' : 'Upload Imagem' }}
          </v-btn>

          <v-btn
            v-if="imagePreview"
            color="error"
            variant="text"
            icon="mdi-delete"
            @click="handleImageRemove"
          />
        </div>

        <p class="text-caption text-medium-emphasis mt-2">
          Tamanho máximo: 5MB. Formatos: PNG, JPG, JPEG
        </p>
      </v-col>

      <!-- Name Field -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.name"
          label="Nome da Cadeia de Valor *"
          placeholder="Ex: Cadeia de Valor Agropecuária"
          :rules="[rules.required, rules.maxLength(255)]"
          :counter="255"
          prepend-inner-icon="mdi-chart-timeline-variant"
          required
        />
      </v-col>

      <!-- Description Field -->
      <v-col cols="12">
        <v-textarea
          v-model="formData.description"
          label="Descrição (opcional)"
          placeholder="Descreva a cadeia de valor..."
          :rules="[rules.maxLength(5000)]"
          :counter="5000"
          rows="4"
          prepend-inner-icon="mdi-text"
        />
      </v-col>

      <!-- Institution Field (Read-only) -->
      <v-col cols="12" md="8">
        <v-text-field
          :model-value="institutionName"
          label="Instituição"
          prepend-inner-icon="mdi-office-building"
          readonly
          disabled
          hint="A instituição é automaticamente definida com base na sua seleção atual"
          persistent-hint
        />
      </v-col>

      <!-- Active Field -->
      <v-col cols="12" md="4">
        <v-select
          v-model="formData.active"
          label="Status *"
          :items="statusOptions"
          :rules="[rules.required]"
          prepend-inner-icon="mdi-check-circle"
          required
        />
      </v-col>

      <!-- Form Actions -->
      <v-col cols="12" class="d-flex justify-end mt-2" style="gap: 12px;">
        <v-btn
          variant="text"
          :disabled="loading"
          @click="$emit('cancel')"
        >
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
import { useInstitutionStore } from '@/stores/institution.store'
import type { ValueChain, ValueChainCreateRequest, ValueChainUpdateRequest } from '@/types/valueChain.types'

interface Props {
  valueChain?: ValueChain
  loading?: boolean
}

interface Emits {
  (_event: 'submit', _data: ValueChainCreateRequest | ValueChainUpdateRequest): void
  (_event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  valueChain: undefined,
  loading: false,
})

const emit = defineEmits<Emits>()

// Store
const institutionStore = useInstitutionStore()

// Refs
const formRef = ref<HTMLFormElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const isValid = ref(false)
const imagePreview = ref<string | null>(null)
const imageFile = ref<File | null>(null)

// Computed
const isEditMode = computed(() => !!props.valueChain)
const institutionName = computed(() => institutionStore.activeInstitution?.name || '')

// Form Data
const formData = reactive<{
  name: string
  description: string
  active: boolean
}>({
  name: '',
  description: '',
  active: true,
})

const initialData = ref<string>('')

const hasChanges = computed(() => {
  if (!isEditMode.value) {
    return true // For create mode, always allow submission if valid
  }
  const currentData = JSON.stringify(formData)
  return currentData !== initialData.value || !!imageFile.value
})

// Options for selects
const statusOptions = [
  { title: 'Ativa', value: true },
  { title: 'Inativa', value: false },
]

// Validation Rules
const rules = {
  required: (v: unknown) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => {
    return !v || v.length <= max || `Máximo de ${max} caracteres`
  },
}

// Image handling
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  // Validate file size (5MB max)
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    alert('Arquivo muito grande. Tamanho máximo: 5MB')
    return
  }

  // Validate file type
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png']
  if (!validTypes.includes(file.type)) {
    alert('Formato inválido. Use PNG, JPG ou JPEG')
    return
  }

  imageFile.value = file

  // Generate preview
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const handleImageRemove = () => {
  imageFile.value = null
  imagePreview.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

// Form submission
const handleSubmit = () => {
  if (!isValid.value) return

  if (isEditMode.value) {
    const updateData: ValueChainUpdateRequest = {}

    if (formData.name !== props.valueChain!.name) {
      updateData.name = formData.name
    }
    if (formData.description !== (props.valueChain!.description || '')) {
      updateData.description = formData.description || undefined
    }
    if (formData.active !== props.valueChain!.active) {
      updateData.active = formData.active
    }
    if (imageFile.value) {
      updateData.image = imageFile.value
    }

    emit('submit', updateData)
  } else {
    const createData: ValueChainCreateRequest = {
      name: formData.name,
      description: formData.description || undefined,
      active: formData.active,
      image: imageFile.value || undefined,
    }

    emit('submit', createData)
  }
}

// Reset form
const resetForm = () => {
  if (props.valueChain) {
    formData.name = props.valueChain.name
    formData.description = props.valueChain.description || ''
    formData.active = props.valueChain.active

    if (props.valueChain.imageUrl) {
      imagePreview.value = props.valueChain.imageUrl
    }
  } else {
    formData.name = ''
    formData.description = ''
    formData.active = true
    imagePreview.value = null
  }

  imageFile.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

// Initialize form data
onMounted(() => {
  if (props.valueChain) {
    formData.name = props.valueChain.name
    formData.description = props.valueChain.description || ''
    formData.active = props.valueChain.active

    if (props.valueChain.imageUrl) {
      imagePreview.value = props.valueChain.imageUrl
    }

    initialData.value = JSON.stringify(formData)
  }
})

// Watch for prop changes
watch(
  () => props.valueChain,
  (newValueChain) => {
    if (newValueChain) {
      formData.name = newValueChain.name
      formData.description = newValueChain.description || ''
      formData.active = newValueChain.active

      if (newValueChain.imageUrl) {
        imagePreview.value = newValueChain.imageUrl
      } else {
        imagePreview.value = null
      }

      imageFile.value = null
      initialData.value = JSON.stringify(formData)
    } else {
      resetForm()
    }
  }
)
</script>
