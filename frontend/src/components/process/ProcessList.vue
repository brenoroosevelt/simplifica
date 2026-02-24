<template>
  <div class="process-list">
    <!-- Filters Section -->
    <div v-if="showFilters" class="pa-4">
      <!-- Main Filters Row -->
      <div class="d-flex flex-column flex-md-row align-start align-md-center" style="gap: 12px;">
        <v-text-field
          v-model="filters.search"
          placeholder="Buscar por nome..."
          prepend-inner-icon="mdi-magnify"
          hide-details
          clearable
          variant="outlined"
          density="compact"
          class="flex-grow-1"
          @update:model-value="debouncedSearch"
        />

        <v-autocomplete
          v-model="filters.valueChainId"
          :items="valueChains"
          item-title="name"
          item-value="id"
          placeholder="Cadeia de Valor"
          prepend-inner-icon="mdi-chart-timeline-variant"
          hide-details
          clearable
          variant="outlined"
          density="compact"
          class="filter-select"
        />

        <v-switch
          v-model="filters.isCritical"
          label="Crítico"
          color="error"
          hide-details
          density="compact"
          class="filter-switch"
          :true-value="true"
          :false-value="null"
        />

        <v-select
          v-model="filters.active"
          :items="statusFilterOptions"
          placeholder="Status"
          prepend-inner-icon="mdi-check-circle"
          hide-details
          clearable
          variant="outlined"
          density="compact"
          class="filter-select"
        />

        <!-- Advanced Filters Toggle Icon -->
        <v-btn
          icon="mdi-filter-variant"
          variant="tonal"
          :color="showAdvancedFilters ? 'primary' : 'default'"
          size="small"
          @click="showAdvancedFilters = !showAdvancedFilters"
        >
          <v-icon>mdi-filter-variant</v-icon>
          <v-badge
            v-if="advancedFiltersCount > 0"
            :content="advancedFiltersCount"
            color="error"
            floating
          />
        </v-btn>
      </div>

      <!-- Advanced Filters Content -->
      <v-expand-transition>
        <div v-show="showAdvancedFilters" class="mt-4">
          <v-row dense>
            <!-- Documentation Status -->
            <v-col cols="12" md="6" lg="4">
              <v-select
                v-model="filters.documentationStatus"
                :items="documentationStatusOptions"
                label="Status Documentação"
                prepend-inner-icon="mdi-file-document"
                hide-details
                clearable
                variant="outlined"
                density="compact"
              />
            </v-col>

            <!-- External Guidance Status -->
            <v-col cols="12" md="6" lg="4">
              <v-select
                v-model="filters.externalGuidanceStatus"
                :items="externalGuidanceStatusOptions"
                label="Status Orientação Externa"
                prepend-inner-icon="mdi-compass"
                hide-details
                clearable
                variant="outlined"
                density="compact"
              />
            </v-col>

            <!-- Risk Management Status -->
            <v-col cols="12" md="6" lg="4">
              <v-select
                v-model="filters.riskManagementStatus"
                :items="riskManagementStatusOptions"
                label="Status Gestão de Riscos"
                prepend-inner-icon="mdi-shield-alert"
                hide-details
                clearable
                variant="outlined"
                density="compact"
              />
            </v-col>

            <!-- Mapping Status -->
            <v-col cols="12" md="6" lg="4">
              <v-select
                v-model="filters.mappingStatus"
                :items="mappingStatusOptions"
                label="Status Mapeamento"
                prepend-inner-icon="mdi-map"
                hide-details
                clearable
                variant="outlined"
                density="compact"
              />
            </v-col>

            <!-- Responsible Unit -->
            <v-col cols="12" md="6" lg="4">
              <v-autocomplete
                v-model="filters.responsibleUnitId"
                :items="units"
                item-title="name"
                item-value="id"
                label="Unidade Responsável"
                prepend-inner-icon="mdi-office-building-outline"
                hide-details
                clearable
                variant="outlined"
                density="compact"
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

            <!-- Direct Unit -->
            <v-col cols="12" md="6" lg="4">
              <v-autocomplete
                v-model="filters.directUnitId"
                :items="units"
                item-title="name"
                item-value="id"
                label="Unidade Direta"
                prepend-inner-icon="mdi-office-building"
                hide-details
                clearable
                variant="outlined"
                density="compact"
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
          </v-row>

          <!-- Clear Filters Button -->
          <div class="d-flex justify-end mt-3">
            <v-btn
              variant="text"
              color="default"
              prepend-icon="mdi-close"
              size="small"
              :disabled="advancedFiltersCount === 0"
              @click="clearAdvancedFilters"
            >
              Limpar Filtros
            </v-btn>
          </div>
        </div>
      </v-expand-transition>
    </div>

    <v-divider v-if="showFilters" />

    <!-- Data Table -->
    <v-data-table-server
      v-model:items-per-page="pagination.itemsPerPage"
      v-model:page="pagination.page"
      v-model:sort-by="pagination.sortBy"
      :headers="headers"
      :items="items"
      :items-length="totalItems"
      :loading="loading"
      :items-per-page-options="[10, 25, 50, 100]"
      hover
      @update:options="handleOptionsUpdate"
    >
      <!-- Name Column -->
      <template #item.name="{ item }">
        <div>
          <div class="font-weight-medium">{{ item.name }}</div>
          <div v-if="item.description" class="text-caption text-medium-emphasis text-truncate" style="max-width: 400px;" :title="item.description">
            {{ truncateText(item.description, 60) }}
          </div>
        </div>
      </template>

      <!-- Value Chain Column -->
      <template #item.valueChainName="{ item }">
        <v-chip
          v-if="item.valueChainName"
          size="small"
          color="info"
          variant="tonal"
          class="font-weight-medium"
        >
          <v-icon start size="14">mdi-chart-timeline-variant</v-icon>
          {{ item.valueChainName }}
        </v-chip>
        <span v-else class="text-caption text-medium-emphasis">-</span>
      </template>

      <!-- Responsible Unit Column -->
      <template #item.responsibleUnitAcronym="{ item }">
        <v-chip
          v-if="item.responsibleUnitAcronym"
          size="small"
          color="primary"
          variant="tonal"
          class="font-weight-medium"
        >
          <v-icon start size="14">mdi-office-building-outline</v-icon>
          {{ item.responsibleUnitAcronym }}
        </v-chip>
        <span v-else class="text-caption text-medium-emphasis">-</span>
      </template>

      <!-- Critical Column -->
      <template #item.isCritical="{ item }">
        <v-chip
          :color="item.isCritical ? 'error' : 'default'"
          size="small"
          variant="tonal"
        >
          <v-icon start size="14">
            {{ item.isCritical ? 'mdi-alert-circle' : 'mdi-check-circle' }}
          </v-icon>
          {{ item.isCritical ? 'Crítico' : 'Normal' }}
        </v-chip>
      </template>

      <!-- Mapping Status Column -->
      <template #item.mappingStatus="{ item }">
        <v-chip
          v-if="item.mappingStatus"
          :color="getMappingStatusColor(item.mappingStatus)"
          size="small"
          variant="tonal"
        >
          <v-icon start size="14">
            {{ getMappingStatusIcon(item.mappingStatus) }}
          </v-icon>
          {{ getMappingStatusLabel(item.mappingStatus) }}
        </v-chip>
        <span v-else class="text-caption text-medium-emphasis">-</span>
      </template>

      <!-- Active Column -->
      <template #item.active="{ item }">
        <v-chip
          :color="item.active ? 'success' : 'default'"
          size="small"
          variant="tonal"
        >
          <v-icon start size="14">
            {{ item.active ? 'mdi-check-circle' : 'mdi-close-circle' }}
          </v-icon>
          {{ item.active ? 'Ativo' : 'Inativo' }}
        </v-chip>
      </template>

      <!-- Actions Column -->
      <template #item.actions="{ item }">
        <div class="d-flex" style="gap: 4px;">
          <v-tooltip text="Mapeamentos" location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-file-tree-outline"
                size="small"
                variant="text"
                color="info"
                @click="$emit('view-mappings', item)"
              />
            </template>
          </v-tooltip>

          <v-tooltip text="Alterar" location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-pencil"
                size="small"
                variant="text"
                @click="$emit('edit', item)"
              />
            </template>
          </v-tooltip>

          <v-tooltip text="Excluir" location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-delete"
                size="small"
                variant="text"
                color="error"
                @click="$emit('delete', item)"
              />
            </template>
          </v-tooltip>
        </div>
      </template>

      <!-- Loading Slot -->
      <template #loading>
        <v-skeleton-loader type="table-row@5" />
      </template>

      <!-- No Data Slot -->
      <template #no-data>
        <div class="text-center py-8">
          <v-icon size="64" color="grey-lighten-1" class="mb-4">
            mdi-file-tree-outline
          </v-icon>
          <p class="text-h6 text-medium-emphasis">
            Nenhum processo encontrado
          </p>
          <p class="text-caption text-medium-emphasis">
            {{ hasActiveFilters ? 'Tente ajustar os filtros' : 'Crie o primeiro processo' }}
          </p>
        </div>
      </template>
    </v-data-table-server>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch, onBeforeUnmount, ref } from 'vue'
import type {
  Process,
  ProcessMappingStatus,
  ProcessDocumentationStatus,
  ProcessExternalGuidanceStatus,
  ProcessRiskManagementStatus,
} from '@/types/process.types'
import type { ValueChain } from '@/types/valueChain.types'
import type { Unit } from '@/types/unit.types'

interface Props {
  items: Process[]
  totalItems: number
  loading?: boolean
  showFilters?: boolean
  valueChains?: ValueChain[]
  units?: Unit[]
  loadingUnits?: boolean
}

interface Emits {
  (_event: 'update:filters', _filters: Filters): void
  (_event: 'update:pagination', _pagination: Pagination): void
  (_event: 'edit', _process: Process): void
  (_event: 'delete', _process: Process): void
  (_event: 'view-mappings', _process: Process): void
}

interface Filters {
  search: string
  active: boolean | null
  valueChainId: string | null
  isCritical: boolean | null
  // Advanced filters
  documentationStatus: ProcessDocumentationStatus | null
  externalGuidanceStatus: ProcessExternalGuidanceStatus | null
  riskManagementStatus: ProcessRiskManagementStatus | null
  mappingStatus: ProcessMappingStatus | null
  responsibleUnitId: string | null
  directUnitId: string | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

withDefaults(defineProps<Props>(), {
  loading: false,
  showFilters: true,
  valueChains: () => [],
  units: () => [],
  loadingUnits: false,
})

const emit = defineEmits<Emits>()

// Advanced Filters Toggle
const showAdvancedFilters = ref(false)

// Filters State
const filters = reactive<Filters>({
  search: '',
  active: null,
  valueChainId: null,
  isCritical: null,
  // Advanced filters
  documentationStatus: null,
  externalGuidanceStatus: null,
  riskManagementStatus: null,
  mappingStatus: null,
  responsibleUnitId: null,
  directUnitId: null,
})

// Pagination State
const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 10,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const hasActiveFilters = computed(() => {
  return !!(
    filters.search ||
    filters.active !== null ||
    filters.valueChainId ||
    filters.isCritical !== null ||
    filters.documentationStatus !== null ||
    filters.externalGuidanceStatus !== null ||
    filters.riskManagementStatus !== null ||
    filters.mappingStatus !== null ||
    filters.responsibleUnitId !== null ||
    filters.directUnitId !== null
  )
})

// Count all active filters (main + advanced)
const advancedFiltersCount = computed(() => {
  let count = 0
  // Main filters
  if (filters.search) count++
  if (filters.active !== null) count++
  if (filters.valueChainId) count++
  if (filters.isCritical !== null) count++
  // Advanced filters
  if (filters.documentationStatus !== null) count++
  if (filters.externalGuidanceStatus !== null) count++
  if (filters.riskManagementStatus !== null) count++
  if (filters.mappingStatus !== null) count++
  if (filters.responsibleUnitId !== null) count++
  if (filters.directUnitId !== null) count++
  return count
})

// Table Headers
const headers = [
  { title: 'Nome', key: 'name', sortable: true },
  { title: 'Cadeia de Valor', key: 'valueChainName', sortable: true, width: '180px' },
  { title: 'Unidade Resp.', key: 'responsibleUnitAcronym', sortable: true, width: '140px' },
  { title: 'Crítico', key: 'isCritical', sortable: true, width: '110px' },
  { title: 'Status Mapeamento', key: 'mappingStatus', sortable: true, width: '160px' },
  { title: 'Status', key: 'active', sortable: true, width: '110px' },
  { title: 'Ações', key: 'actions', sortable: false, width: '140px' },
]

// Filter Options
const statusFilterOptions = [
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

// Clear all filters (main + advanced)
const clearAdvancedFilters = () => {
  // Clear main filters
  filters.search = ''
  filters.active = null
  filters.valueChainId = null
  filters.isCritical = null
  // Clear advanced filters
  filters.documentationStatus = null
  filters.externalGuidanceStatus = null
  filters.riskManagementStatus = null
  filters.mappingStatus = null
  filters.responsibleUnitId = null
  filters.directUnitId = null
}

// Debounced search
let searchTimeout: ReturnType<typeof setTimeout> | null = null
const debouncedSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    emitFilters()
  }, 500)
}

// Emit filter changes
const emitFilters = () => {
  emit('update:filters', { ...filters })
}

// Emit pagination changes
const emitPagination = () => {
  emit('update:pagination', { ...pagination })
}

// Handle table options update
const handleOptionsUpdate = (options: {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}) => {
  pagination.page = options.page
  pagination.itemsPerPage = options.itemsPerPage
  pagination.sortBy = options.sortBy
  emitPagination()
}

// Watch filters (except search which uses debounce)
watch(
  () => [
    filters.active,
    filters.valueChainId,
    filters.isCritical,
    filters.documentationStatus,
    filters.externalGuidanceStatus,
    filters.riskManagementStatus,
    filters.mappingStatus,
    filters.responsibleUnitId,
    filters.directUnitId,
  ],
  () => {
    pagination.page = 1 // Reset to first page on filter change
    emitFilters()
  }
)

// Lifecycle
onBeforeUnmount(() => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
})

// Utility functions
const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const getMappingStatusColor = (status: ProcessMappingStatus): string => {
  switch (status) {
    case 'MAPPED':
      return 'success'
    case 'NOT_MAPPED':
      return 'error'
    case 'MAPPED_WITH_PENDING':
      return 'warning'
    default:
      return 'default'
  }
}

const getMappingStatusIcon = (status: ProcessMappingStatus): string => {
  switch (status) {
    case 'MAPPED':
      return 'mdi-check-circle'
    case 'NOT_MAPPED':
      return 'mdi-close-circle'
    case 'MAPPED_WITH_PENDING':
      return 'mdi-alert-circle'
    default:
      return 'mdi-help-circle'
  }
}

const getMappingStatusLabel = (status: ProcessMappingStatus): string => {
  switch (status) {
    case 'MAPPED':
      return 'Mapeado'
    case 'NOT_MAPPED':
      return 'Não Mapeado'
    case 'MAPPED_WITH_PENDING':
      return 'Com Pendências'
    default:
      return '-'
  }
}
</script>

<style scoped>
.filter-select {
  min-width: 180px;
  max-width: 220px;
}

.filter-switch {
  min-width: 100px;
}

@media (max-width: 960px) {
  .filter-select,
  .filter-switch {
    min-width: 100%;
    max-width: 100%;
  }
}

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
