<template>
  <div class="institutions-page">
    <PageHeader
      title="Instituições"
      subtitle="Gerencie as instituições do sistema"
    >
      <template #actions>
        <v-btn
          color="primary"
          variant="flat"
          prepend-icon="mdi-plus"
          @click="openCreateDialog"
        >
          Nova Instituição
        </v-btn>
      </template>
    </PageHeader>

    <v-card variant="flat" border>
      <InstitutionList
        :items="institutions"
        :total-items="totalInstitutions"
        :loading="isLoading"
        @update:filters="handleFiltersUpdate"
        @update:pagination="handlePaginationUpdate"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
        @manage-users="handleManageUsers"
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
          {{ isEditMode ? 'Alterar Instituição' : 'Nova Instituição' }}
        </v-card-title>

        <v-card-text class="pa-6">
          <InstitutionForm
            :institution="selectedInstitution || undefined"
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
            Tem certeza que deseja excluir a instituição
            <strong>{{ selectedInstitution?.name }}</strong>?
          </p>
          <p class="text-body-2 text-grey-darken-1">
            Esta ação irá desativar a instituição e todos os vínculos com usuários.
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
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InstitutionList from '@/components/institution/InstitutionList.vue'
import InstitutionForm from '@/components/institution/InstitutionForm.vue'
import { institutionService } from '@/services/institution.service'
import type {
  Institution,
  InstitutionCreateRequest,
  InstitutionUpdateRequest,
  InstitutionListParams,
  InstitutionType,
} from '@/types/institution.types'

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

// State
const institutions = ref<Institution[]>([])
const totalInstitutions = ref(0)
const isLoading = ref(false)
const formDialog = ref(false)
const deleteDialog = ref(false)
const selectedInstitution = ref<Institution | null>(null)
const formLoading = ref(false)
const deleteLoading = ref(false)

const filters = ref<Filters>({
  search: '',
  type: null,
  active: null,
})

const pagination = ref<Pagination>({
  page: 1,
  itemsPerPage: 25,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

const isEditMode = computed(() => !!selectedInstitution.value)

// Data fetching
async function fetchInstitutions(): Promise<void> {
  isLoading.value = true

  try {
    const sortBy = pagination.value.sortBy[0]
    const params: InstitutionListParams = {
      page: pagination.value.page - 1, // Backend uses 0-based pagination
      size: pagination.value.itemsPerPage,
      sort: sortBy?.key || 'name',
      direction: sortBy?.order || 'asc',
      search: filters.value.search || undefined,
      type: filters.value.type || undefined,
      active: filters.value.active ?? undefined,
    }

    const response = await institutionService.list(params)
    institutions.value = response.content
    totalInstitutions.value = response.totalElements
  } catch (err) {
    console.error('Failed to fetch institutions:', err)
    showSnackbar('Erro ao carregar instituições', 'error')
  } finally {
    isLoading.value = false
  }
}

// Event handlers
function handleFiltersUpdate(newFilters: Filters): void {
  filters.value = newFilters
  pagination.value.page = 1 // Reset to first page
  fetchInstitutions()
}

function handlePaginationUpdate(newPagination: Pagination): void {
  pagination.value = newPagination
  fetchInstitutions()
}

// Dialog handlers
function openCreateDialog(): void {
  selectedInstitution.value = null
  formDialog.value = true
}

function openEditDialog(institution: Institution): void {
  selectedInstitution.value = institution
  formDialog.value = true
}

function openDeleteDialog(institution: Institution): void {
  selectedInstitution.value = institution
  deleteDialog.value = true
}

function closeFormDialog(): void {
  formDialog.value = false
  selectedInstitution.value = null
}

function closeDeleteDialog(): void {
  deleteDialog.value = false
  selectedInstitution.value = null
}

// CRUD operations
async function handleFormSubmit(data: InstitutionCreateRequest | InstitutionUpdateRequest): Promise<void> {
  formLoading.value = true

  try {
    if (isEditMode.value && selectedInstitution.value) {
      await institutionService.update(selectedInstitution.value.id, data as InstitutionUpdateRequest)
      showSnackbar('Instituição atualizada com sucesso', 'success')
    } else {
      await institutionService.create(data as InstitutionCreateRequest)
      showSnackbar('Instituição criada com sucesso', 'success')
    }

    closeFormDialog()
    await fetchInstitutions()
  } catch (err: any) {
    console.error('Failed to save institution:', err)
    const message = err.response?.data?.message || 'Erro ao salvar instituição'
    showSnackbar(message, 'error')
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(): Promise<void> {
  if (!selectedInstitution.value) return

  deleteLoading.value = true

  try {
    await institutionService.delete(selectedInstitution.value.id)
    showSnackbar('Instituição excluída com sucesso', 'success')
    closeDeleteDialog()
    await fetchInstitutions()
  } catch (err: any) {
    console.error('Failed to delete institution:', err)
    const message = err.response?.data?.message || 'Erro ao excluir instituição'
    showSnackbar(message, 'error')
  } finally {
    deleteLoading.value = false
  }
}

function handleManageUsers(institution: Institution): void {
  // TODO: Navigate to institution users page
  console.log('Manage users for institution:', institution.id)
  showSnackbar('Funcionalidade em desenvolvimento', 'info')
}

function showSnackbar(message: string, color: string): void {
  snackbar.value = {
    show: true,
    message,
    color,
  }
}

// Lifecycle
onMounted(() => {
  fetchInstitutions()
})
</script>

<style scoped>
.institutions-page {
  /* Add custom styles if needed */
}
</style>
