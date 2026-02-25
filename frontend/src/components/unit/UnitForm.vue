<template>
  <v-form ref="formRef" v-model="isValid" @submit.prevent="handleSubmit">
    <v-row>
      <!-- Name Field -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.name"
          label="Nome da Unidade *"
          placeholder="Ex: Tecnologia da Informação"
          :rules="[rules.required, rules.maxLength(255)]"
          :counter="255"
          prepend-inner-icon="mdi-office-building-outline"
          required
        />
      </v-col>

      <!-- Acronym Field -->
      <v-col cols="12" md="6">
        <v-text-field
          v-model="formData.acronym"
          label="Sigla *"
          placeholder="Ex: TI"
          :rules="[rules.required, rules.maxLength(50), rules.acronymPattern]"
          :counter="50"
          hint="Será convertida automaticamente para maiúsculas"
          persistent-hint
          prepend-inner-icon="mdi-tag"
          required
          @update:model-value="normalizeAcronym"
        />
      </v-col>

      <!-- Active Field -->
      <v-col cols="12" md="6">
        <ActiveSwitch v-model="formData.active" />
      </v-col>

      <!-- Institution Field (Read-only) -->
      <v-col cols="12">
        <v-text-field
          :model-value="institutionName"
          label="Instituição"
          prepend-inner-icon="mdi-office-building"
          readonly
          variant="outlined"
          hint="A instituição é automaticamente definida com base na sua seleção atual"
          persistent-hint
        />
      </v-col>

      <!-- Parent Unit Field -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.parentUnit"
          label="Unidade Superior (opcional)"
          placeholder="Ex: Secretaria de Administração"
          :rules="[rules.maxLength(255)]"
          :counter="255"
          prepend-inner-icon="mdi-sitemap"
        />
      </v-col>

      <!-- Description Field -->
      <v-col cols="12">
        <v-textarea
          v-model="formData.description"
          label="Descrição (opcional)"
          placeholder="Descreva a unidade organizacional..."
          :rules="[rules.maxLength(5000)]"
          :counter="5000"
          rows="4"
          prepend-inner-icon="mdi-text"
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
import type { Unit, UnitCreateRequest, UnitUpdateRequest } from '@/types/unit.types'
import ActiveSwitch from '@/components/common/ActiveSwitch.vue'

interface Props {
  unit?: Unit
  loading?: boolean
}

interface Emits {
  (_event: 'submit', _data: UnitCreateRequest | UnitUpdateRequest): void
  (_event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  unit: undefined,
  loading: false,
})

const emit = defineEmits<Emits>()

// Store
const institutionStore = useInstitutionStore()

// Refs
const formRef = ref<HTMLFormElement | null>(null)
const isValid = ref(false)

// Computed
const isEditMode = computed(() => !!props.unit)
const institutionName = computed(() => institutionStore.activeInstitution?.name || '')

// Form Data
const formData = reactive<{
  name: string
  acronym: string
  parentUnit: string
  description: string
  active: boolean
}>({
  name: '',
  acronym: '',
  parentUnit: '',
  description: '',
  active: true,
})

const initialData = ref<string>('')

const hasChanges = computed(() => {
  if (!isEditMode.value) {
    // In create mode, only allow submission if required fields are filled
    return !!(formData.name && formData.acronym)
  }
  const currentData = JSON.stringify(formData)
  return currentData !== initialData.value
})

// Validation Rules
const rules = {
  required: (v: unknown) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => {
    return !v || v.length <= max || `Máximo de ${max} caracteres`
  },
  acronymPattern: (v: string) => {
    if (!v) return true
    const pattern = /^[A-Za-z0-9-]+$/
    return pattern.test(v) || 'Use apenas letras, números e hífens'
  },
}

// Normalize acronym to uppercase
const normalizeAcronym = () => {
  if (formData.acronym) {
    formData.acronym = formData.acronym.toUpperCase().trim()
  }
}

// Form submission
const handleSubmit = () => {
  if (!isValid.value) return

  if (isEditMode.value) {
    const updateData: UnitUpdateRequest = {
      name: formData.name,
      acronym: formData.acronym,
      parentUnit: formData.parentUnit || undefined,
      description: formData.description || undefined,
      active: formData.active,
    }

    emit('submit', updateData)
  } else {
    const createData: UnitCreateRequest = {
      name: formData.name,
      acronym: formData.acronym,
      parentUnit: formData.parentUnit || undefined,
      description: formData.description || undefined,
      active: formData.active,
    }

    emit('submit', createData)
  }
}

// Reset form
const resetForm = () => {
  if (props.unit) {
    formData.name = props.unit.name
    formData.acronym = props.unit.acronym
    formData.parentUnit = props.unit.parentUnit || ''
    formData.description = props.unit.description || ''
    formData.active = props.unit.active
  } else {
    formData.name = ''
    formData.acronym = ''
    formData.parentUnit = ''
    formData.description = ''
    formData.active = true
  }
}

// Initialize form data
onMounted(() => {
  if (props.unit) {
    formData.name = props.unit.name
    formData.acronym = props.unit.acronym
    formData.parentUnit = props.unit.parentUnit || ''
    formData.description = props.unit.description || ''
    formData.active = props.unit.active

    initialData.value = JSON.stringify(formData)
  }
})

// Watch for prop changes
watch(
  () => props.unit,
  (newUnit) => {
    if (newUnit) {
      formData.name = newUnit.name
      formData.acronym = newUnit.acronym
      formData.parentUnit = newUnit.parentUnit || ''
      formData.description = newUnit.description || ''
      formData.active = newUnit.active

      initialData.value = JSON.stringify(formData)
    } else {
      resetForm()
    }
  }
)
</script>
