<template>
  <v-form ref="formRef" v-model="isValid" @submit.prevent="handleSubmit">
    <v-row>
      <!-- Name Field -->
      <v-col cols="12">
        <v-text-field
          v-model="formData.name"
          label="Nome do Processo *"
          placeholder="Ex: Gestão de Contratos"
          :rules="[rules.required, rules.maxLength(255)]"
          :counter="255"
          prepend-inner-icon="mdi-file-tree-outline"
          required
        />
      </v-col>

      <!-- Value Chain Field -->
      <v-col cols="12" md="6">
        <v-autocomplete
          v-model="formData.valueChainId"
          :items="valueChains"
          item-title="name"
          item-value="id"
          label="Cadeia de Valor"
          placeholder="Selecione uma cadeia de valor"
          prepend-inner-icon="mdi-chart-timeline-variant"
          clearable
          :loading="loadingValueChains"
          no-data-text="Nenhuma cadeia de valor encontrada"
        />
      </v-col>

      <!-- Critical Field -->
      <v-col cols="12" md="6">
        <v-checkbox
          v-model="formData.isCritical"
          label="Processo Crítico"
          color="error"
          hide-details
        />
      </v-col>

      <!-- Responsible Unit Field -->
      <v-col cols="12" md="6">
        <v-autocomplete
          v-model="formData.responsibleUnitId"
          :items="units"
          item-title="name"
          item-value="id"
          label="Unidade Responsável"
          placeholder="Selecione uma unidade"
          prepend-inner-icon="mdi-office-building-outline"
          clearable
          :loading="loadingUnits"
          no-data-text="Nenhuma unidade encontrada"
        >
          <template #item="{ props: itemProps, item }">
            <v-list-item v-bind="itemProps">
              <template #prepend>
                <v-chip size="x-small" color="primary" variant="tonal" class="mr-2">
                  {{ item.raw.acronym }}
                </v-chip>
              </template>
            </v-list-item>
          </template>
        </v-autocomplete>
      </v-col>

      <!-- Direct Unit Field -->
      <v-col cols="12" md="6">
        <v-autocomplete
          v-model="formData.directUnitId"
          :items="units"
          item-title="name"
          item-value="id"
          label="Unidade Direta"
          placeholder="Selecione uma unidade"
          prepend-inner-icon="mdi-office-building"
          clearable
          :loading="loadingUnits"
          no-data-text="Nenhuma unidade encontrada"
        >
          <template #item="{ props: itemProps, item }">
            <v-list-item v-bind="itemProps">
              <template #prepend>
                <v-chip size="x-small" color="primary" variant="tonal" class="mr-2">
                  {{ item.raw.acronym }}
                </v-chip>
              </template>
            </v-list-item>
          </template>
        </v-autocomplete>
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

      <!-- Description Field -->
      <v-col cols="12">
        <v-textarea
          v-model="formData.description"
          label="Descrição (opcional)"
          placeholder="Descreva o processo..."
          :rules="[rules.maxLength(5000)]"
          :counter="5000"
          rows="4"
          prepend-inner-icon="mdi-text"
        />
      </v-col>

      <!-- Expansion Panels for Additional Info -->
      <v-col cols="12">
        <v-expansion-panels variant="accordion" multiple>
          <!-- Documentation Panel -->
          <v-expansion-panel>
            <v-expansion-panel-title>
              <div class="d-flex align-center">
                <v-icon class="mr-3" color="primary">mdi-file-document</v-icon>
                <span>Documentação</span>
              </div>
            </v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.documentationStatus"
                    :items="documentationStatusOptions"
                    label="Status da Documentação"
                    prepend-inner-icon="mdi-format-list-bulleted"
                    clearable
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.documentationUrl"
                    label="URL da Documentação"
                    placeholder="https://"
                    prepend-inner-icon="mdi-link"
                    :rules="[rules.url]"
                    clearable
                  />
                </v-col>
              </v-row>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- External Guidance Panel -->
          <v-expansion-panel>
            <v-expansion-panel-title>
              <div class="d-flex align-center">
                <v-icon class="mr-3" color="info">mdi-compass</v-icon>
                <span>Orientação Externa</span>
              </div>
            </v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.externalGuidanceStatus"
                    :items="externalGuidanceStatusOptions"
                    label="Status da Orientação"
                    prepend-inner-icon="mdi-format-list-bulleted"
                    clearable
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.externalGuidanceUrl"
                    label="URL da Orientação"
                    placeholder="https://"
                    prepend-inner-icon="mdi-link"
                    :rules="[rules.url]"
                    clearable
                  />
                </v-col>
              </v-row>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- Risk Management Panel -->
          <v-expansion-panel>
            <v-expansion-panel-title>
              <div class="d-flex align-center">
                <v-icon class="mr-3" color="error">mdi-shield-alert</v-icon>
                <span>Gestão de Riscos</span>
              </div>
            </v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.riskManagementStatus"
                    :items="riskManagementStatusOptions"
                    label="Status da Gestão de Riscos"
                    prepend-inner-icon="mdi-format-list-bulleted"
                    clearable
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.riskManagementUrl"
                    label="URL da Gestão de Riscos"
                    placeholder="https://"
                    prepend-inner-icon="mdi-link"
                    :rules="[rules.url]"
                    clearable
                  />
                </v-col>
              </v-row>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-col>

      <!-- Mapping Status -->
      <v-col cols="12" md="6">
        <v-select
          v-model="formData.mappingStatus"
          :items="mappingStatusOptions"
          label="Status de Mapeamento"
          prepend-inner-icon="mdi-map"
          clearable
        />
      </v-col>

      <!-- Active Field -->
      <v-col cols="12" md="6">
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
import type {
  Process,
  ProcessCreateRequest,
  ProcessUpdateRequest,
  ProcessDocumentationStatus,
  ProcessExternalGuidanceStatus,
  ProcessRiskManagementStatus,
  ProcessMappingStatus,
} from '@/types/process.types'
import type { ValueChain } from '@/types/valueChain.types'
import type { Unit } from '@/types/unit.types'

interface Props {
  process?: Process
  loading?: boolean
  valueChains?: ValueChain[]
  units?: Unit[]
  loadingValueChains?: boolean
  loadingUnits?: boolean
}

interface Emits {
  (_event: 'submit', _data: ProcessCreateRequest | ProcessUpdateRequest): void
  (_event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  process: undefined,
  loading: false,
  valueChains: () => [],
  units: () => [],
  loadingValueChains: false,
  loadingUnits: false,
})

const emit = defineEmits<Emits>()

// Store
const institutionStore = useInstitutionStore()

// Refs
const formRef = ref<HTMLFormElement | null>(null)
const isValid = ref(false)

// Computed
const isEditMode = computed(() => !!props.process)
const institutionName = computed(() => institutionStore.activeInstitution?.name || '')

// Form Data
const formData = reactive<{
  name: string
  valueChainId: string | undefined
  responsibleUnitId: string | undefined
  directUnitId: string | undefined
  description: string
  isCritical: boolean
  documentationStatus: ProcessDocumentationStatus | undefined
  documentationUrl: string
  externalGuidanceStatus: ProcessExternalGuidanceStatus | undefined
  externalGuidanceUrl: string
  riskManagementStatus: ProcessRiskManagementStatus | undefined
  riskManagementUrl: string
  mappingStatus: ProcessMappingStatus | undefined
  active: boolean
}>({
  name: '',
  valueChainId: undefined,
  responsibleUnitId: undefined,
  directUnitId: undefined,
  description: '',
  isCritical: false,
  documentationStatus: undefined,
  documentationUrl: '',
  externalGuidanceStatus: undefined,
  externalGuidanceUrl: '',
  riskManagementStatus: undefined,
  riskManagementUrl: '',
  mappingStatus: undefined,
  active: true,
})

const initialData = ref<string>('')

const hasChanges = computed(() => {
  if (!isEditMode.value) {
    return !!formData.name
  }
  const currentData = JSON.stringify(formData)
  return currentData !== initialData.value
})

// Options for selects
const statusOptions = [
  { title: 'Ativo', value: true },
  { title: 'Inativo', value: false },
]

const documentationStatusOptions = [
  { title: 'Documentado', value: 'DOCUMENTED' },
  { title: 'Não Documentado', value: 'NOT_DOCUMENTED' },
  { title: 'Documentado com Pendências', value: 'DOCUMENTED_WITH_PENDING' },
]

const externalGuidanceStatusOptions = [
  { title: 'Disponível', value: 'AVAILABLE' },
  { title: 'Não Disponível', value: 'NOT_AVAILABLE' },
  { title: 'Disponível com Pendências', value: 'AVAILABLE_WITH_PENDING' },
  { title: 'Não Necessário', value: 'NOT_NECESSARY' },
]

const riskManagementStatusOptions = [
  { title: 'Preparado', value: 'PREPARED' },
  { title: 'Preparado com Pendências', value: 'PREPARED_WITH_PENDING' },
  { title: 'Não Preparado', value: 'NOT_PREPARED' },
]

const mappingStatusOptions = [
  { title: 'Mapeado', value: 'MAPPED' },
  { title: 'Não Mapeado', value: 'NOT_MAPPED' },
  { title: 'Mapeado com Pendências', value: 'MAPPED_WITH_PENDING' },
]

// Validation Rules
const rules = {
  required: (v: unknown) => !!v || 'Campo obrigatório',
  maxLength: (max: number) => (v: string) => {
    return !v || v.length <= max || `Máximo de ${max} caracteres`
  },
  url: (v: string) => {
    if (!v) return true
    const pattern = /^https?:\/\/.+/
    return pattern.test(v) || 'URL deve começar com http:// ou https://'
  },
}

// Form submission
const handleSubmit = () => {
  if (!isValid.value) return

  if (isEditMode.value && props.process) {
    // Helper function to detect changes
    const getChangedFields = <T extends Record<string, any>>(
      current: T,
      original: Partial<T>
    ): Partial<T> => {
      const changed: Partial<T> = {}

      for (const key in current) {
        const currentValue = current[key]
        const originalValue = original[key]

        // Normalize empty strings to undefined for comparison
        const normalizedCurrent = currentValue === '' ? undefined : currentValue
        const normalizedOriginal = originalValue === '' || originalValue === null ? undefined : originalValue

        if (normalizedCurrent !== normalizedOriginal) {
          changed[key] = normalizedCurrent
        }
      }

      return changed
    }

    const updateData = getChangedFields(formData, props.process)

    if (Object.keys(updateData).length === 0) {
      emit('cancel')
      return
    }

    emit('submit', updateData as ProcessUpdateRequest)
  } else {
    const createData: ProcessCreateRequest = {
      name: formData.name,
      valueChainId: formData.valueChainId || undefined,
      responsibleUnitId: formData.responsibleUnitId || undefined,
      directUnitId: formData.directUnitId || undefined,
      description: formData.description || undefined,
      isCritical: formData.isCritical,
      documentationStatus: formData.documentationStatus || undefined,
      documentationUrl: formData.documentationUrl || undefined,
      externalGuidanceStatus: formData.externalGuidanceStatus || undefined,
      externalGuidanceUrl: formData.externalGuidanceUrl || undefined,
      riskManagementStatus: formData.riskManagementStatus || undefined,
      riskManagementUrl: formData.riskManagementUrl || undefined,
      mappingStatus: formData.mappingStatus || undefined,
      active: formData.active,
    }

    emit('submit', createData)
  }
}

// Reset form
const resetForm = () => {
  if (props.process) {
    formData.name = props.process.name
    formData.valueChainId = props.process.valueChainId || undefined
    formData.responsibleUnitId = props.process.responsibleUnitId || undefined
    formData.directUnitId = props.process.directUnitId || undefined
    formData.description = props.process.description || ''
    formData.isCritical = props.process.isCritical
    formData.documentationStatus = props.process.documentationStatus || undefined
    formData.documentationUrl = props.process.documentationUrl || ''
    formData.externalGuidanceStatus = props.process.externalGuidanceStatus || undefined
    formData.externalGuidanceUrl = props.process.externalGuidanceUrl || ''
    formData.riskManagementStatus = props.process.riskManagementStatus || undefined
    formData.riskManagementUrl = props.process.riskManagementUrl || ''
    formData.mappingStatus = props.process.mappingStatus || undefined
    formData.active = props.process.active
  } else {
    formData.name = ''
    formData.valueChainId = undefined
    formData.responsibleUnitId = undefined
    formData.directUnitId = undefined
    formData.description = ''
    formData.isCritical = false
    formData.documentationStatus = undefined
    formData.documentationUrl = ''
    formData.externalGuidanceStatus = undefined
    formData.externalGuidanceUrl = ''
    formData.riskManagementStatus = undefined
    formData.riskManagementUrl = ''
    formData.mappingStatus = undefined
    formData.active = true
  }
}

// Initialize form data
onMounted(() => {
  if (props.process) {
    resetForm()
    initialData.value = JSON.stringify(formData)
  }
})

// Watch for prop changes
watch(
  () => props.process,
  (newProcess) => {
    if (newProcess) {
      resetForm()
      initialData.value = JSON.stringify(formData)
    } else {
      resetForm()
    }
  }
)
</script>
