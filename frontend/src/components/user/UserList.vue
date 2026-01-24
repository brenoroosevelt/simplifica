<template>
  <div class="user-list">
    <!-- Filters Section -->
    <div class="d-flex flex-column flex-md-row align-start align-md-center pa-4" style="gap: 12px;">
      <v-text-field
        v-model="filters.search"
        placeholder="Buscar por nome ou email..."
        prepend-inner-icon="mdi-magnify"
        hide-details
        clearable
        class="flex-grow-1"
        @update:model-value="debouncedSearch"
      />

      <v-select
        v-model="filters.status"
        :items="statusFilterOptions"
        placeholder="Status"
        prepend-inner-icon="mdi-check-circle"
        hide-details
        clearable
        class="filter-select"
      />

      <v-select
        v-model="filters.role"
        :items="roleFilterOptions"
        placeholder="Função"
        prepend-inner-icon="mdi-shield-account"
        hide-details
        clearable
        class="filter-select"
      />
    </div>

    <v-divider />

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
      <!-- Avatar Column -->
      <template #item.avatar="{ item }">
        <v-avatar
          size="40"
          :color="item.pictureUrl ? 'transparent' : 'primary'"
        >
          <v-img
            v-if="item.pictureUrl"
            :src="item.pictureUrl"
            :alt="item.name"
            cover
          />
          <span v-else class="text-white text-caption">
            {{ getUserInitials(item.name) }}
          </span>
        </v-avatar>
      </template>

      <!-- Name Column -->
      <template #item.name="{ item }">
        <div>
          <div class="font-weight-medium">{{ item.name }}</div>
          <div class="text-caption text-medium-emphasis">{{ item.email }}</div>
        </div>
      </template>

      <!-- Provider Column -->
      <template #item.provider="{ item }">
        <v-chip
          :color="getProviderColor(item.provider)"
          size="small"
          variant="tonal"
        >
          {{ item.provider }}
        </v-chip>
      </template>

      <!-- Role Column -->
      <template #item.role="{ item }">
        <v-chip
          :color="item.role === 'ADMIN' ? 'primary' : 'grey'"
          size="small"
          variant="tonal"
        >
          {{ item.role === 'ADMIN' ? 'Administrador' : 'Usuário' }}
        </v-chip>
      </template>

      <!-- Status Column -->
      <template #item.status="{ item }">
        <v-chip
          :color="getStatusColor(item.status)"
          size="small"
          variant="tonal"
        >
          <v-icon start size="14">
            {{ getStatusIcon(item.status) }}
          </v-icon>
          {{ getStatusLabel(item.status) }}
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

          <v-tooltip text="Gerenciar Instituições" location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-office-building"
                size="small"
                variant="text"
                color="primary"
                @click="$emit('manage-institutions', item)"
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
            mdi-account-outline
          </v-icon>
          <p class="text-h6 text-medium-emphasis">
            Nenhum usuário encontrado
          </p>
          <p class="text-caption text-medium-emphasis">
            {{ hasActiveFilters ? 'Tente ajustar os filtros' : 'Aguardando cadastro de usuários' }}
          </p>
        </div>
      </template>
    </v-data-table-server>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'

interface User {
  id: string
  name: string
  email: string
  pictureUrl?: string
  provider: string
  role: string
  status: string
  createdAt: string
}

interface Props {
  items: User[]
  totalItems: number
  loading?: boolean
}

interface Emits {
  (event: 'update:filters', filters: Filters): void
  (event: 'update:pagination', pagination: Pagination): void
  (event: 'edit', user: User): void
  (event: 'delete', user: User): void
  (event: 'manage-institutions', user: User): void
}

interface Filters {
  search: string
  status: string | null
  role: string | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

defineProps<Props>()

const emit = defineEmits<Emits>()

// Filters State
const filters = reactive<Filters>({
  search: '',
  status: null,
  role: null,
})

// Pagination State
const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 25,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const hasActiveFilters = computed(() => {
  return !!(filters.search || filters.status !== null || filters.role !== null)
})

// Table Headers
const headers = [
  { title: '', key: 'avatar', sortable: false, width: '60px' },
  { title: 'Usuário', key: 'name', sortable: true },
  { title: 'Provider', key: 'provider', sortable: true },
  { title: 'Função', key: 'role', sortable: true },
  { title: 'Status', key: 'status', sortable: true },
  { title: 'Cadastrado em', key: 'createdAt', sortable: true },
  { title: 'Ações', key: 'actions', sortable: false, align: 'end' as const, width: '140px' },
]

// Filter Options
const statusFilterOptions = [
  { title: 'Ativo', value: 'ACTIVE' },
  { title: 'Pendente', value: 'PENDING' },
  { title: 'Inativo', value: 'INACTIVE' },
]

const roleFilterOptions = [
  { title: 'Administrador', value: 'ADMIN' },
  { title: 'Usuário', value: 'USER' },
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
  () => [filters.status, filters.role],
  () => {
    pagination.page = 1 // Reset to first page on filter change
    emitFilters()
  }
)

// Utility functions
const getUserInitials = (name: string): string => {
  if (!name) return '?'
  const names = name.split(' ')
  if (names.length >= 2) {
    return `${names[0]?.[0] || ''}${names[names.length - 1]?.[0] || ''}`.toUpperCase()
  }
  return names[0]?.[0]?.toUpperCase() || '?'
}

const getProviderColor = (provider: string): string => {
  const colors: Record<string, string> = {
    GOOGLE: 'red',
    MICROSOFT: 'blue',
  }
  return colors[provider] || 'grey'
}

const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    ACTIVE: 'success',
    PENDING: 'warning',
    INACTIVE: 'error',
  }
  return colors[status] || 'grey'
}

const getStatusIcon = (status: string): string => {
  const icons: Record<string, string> = {
    ACTIVE: 'mdi-check-circle',
    PENDING: 'mdi-clock-outline',
    INACTIVE: 'mdi-close-circle',
  }
  return icons[status] || 'mdi-help-circle'
}

const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    ACTIVE: 'Ativo',
    PENDING: 'Pendente',
    INACTIVE: 'Inativo',
  }
  return labels[status] || status
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
</style>
