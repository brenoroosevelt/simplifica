<template>
  <div>
    <!-- Filters -->
    <div class="d-flex flex-column flex-md-row align-start align-md-center pa-4" style="gap: 12px;">
      <v-text-field
        v-model="filters.search"
        placeholder="Buscar por título..."
        prepend-inner-icon="mdi-magnify"
        hide-details
        clearable
        class="flex-grow-1"
        @update:model-value="debouncedSearch"
      />
    </div>

    <v-divider />

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
      <!-- Title Column -->
      <template #item.title="{ item }">
        <div>
          <div class="font-weight-medium">{{ item.title }}</div>
          <div
            v-if="item.description"
            class="text-caption text-medium-emphasis text-truncate"
            style="max-width: 400px;"
            :title="item.description"
          >
            {{ item.description.length > 60 ? item.description.substring(0, 60) + '...' : item.description }}
          </div>
        </div>
      </template>

      <!-- Attachment Column -->
      <template #item.attachment="{ item }">
        <div v-if="item.fileUrl">
          <v-chip
            size="small"
            color="primary"
            variant="tonal"
            prepend-icon="mdi-file"
            :href="item.fileUrl"
            target="_blank"
            tag="a"
          >
            {{ item.fileOriginalName || 'Arquivo' }}
          </v-chip>
        </div>
        <div v-else-if="item.externalLink">
          <v-chip
            size="small"
            color="secondary"
            variant="tonal"
            prepend-icon="mdi-link"
            :href="item.externalLink"
            target="_blank"
            tag="a"
          >
            Link externo
          </v-chip>
        </div>
        <span v-else class="text-caption text-medium-emphasis">—</span>
      </template>

      <!-- Created At Column -->
      <template #item.createdAt="{ item }">
        <span class="text-body-2">{{ formatDate(item.createdAt) }}</span>
      </template>

      <!-- Actions Column -->
      <template #item.actions="{ item }">
        <div class="d-flex" style="gap: 4px;">
          <v-tooltip text="Alterar" location="top">
            <template #activator="{ props: tp }">
              <v-btn v-bind="tp" icon="mdi-pencil" size="small" variant="text" @click="$emit('edit', item)" />
            </template>
          </v-tooltip>
          <v-tooltip text="Excluir" location="top">
            <template #activator="{ props: tp }">
              <v-btn v-bind="tp" icon="mdi-delete" size="small" variant="text" color="error" @click="$emit('delete', item)" />
            </template>
          </v-tooltip>
        </div>
      </template>

      <template #loading>
        <v-skeleton-loader type="table-row@5" />
      </template>

      <template #no-data>
        <div class="text-center py-8">
          <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-file-document-outline</v-icon>
          <p class="text-h6 text-medium-emphasis">Nenhum normativo encontrado</p>
          <p class="text-caption text-medium-emphasis">
            {{ hasActiveFilters ? 'Tente ajustar os filtros' : 'Crie o primeiro normativo' }}
          </p>
        </div>
      </template>
    </v-data-table-server>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import type { Normative } from '@/types/normative.types'

interface Props {
  items: Normative[]
  totalItems: number
  loading?: boolean
}

interface Emits {
  (_event: 'update:filters', _filters: Filters): void
  (_event: 'update:pagination', _pagination: Pagination): void
  (_event: 'edit', _normative: Normative): void
  (_event: 'delete', _normative: Normative): void
}

interface Filters { search: string }
interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

withDefaults(defineProps<Props>(), { loading: false })

const emit = defineEmits<Emits>()

const filters = reactive<Filters>({ search: '' })
const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 10,
  sortBy: [{ key: 'createdAt', order: 'desc' }],
})

const hasActiveFilters = computed(() => !!filters.search)

const headers = [
  { title: 'Título', key: 'title', sortable: true },
  { title: 'Anexo', key: 'attachment', sortable: false },
  { title: 'Criado em', key: 'createdAt', sortable: true, width: '140px' },
  { title: 'Ações', key: 'actions', sortable: false, align: 'end' as const, width: '100px' },
]

let searchTimeout: ReturnType<typeof setTimeout> | null = null
const debouncedSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => emit('update:filters', { ...filters }), 500)
}

const handleOptionsUpdate = (options: {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}) => {
  pagination.page = options.page
  pagination.itemsPerPage = options.itemsPerPage
  pagination.sortBy = options.sortBy
  emit('update:pagination', { ...pagination })
}

const formatDate = (dateString: string) =>
  new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(
    new Date(dateString),
  )
</script>

<style scoped>
.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
