<template>
  <div class="value-chain-list">
    <!-- Filters Section -->
    <div v-if="showFilters" class="d-flex flex-column flex-md-row align-start align-md-center pa-4" style="gap: 12px;">
      <v-text-field
        v-model="filters.search"
        placeholder="Buscar por nome..."
        prepend-inner-icon="mdi-magnify"
        hide-details
        clearable
        class="flex-grow-1"
        @update:model-value="debouncedSearch"
      />

      <v-select
        v-model="filters.active"
        :items="statusFilterOptions"
        placeholder="Status"
        prepend-inner-icon="mdi-check-circle"
        hide-details
        clearable
        class="filter-select"
      />
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
      <!-- Image Column -->
      <template #item.image="{ item }">
        <v-avatar
          size="48"
          rounded="lg"
          :color="item.imageThumbnailUrl || item.imageUrl ? 'transparent' : 'grey-lighten-3'"
        >
          <v-img
            v-if="item.imageThumbnailUrl || item.imageUrl"
            :src="item.imageThumbnailUrl || item.imageUrl"
            :alt="item.name"
            cover
          />
          <v-icon v-else size="24" color="grey">mdi-chart-timeline-variant</v-icon>
        </v-avatar>
      </template>

      <!-- Name Column -->
      <template #item.name="{ item }">
        <div>
          <div class="font-weight-medium">{{ item.name }}</div>
          <div v-if="item.description" class="text-caption text-medium-emphasis text-truncate" style="max-width: 400px;">
            {{ item.description }}
          </div>
        </div>
      </template>

      <!-- Institution Column -->
      <template v-if="showInstitution" #item.institution="{ item }">
        <div>
          <div class="font-weight-medium text-body-2">{{ item.institutionAcronym }}</div>
          <div class="text-caption text-medium-emphasis">{{ item.institutionName }}</div>
        </div>
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
          {{ item.active ? 'Ativa' : 'Inativa' }}
        </v-chip>
      </template>

      <!-- Created At Column -->
      <template #item.createdAt="{ item }">
        <span class="text-body-2">
          {{ formatDate(item.createdAt) }}
        </span>
      </template>

      <!-- Actions Column -->
      <template #item.actions="{ item }">
        <div class="d-flex" style="gap: 4px;">
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
            mdi-chart-timeline-variant-multiple
          </v-icon>
          <p class="text-h6 text-medium-emphasis">
            Nenhuma cadeia de valor encontrada
          </p>
          <p class="text-caption text-medium-emphasis">
            {{ hasActiveFilters ? 'Tente ajustar os filtros' : 'Crie a primeira cadeia de valor' }}
          </p>
        </div>
      </template>
    </v-data-table-server>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import type { ValueChain } from '@/types/valueChain.types'

interface Props {
  items: ValueChain[]
  totalItems: number
  loading?: boolean
  showFilters?: boolean
  showInstitution?: boolean
}

interface Emits {
  (_event: 'update:filters', _filters: Filters): void
  (_event: 'update:pagination', _pagination: Pagination): void
  (_event: 'edit', _valueChain: ValueChain): void
  (_event: 'delete', _valueChain: ValueChain): void
}

interface Filters {
  search: string
  active: boolean | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

const emit = defineEmits<Emits>()

// Filters State
const filters = reactive<Filters>({
  search: '',
  active: null,
})

// Pagination State
const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 10,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const hasActiveFilters = computed(() => {
  return !!(filters.search || filters.active !== null)
})

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showFilters: true,
  showInstitution: false,
})

// Table Headers
const headers = computed(() => {
  const baseHeaders = [
    { title: '', key: 'image', sortable: false, width: '80px' },
    { title: 'Nome', key: 'name', sortable: true },
  ]

  // Add institution column if showInstitution prop is true
  if (props.showInstitution) {
    baseHeaders.push({ title: 'Instituição', key: 'institution', sortable: true })
  }

  baseHeaders.push(
    { title: 'Status', key: 'active', sortable: true },
    { title: 'Criada em', key: 'createdAt', sortable: true },
    { title: 'Ações', key: 'actions', sortable: false, width: '100px' }
  )

  return baseHeaders
})

// Filter Options
const statusFilterOptions = [
  { title: 'Ativa', value: true },
  { title: 'Inativa', value: false },
]

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
  () => filters.active,
  () => {
    pagination.page = 1 // Reset to first page on filter change
    emitFilters()
  }
)

// Utility functions
const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date)
}
</script>

<style scoped>
.filter-select {
  min-width: 160px;
  max-width: 200px;
}

@media (max-width: 960px) {
  .filter-select {
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
