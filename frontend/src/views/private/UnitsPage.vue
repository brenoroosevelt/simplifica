<template>
  <div class="units-page">
    <PageHeader
      title="Unidades"
      subtitle="Gerencie as unidades da instituição"
    >
      <template #actions>
        <div class="d-flex" style="gap: 12px;">
          <UnitCsvImport @import-completed="loadUnits" />
          <v-btn
            color="primary"
            variant="flat"
            prepend-icon="mdi-plus"
            @click="openCreateDialog"
          >
            Nova Unidade
          </v-btn>
        </div>
      </template>
    </PageHeader>

    <v-card variant="flat" border>
      <UnitList
        :items="units"
        :total-items="totalUnits"
        :loading="isLoading"
        :show-filters="true"
        @update:filters="handleFiltersUpdate"
        @update:pagination="handlePaginationUpdate"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
      />
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog
      v-model="formDialog"
      max-width="800"
      persistent
      scrollable
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          {{ isEditMode ? 'Alterar Unidade' : 'Nova Unidade' }}
        </v-card-title>

        <v-card-text class="pa-6">
          <UnitForm
            :unit="selectedUnit || undefined"
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
            Tem certeza que deseja excluir a unidade
            <strong>{{ selectedUnit?.name }}</strong> ({{ selectedUnit?.acronym }})?
          </p>
          <p class="text-body-2 text-grey-darken-1">
            Esta ação irá desativar a unidade.
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
import UnitList from '@/components/unit/UnitList.vue'
import UnitForm from '@/components/unit/UnitForm.vue'
import UnitCsvImport from '@/components/unit/UnitCsvImport.vue'
import { unitService } from '@/services/unit.service'
import type {
  Unit,
  UnitCreateRequest,
  UnitUpdateRequest,
  UnitListParams,
} from '@/types/unit.types'

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
const units = ref<Unit[]>([])
const totalUnits = ref(0)
const isLoading = ref(false)
const formDialog = ref(false)
const deleteDialog = ref(false)
const selectedUnit = ref<Unit | null>(null)
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

const isEditMode = computed(() => !!selectedUnit.value)

// Data fetching
async function loadUnits(): Promise<void> {
  isLoading.value = true

  try {
    const sortBy = pagination.sortBy[0]
    const params: UnitListParams = {
      page: pagination.page - 1, // Backend uses 0-based pagination
      size: pagination.itemsPerPage,
      sort: sortBy?.key || 'name',
      direction: sortBy?.order || 'asc',
      search: filters.search || undefined,
      active: filters.active ?? undefined,
    }

    const response = await unitService.list(params)
    units.value = response.content
    totalUnits.value = response.totalElements
  } catch (err) {
    console.error('Failed to load units:', err)
    showSnackbar('Erro ao carregar unidades', 'error')
  } finally {
    isLoading.value = false
  }
}

// Event handlers
function handleFiltersUpdate(newFilters: Filters): void {
  filters.search = newFilters.search
  filters.active = newFilters.active
  pagination.page = 1 // Reset to first page
  loadUnits()
}

function handlePaginationUpdate(newPagination: Pagination): void {
  pagination.page = newPagination.page
  pagination.itemsPerPage = newPagination.itemsPerPage
  pagination.sortBy = newPagination.sortBy
  loadUnits()
}

// Dialog handlers
function openCreateDialog(): void {
  selectedUnit.value = null
  formDialog.value = true
}

function openEditDialog(unit: Unit): void {
  selectedUnit.value = unit
  formDialog.value = true
}

function openDeleteDialog(unit: Unit): void {
  selectedUnit.value = unit
  deleteDialog.value = true
}

function closeFormDialog(): void {
  formDialog.value = false
  selectedUnit.value = null
}

function closeDeleteDialog(): void {
  deleteDialog.value = false
  selectedUnit.value = null
}

// CRUD operations
async function handleFormSubmit(data: UnitCreateRequest | UnitUpdateRequest): Promise<void> {
  formLoading.value = true

  try {
    if (isEditMode.value && selectedUnit.value) {
      await unitService.update(selectedUnit.value.id, data as UnitUpdateRequest)
      showSnackbar('Unidade atualizada com sucesso', 'success')
    } else {
      await unitService.create(data as UnitCreateRequest)
      showSnackbar('Unidade criada com sucesso', 'success')
    }

    closeFormDialog()
    await loadUnits()
  } catch (err) {
    console.error('Failed to save unit:', err)
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao salvar unidade'
    showSnackbar(message, 'error')
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(): Promise<void> {
  if (!selectedUnit.value) return

  deleteLoading.value = true

  try {
    await unitService.delete(selectedUnit.value.id)
    showSnackbar('Unidade excluída com sucesso', 'success')
    closeDeleteDialog()
    await loadUnits()
  } catch (err) {
    console.error('Failed to delete unit:', err)
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao excluir unidade'
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
  loadUnits()
})
</script>

<style scoped>
.units-page {
  /* Add custom styles if needed */
}
</style>
