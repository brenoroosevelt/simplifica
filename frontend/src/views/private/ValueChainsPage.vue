<template>
  <div class="value-chains-page">
    <PageHeader
      title="Cadeias de Valor"
      subtitle="Gerencie as cadeias de valor da instituição"
    >
      <template #actions>
        <v-btn
          color="primary"
          variant="flat"
          prepend-icon="mdi-plus"
          @click="openCreateDialog"
        >
          Nova Cadeia de Valor
        </v-btn>
      </template>
    </PageHeader>

    <v-card variant="flat" border>
      <ValueChainList
        :items="valueChains"
        :total-items="totalValueChains"
        :loading="isLoading"
        :show-filters="true"
        :show-institution="false"
        @update:filters="handleFiltersUpdate"
        @update:pagination="handlePaginationUpdate"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
      />
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog
      v-model="formDialog"
      max-width="600"
      persistent
      scrollable
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          {{ isEditMode ? 'Alterar Cadeia de Valor' : 'Nova Cadeia de Valor' }}
        </v-card-title>

        <v-card-text class="pa-6">
          <ValueChainForm
            :value-chain="selectedValueChain || undefined"
            :loading="formLoading"
            @submit="handleFormSubmit"
            @cancel="closeFormDialog"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog
      v-model="deleteDialog"
      max-width="500"
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          Confirmar Exclusão
        </v-card-title>

        <v-card-text class="pa-6">
          <p class="text-body-1 mb-4">
            Tem certeza que deseja excluir a cadeia de valor
            <strong>{{ selectedValueChain?.name }}</strong>?
          </p>
          <p class="text-body-2 text-grey-darken-1">
            Esta ação irá desativar a cadeia de valor.
            Os dados serão preservados no sistema.
          </p>
        </v-card-text>

        <v-card-actions class="pa-4 d-flex" style="gap: 12px;">
          <v-spacer />
          <v-btn
            variant="text"
            @click="closeDeleteDialog"
          >
            Cancelar
          </v-btn>
          <v-btn
            color="error"
            variant="flat"
            :loading="deleteLoading"
            @click="handleDelete"
          >
            Excluir
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar for feedback -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="3000"
      location="top right"
    >
      {{ snackbar.message }}
      <template #actions>
        <v-btn
          variant="text"
          @click="snackbar.show = false"
        >
          Fechar
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ValueChainList from '@/components/valueChain/ValueChainList.vue'
import ValueChainForm from '@/components/valueChain/ValueChainForm.vue'
import { valueChainService } from '@/services/valueChain.service'
import type {
  ValueChain,
  ValueChainCreateRequest,
  ValueChainUpdateRequest,
  ValueChainListParams,
} from '@/types/valueChain.types'

interface Filters {
  search: string
  active: boolean | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

// State
const valueChains = ref<ValueChain[]>([])
const totalValueChains = ref(0)
const isLoading = ref(false)
const formDialog = ref(false)
const deleteDialog = ref(false)
const selectedValueChain = ref<ValueChain | null>(null)
const formLoading = ref(false)
const deleteLoading = ref(false)

const filters = reactive<Filters>({
  search: '',
  active: null,
})

const pagination = reactive<Pagination>({
  page: 1,
  itemsPerPage: 10,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const snackbar = reactive({
  show: false,
  message: '',
  color: 'success',
})

const isEditMode = computed(() => !!selectedValueChain.value)

// Data fetching
async function loadValueChains(): Promise<void> {
  isLoading.value = true

  try {
    const sortBy = pagination.sortBy[0]
    const params: ValueChainListParams = {
      page: pagination.page - 1, // Backend uses 0-based pagination
      size: pagination.itemsPerPage,
      sort: sortBy?.key || 'name',
      direction: sortBy?.order || 'asc',
      search: filters.search || undefined,
      active: filters.active ?? undefined,
    }

    const response = await valueChainService.list(params)
    valueChains.value = response.content
    totalValueChains.value = response.totalElements
  } catch (err) {
    console.error('Failed to load value chains:', err)
    showSnackbar('Erro ao carregar cadeias de valor', 'error')
  } finally {
    isLoading.value = false
  }
}

// Event handlers
function handleFiltersUpdate(newFilters: Filters): void {
  filters.search = newFilters.search
  filters.active = newFilters.active
  pagination.page = 1 // Reset to first page
  loadValueChains()
}

function handlePaginationUpdate(newPagination: Pagination): void {
  pagination.page = newPagination.page
  pagination.itemsPerPage = newPagination.itemsPerPage
  pagination.sortBy = newPagination.sortBy
  loadValueChains()
}

// Dialog handlers
function openCreateDialog(): void {
  selectedValueChain.value = null
  formDialog.value = true
}

function openEditDialog(valueChain: ValueChain): void {
  selectedValueChain.value = valueChain
  formDialog.value = true
}

function openDeleteDialog(valueChain: ValueChain): void {
  selectedValueChain.value = valueChain
  deleteDialog.value = true
}

function closeFormDialog(): void {
  formDialog.value = false
  selectedValueChain.value = null
}

function closeDeleteDialog(): void {
  deleteDialog.value = false
  selectedValueChain.value = null
}

// CRUD operations
async function handleFormSubmit(data: ValueChainCreateRequest | ValueChainUpdateRequest): Promise<void> {
  formLoading.value = true

  try {
    if (isEditMode.value && selectedValueChain.value) {
      await valueChainService.update(selectedValueChain.value.id, data as ValueChainUpdateRequest)
      showSnackbar('Cadeia de valor atualizada com sucesso', 'success')
    } else {
      await valueChainService.create(data as ValueChainCreateRequest)
      showSnackbar('Cadeia de valor criada com sucesso', 'success')
    }

    closeFormDialog()
    await loadValueChains()
  } catch (err: any) {
    console.error('Failed to save value chain:', err)
    const message = err.response?.data?.message || 'Erro ao salvar cadeia de valor'
    showSnackbar(message, 'error')
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(): Promise<void> {
  if (!selectedValueChain.value) return

  deleteLoading.value = true

  try {
    await valueChainService.delete(selectedValueChain.value.id)
    showSnackbar('Cadeia de valor excluída com sucesso', 'success')
    closeDeleteDialog()
    await loadValueChains()
  } catch (err: any) {
    console.error('Failed to delete value chain:', err)
    const message = err.response?.data?.message || 'Erro ao excluir cadeia de valor'
    showSnackbar(message, 'error')
  } finally {
    deleteLoading.value = false
  }
}

function showSnackbar(message: string, color: string): void {
  snackbar.show = true
  snackbar.message = message
  snackbar.color = color
}

// Lifecycle
onMounted(() => {
  loadValueChains()
})
</script>

<style scoped>
.value-chains-page {
  /* Add custom styles if needed */
}
</style>
