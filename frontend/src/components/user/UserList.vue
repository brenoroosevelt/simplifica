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

      <v-autocomplete
        v-if="props.isAdmin"
        v-model="filters.institutionId"
        :items="institutionOptions"
        :loading="loadingInstitutions"
        placeholder="Instituição"
        prepend-inner-icon="mdi-office-building"
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

      <!-- Institutions Column (only for admin) -->
      <template #item.institutions="{ item }">
        <div v-if="item.institutions && item.institutions.length > 0" class="d-flex flex-wrap" style="gap: 4px;">
          <v-tooltip
            v-for="inst in item.institutions.slice(0, 2)"
            :key="inst.institutionId"
            :text="inst.institutionName"
            location="top"
          >
            <template #activator="{ props: tooltipProps }">
              <v-chip
                v-bind="tooltipProps"
                size="small"
                variant="tonal"
                color="primary"
              >
                {{ inst.institutionAcronym }}
              </v-chip>
            </template>
          </v-tooltip>
          <v-chip
            v-if="item.institutions.length > 2"
            size="small"
            variant="tonal"
            color="grey"
          >
            +{{ item.institutions.length - 2 }}
          </v-chip>
        </div>
        <span v-else class="text-caption text-medium-emphasis">
          Nenhuma
        </span>
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
          <v-tooltip text="Editar Usuário" location="top">
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

          <v-tooltip text="Gerenciar Instituições e Papéis" location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-office-building-cog"
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
import { reactive, computed, watch, onMounted, ref } from 'vue'
import { institutionService } from '@/services/institution.service'
import type { Institution } from '@/types/institution.types'
import type { UserListItem } from '@/types/user.types'
import type { UserStatus } from '@/types/auth.types'

interface Props {
  items: UserListItem[]
  totalItems: number
  loading?: boolean
  isAdmin?: boolean
}

interface Emits {
  (_event: 'update:filters', _filters: Filters): void
  (_event: 'update:pagination', _pagination: Pagination): void
  (_event: 'edit', _user: UserListItem): void
  (_event: 'manage-institutions', _user: UserListItem): void
}

interface Filters {
  search?: string
  status?: UserStatus | null
  institutionId?: string | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

const props = withDefaults(defineProps<Props>(), {
  isAdmin: false,
})

const emit = defineEmits<Emits>()

// Filters State
const filters = reactive<Filters>({
  search: '',
  status: null,
  institutionId: null,
})

// Pagination State
const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 25,
  sortBy: [{ key: 'name', order: 'asc' }],
})

// Institutions for filter
const loadingInstitutions = ref(false)
const institutions = ref<Institution[]>([])

const institutionOptions = computed(() => {
  return institutions.value.map((inst) => ({
    title: inst.name,
    value: inst.id,
  }))
})

const hasActiveFilters = computed(() => {
  return !!(
    filters.search ||
    filters.status !== null ||
    filters.institutionId !== null
  )
})

// Table Headers
const headers = computed(() => {
  const baseHeaders: any[] = [
    { title: '', key: 'avatar', sortable: false, width: '60px' },
    { title: 'Usuário', key: 'name', sortable: true },
    { title: 'Provider', key: 'provider', sortable: true },
    { title: 'Status', key: 'status', sortable: true },
  ]

  // Adicionar coluna de instituições apenas para admin
  if (props.isAdmin) {
    baseHeaders.push({
      title: 'Instituições',
      key: 'institutions',
      sortable: false,
      width: '200px'
    })
  }

  baseHeaders.push({ title: 'Cadastrado em', key: 'createdAt', sortable: true })
  baseHeaders.push({
    title: 'Ações',
    key: 'actions',
    sortable: false,
    align: 'end' as const,
    width: '140px'
  })

  return baseHeaders
})

// Filter Options
const statusFilterOptions = [
  { title: 'Ativo', value: 'ACTIVE' },
  { title: 'Pendente', value: 'PENDING' },
  { title: 'Inativo', value: 'INACTIVE' },
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
  () => [filters.status, filters.institutionId],
  () => {
    pagination.page = 1 // Reset to first page on filter change
    emitFilters()
  }
)

// Load institutions when component mounts if user is admin
onMounted(() => {
  if (props.isAdmin) {
    fetchInstitutions()
  }
})

// Fetch institutions for filter
async function fetchInstitutions(): Promise<void> {
  try {
    loadingInstitutions.value = true
    const response = await institutionService.listInstitutions({
      page: 0,
      size: 1000,
      active: true,
    })
    institutions.value = response.content
  } catch (error: any) {
    // Se não tiver permissão, apenas loga (não quebra a UI)
    if (error?.response?.status === 403) {
      console.warn('User does not have permission to list institutions')
    } else {
      console.error('Failed to fetch institutions:', error)
    }
    institutions.value = []
  } finally {
    loadingInstitutions.value = false
  }
}

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
  return colors[String(provider)] || 'grey'
}

const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    ACTIVE: 'success',
    PENDING: 'warning',
    INACTIVE: 'error',
  }
  return colors[String(status)] || 'grey'
}

const getStatusIcon = (status: string): string => {
  const icons: Record<string, string> = {
    ACTIVE: 'mdi-check-circle',
    PENDING: 'mdi-clock-outline',
    INACTIVE: 'mdi-close-circle',
  }
  return icons[String(status)] || 'mdi-help-circle'
}

const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    ACTIVE: 'Ativo',
    PENDING: 'Pendente',
    INACTIVE: 'Inativo',
  }
  return labels[String(status)] || String(status)
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
