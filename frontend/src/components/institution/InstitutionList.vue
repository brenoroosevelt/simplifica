<template>
  <div class="institution-list">
    <!-- Filters Section - Apenas para ADMIN -->
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
        v-model="filters.type"
        :items="typeFilterOptions"
        placeholder="Tipo"
        prepend-inner-icon="mdi-tag"
        hide-details
        clearable
        class="filter-select"
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
      :hide-default-footer="!props.isAdmin"
      hover
      @update:options="handleOptionsUpdate"
    >
      <!-- Logo Column -->
      <template #item.logo="{ item }">
        <div class="institution-logo-cell">
          <v-img
            v-if="item.logoThumbnailUrl || item.logoUrl"
            :src="item.logoThumbnailUrl || item.logoUrl"
            :alt="item.acronym"
            class="institution-logo"
            contain
          />
          <div v-else class="institution-logo institution-logo--placeholder">
            <v-icon size="22" color="grey">mdi-office-building</v-icon>
          </div>
        </div>
      </template>

      <!-- Name Column -->
      <template #item.name="{ item }">
        <div>
          <div class="font-weight-medium">{{ item.acronym }}</div>
          <div class="text-caption text-medium-emphasis">{{ item.name }}</div>
        </div>
      </template>

      <!-- Type Column -->
      <template #item.type="{ item }">
        <v-chip
          :color="getTypeColor(item.type)"
          size="small"
          variant="tonal"
        >
          {{ getTypeLabel(item.type) }}
        </v-chip>
      </template>

      <!-- Domain Column -->
      <template #item.domain="{ item }">
        <span v-if="item.domain" class="text-body-2">
          {{ item.domain }}
        </span>
        <span v-else class="text-medium-emphasis text-caption">
          Não informado
        </span>
      </template>

      <!-- Active Column -->
      <template #item.active="{ item }">
        <v-chip
          :color="item.active ? 'success' : 'error'"
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
          <!-- Editar: disponível para ADMIN e MANAGER -->
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

          <!-- Excluir: apenas ADMIN -->
          <v-tooltip v-if="isAdmin" text="Excluir" location="top">
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

          <!-- Gerenciar Usuários: redireciona para página de usuários -->
          <v-tooltip text="Gerenciar Usuários" location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-account-group"
                size="small"
                variant="text"
                color="primary"
                @click="$emit('manage-users', item)"
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
            Nenhuma instituição encontrada
          </p>
          <p class="text-caption text-medium-emphasis">
            {{ hasActiveFilters ? 'Tente ajustar os filtros' : 'Crie a primeira instituição' }}
          </p>
        </div>
      </template>
    </v-data-table-server>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import { InstitutionType } from '@/types/institution.types'
import type { Institution } from '@/types/institution.types'

interface Props {
  items: Institution[]
  totalItems: number
  loading?: boolean
  isAdmin?: boolean
  showFilters?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  isAdmin: false,
  showFilters: true,
})

interface Emits {
  (event: 'update:filters', filters: Filters): void
  (event: 'update:pagination', pagination: Pagination): void
  (event: 'edit', institution: Institution): void
  (event: 'delete', institution: Institution): void
  (event: 'manage-users', institution: Institution): void
}

interface Filters {
  search: string
  type: InstitutionType | null
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
  type: null,
  active: true, // Por padrão, mostrar apenas instituições ativas
})

// Pagination State
const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 25,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const hasActiveFilters = computed(() => {
  return !!(filters.search || filters.type !== null || filters.active !== null)
})

// Table Headers — sorting disabled for non-admin
const headers = computed(() => [
  { title: '', key: 'logo', sortable: false, width: '80px' },
  { title: 'Instituição', key: 'name', sortable: props.isAdmin },
  { title: 'Tipo', key: 'type', sortable: props.isAdmin },
  { title: 'Domínio', key: 'domain', sortable: props.isAdmin },
  { title: 'Status', key: 'active', sortable: props.isAdmin },
  { title: 'Criada em', key: 'createdAt', sortable: props.isAdmin },
  { title: 'Ações', key: 'actions', sortable: false, align: 'end' as const, width: '140px' },
])

// Filter Options
const typeFilterOptions = [
  { title: 'Federal', value: InstitutionType.FEDERAL },
  { title: 'Estadual', value: InstitutionType.ESTADUAL },
  { title: 'Municipal', value: InstitutionType.MUNICIPAL },
  { title: 'Privada', value: InstitutionType.PRIVADA },
]

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
  // Se o filtro active for null, tratar como true (apenas ativas)
  emit('update:filters', {
    ...filters,
    active: filters.active === null ? true : filters.active
  })
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
  () => [filters.type, filters.active],
  () => {
    pagination.page = 1 // Reset to first page on filter change
    emitFilters()
  }
)

// Utility functions
const getTypeLabel = (type: InstitutionType): string => {
  const labels: Record<InstitutionType, string> = {
    FEDERAL: 'Federal',
    ESTADUAL: 'Estadual',
    MUNICIPAL: 'Municipal',
    PRIVADA: 'Privada',
  }
  return labels[type] || type
}

const getTypeColor = (type: InstitutionType): string => {
  const colors: Record<InstitutionType, string> = {
    FEDERAL: 'blue',
    ESTADUAL: 'green',
    MUNICIPAL: 'orange',
    PRIVADA: 'purple',
  }
  return colors[type] || 'grey'
}

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

.institution-logo-cell {
  padding: 6px 0;
}

.institution-logo {
  width: 56px;
  height: 44px;
  border-radius: 6px;
  object-fit: contain;
}

.institution-logo--placeholder {
  background-color: rgb(var(--v-theme-surface-variant), 0.3);
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
