<template>
  <v-form ref="formRef" v-model="isValid" @submit.prevent="handleSubmit">
    <v-row>
      <!-- Logo Upload Section -->
      <v-col cols="12" class="text-center">
        <v-avatar
          size="120"
          rounded="lg"
          :color="logoPreview ? 'transparent' : 'grey-lighten-3'"
          class="mb-4"
        >
          <v-img
            v-if="logoPreview"
            :src="logoPreview"
            alt="Logo Preview"
            cover
          />
          <v-icon v-else size="64" color="grey-lighten-1">
            mdi-office-building
          </v-icon>
        </v-avatar>

        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          hidden
          @change="handleFileChange"
        >

        <div class="d-flex justify-center" style="gap: 8px;">
          <v-btn
            color="primary"
            variant="tonal"
            prepend-icon="mdi-upload"
            @click="triggerFileInput"
          >
            {{ logoPreview ? 'Alterar Logo' : 'Upload Logo' }}
          </v-btn>

          <v-btn
            v-if="logoPreview"
            color="error"
            variant="text"
            icon="mdi-delete"
            @click="clearLogo"
          />
        </div>

        <p class="text-caption text-medium-emphasis mt-2">
          Tamanho máximo: 5MB. Formatos: JPG, PNG, WebP
        </p>
      </v-col>

      <!-- Name Field -->
      <v-col cols="12" md="8">
        <v-text-field
          v-model="formData.name"
          label="Nome da Instituição *"
          placeholder="Ex: Universidade Federal de Mato Grosso do Sul"
          :rules="[rules.required, rules.maxLength(255)]"
          :counter="255"
          prepend-inner-icon="mdi-office-building"
          required
        />
      </v-col>

      <!-- Acronym Field -->
      <v-col cols="12" md="4">
        <v-text-field
          v-model="formData.acronym"
          label="Sigla *"
          placeholder="Ex: UFMS"
          :rules="[rules.required, rules.maxLength(50), rules.acronym]"
          :counter="50"
          :disabled="isEditMode"
          :hint="isEditMode ? 'A sigla não pode ser alterada' : ''"
          persistent-hint
          required
        />
      </v-col>

      <!-- Type Field -->
      <v-col cols="12" md="6">
        <v-select
          v-model="formData.type"
          label="Tipo de Instituição *"
          :items="typeOptions"
          :rules="[rules.required]"
          prepend-inner-icon="mdi-tag"
          required
        />
      </v-col>

      <!-- Active Field - Apenas ADMIN pode alterar -->
      <v-col cols="12" md="6">
        <v-switch
          v-model="formData.active"
          :label="formData.active ? 'Ativa' : 'Inativa'"
          :color="formData.active ? 'primary' : undefined"
          :disabled="!isAdmin"
          :hint="!isAdmin ? 'Apenas administradores podem alterar o status' : ''"
          persistent-hint
          hide-details="auto"
          density="compact"
        />
      </v-col>

      <!-- Domain Field - Apenas ADMIN pode alterar -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.domain"
          label="Domínio (opcional)"
          placeholder="Ex: ufms.br"
          :rules="[rules.domain]"
          :disabled="!isAdmin"
          :hint="!isAdmin ? 'Apenas administradores podem alterar o domínio' : 'Domínio de e-mail para vinculação automática futura'"
          persistent-hint
          prepend-inner-icon="mdi-web"
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
import { InstitutionType } from '@/types/institution.types'
import type { Institution, InstitutionCreateRequest, InstitutionUpdateRequest } from '@/types/institution.types'

interface Props {
  institution?: Institution
  loading?: boolean
  isAdmin?: boolean
}

interface Emits {
  (event: 'submit', data: InstitutionCreateRequest | InstitutionUpdateRequest): void
  (event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  institution: undefined,
  loading: false,
  isAdmin: true, // Default true para não quebrar funcionalidade existente
})

const emit = defineEmits<Emits>()

const formRef = ref<HTMLFormElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const isValid = ref(false)
const logoPreview = ref<string | null>(null)
const selectedFile = ref<File | null>(null)
const imageRemoved = ref(false)

const isEditMode = computed(() => !!props.institution)

const formData = reactive<{
  name: string
  acronym: string
  type: InstitutionType | null
  domain: string
  active: boolean
}>({
  name: '',
  acronym: '',
  type: null,
  domain: '',
  active: true,
})

const initialData = ref<string>('')

const hasChanges = computed(() => {
  if (!isEditMode.value) {
    return true // For create mode, always allow submission if valid
  }
  const currentData = JSON.stringify(formData)
  return currentData !== initialData.value || !!selectedFile.value || imageRemoved.value
})

// Options for selects
const typeOptions = [
  { title: 'Federal', value: InstitutionType.FEDERAL },
  { title: 'Estadual', value: InstitutionType.ESTADUAL },
  { title: 'Municipal', value: InstitutionType.MUNICIPAL },
  { title: 'Privada', value: InstitutionType.PRIVADA },
]

// Validation Rules
const rules = {
  required: (v: string) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => {
    return !v || v.length <= max || `Máximo de ${max} caracteres`
  },
  acronym: (v: string) => {
    if (!v) return true
    const pattern = /^[A-Z0-9]+$/
    return pattern.test(v) || 'Sigla deve conter apenas letras maiúsculas e números'
  },
  domain: (v: string) => {
    if (!v) return true
    const pattern = /^[a-z0-9.-]+\.[a-z]{2,}$/
    return pattern.test(v) || 'Formato de domínio inválido (ex: ufms.br)'
  },
}

// File handling
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (event: Event) => {
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
  const validTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!validTypes.includes(file.type)) {
    alert('Formato inválido. Use JPG, PNG ou WebP')
    return
  }

  selectedFile.value = file

  // Generate preview
  const reader = new FileReader()
  reader.onload = (e) => {
    logoPreview.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const clearLogo = () => {
  if (props.institution?.logoUrl && !selectedFile.value) {
    imageRemoved.value = true
  }
  selectedFile.value = null
  logoPreview.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

// Form submission
const handleSubmit = () => {
  if (!isValid.value) return

  if (isEditMode.value) {
    const updateData: InstitutionUpdateRequest = {}

    if (formData.name !== props.institution!.name) {
      updateData.name = formData.name
    }
    if (formData.type !== props.institution!.type) {
      updateData.type = formData.type!
    }
    if (formData.domain !== (props.institution!.domain || '')) {
      updateData.domain = formData.domain || undefined
    }
    if (formData.active !== props.institution!.active) {
      updateData.active = formData.active
    }
    if (selectedFile.value) {
      updateData.logo = selectedFile.value
    }
    if (imageRemoved.value) {
      updateData.removeImage = true
    }

    emit('submit', updateData)
  } else {
    const createData: InstitutionCreateRequest = {
      name: formData.name,
      acronym: formData.acronym.toUpperCase(),
      type: formData.type!,
      domain: formData.domain || undefined,
      active: formData.active,
      logo: selectedFile.value || undefined,
    }

    emit('submit', createData)
  }
}

// Initialize form data
onMounted(() => {
  if (props.institution) {
    formData.name = props.institution.name
    formData.acronym = props.institution.acronym
    formData.type = props.institution.type
    formData.domain = props.institution.domain || ''
    formData.active = props.institution.active

    if (props.institution.logoThumbnailUrl || props.institution.logoUrl) {
      logoPreview.value = props.institution.logoThumbnailUrl || props.institution.logoUrl
    }

    initialData.value = JSON.stringify(formData)
  }
})

// Watch for prop changes
watch(
  () => props.institution,
  (newInstitution) => {
    if (newInstitution) {
      formData.name = newInstitution.name
      formData.acronym = newInstitution.acronym
      formData.type = newInstitution.type
      formData.domain = newInstitution.domain || ''
      formData.active = newInstitution.active

      if (newInstitution.logoThumbnailUrl || newInstitution.logoUrl) {
        logoPreview.value = newInstitution.logoThumbnailUrl || newInstitution.logoUrl
      }

      imageRemoved.value = false
      initialData.value = JSON.stringify(formData)
    }
  }
)
</script>
