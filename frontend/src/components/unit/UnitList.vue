<template>
  <div class="unit-list">
    <!-- Filters Section -->
    <div v-if="showFilters" class="d-flex flex-column flex-md-row align-start align-md-center pa-4" style="gap: 12px;">
      <v-text-field
        v-model="filters.search"
        placeholder="Buscar por nome ou sigla..."
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
      <!-- Acronym Column -->
      <template #item.acronym="{ item }">
        <v-chip
          size="small"
          color="primary"
          variant="tonal"
          class="font-weight-medium"
        >
          {{ item.acronym }}
        </v-chip>
      </template>

      <!-- Name Column -->
      <template #item.name="{ item }">
        <div>
          <div class="font-weight-medium">{{ item.name }}</div>
          <div v-if="item.description" class="text-caption text-medium-emphasis text-truncate" style="max-width: 400px;" :title="item.description">
            {{ truncateText(item.description, 50) }}
          </div>
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
            mdi-office-building-outline
          </v-icon>
          <p class="text-h6 text-medium-emphasis">
            Nenhuma unidade encontrada
          </p>
          <p class="text-caption text-medium-emphasis">
            {{ hasActiveFilters ? 'Tente ajustar os filtros' : 'Crie a primeira unidade' }}
          </p>
        </div>
      </template>
    </v-data-table-server>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import type { Unit } from '@/types/unit.types'

interface Props {
  items: Unit[]
  totalItems: number
  loading?: boolean
  showFilters?: boolean
}

interface Emits {
  (_event: 'update:filters', _filters: Filters): void
  (_event: 'update:pagination', _pagination: Pagination): void
  (_event: 'edit', _unit: Unit): void
  (_event: 'delete', _unit: Unit): void
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

withDefaults(defineProps<Props>(), {
  loading: false,
  showFilters: true,
})

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

// Table Headers
const headers = [
  { title: 'Sigla', key: 'acronym', sortable: true, width: '120px' },
  { title: 'Nome', key: 'name', sortable: true },
  { title: 'Status', key: 'active', sortable: true, width: '120px' },
  { title: 'Criada em', key: 'createdAt', sortable: true, width: '130px' },
  { title: 'Ações', key: 'actions', sortable: false, width: '100px' },
]

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

const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
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
