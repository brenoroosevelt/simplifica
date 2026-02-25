<template>
  <div>
    <PageHeader title="Normativos" subtitle="Gerencie os normativos institucionais">
      <template #actions>
        <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" @click="openCreateDialog">
          Novo Normativo
        </v-btn>
      </template>
    </PageHeader>

    <v-card variant="flat" border>
      <NormativeList
        :items="normatives"
        :total-items="totalNormatives"
        :loading="isLoading"
        @update:filters="handleFiltersUpdate"
        @update:pagination="handlePaginationUpdate"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
      />
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog v-model="formDialog" max-width="700" persistent scrollable>
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          {{ isEditMode ? 'Alterar Normativo' : 'Novo Normativo' }}
        </v-card-title>
        <v-card-text class="pa-6">
          <NormativeForm
            :normative="selectedNormative || undefined"
            :loading="formLoading"
            @submit="handleFormSubmit"
            @cancel="closeFormDialog"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          Confirmar Exclusão
        </v-card-title>
        <v-card-text class="pa-6">
          <p class="text-body-1 mb-4">
            Tem certeza que deseja excluir o normativo
            <strong>{{ selectedNormative?.title }}</strong>?
          </p>
          <p class="text-body-2 text-grey-darken-1">
            Esta ação é irreversível. O arquivo associado também será removido.
          </p>
        </v-card-text>
        <v-card-actions class="pa-4 d-flex" style="gap: 12px;">
          <v-spacer />
          <v-btn variant="text" @click="closeDeleteDialog">Cancelar</v-btn>
          <v-btn color="error" variant="flat" :loading="deleteLoading" @click="handleDelete">Excluir</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.message }}
      <template #actions>
        <v-btn variant="text" @click="snackbar.show = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import NormativeList from '@/components/normative/NormativeList.vue'
import NormativeForm from '@/components/normative/NormativeForm.vue'
import { normativeService } from '@/services/normative.service'
import type {
  Normative,
  NormativeCreateRequest,
  NormativeUpdateRequest,
  NormativeListParams,
} from '@/types/normative.types'

interface Filters { search: string }
interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

const normatives = ref<Normative[]>([])
const totalNormatives = ref(0)
const isLoading = ref(false)
const formDialog = ref(false)
const deleteDialog = ref(false)
const selectedNormative = ref<Normative | null>(null)
const formLoading = ref(false)
const deleteLoading = ref(false)

const filters = ref<Filters>({ search: '' })
const pagination = ref<Pagination>({
  page: 1,
  itemsPerPage: 10,
  sortBy: [{ key: 'createdAt', order: 'desc' }],
})

const snackbar = ref({ show: false, message: '', color: 'success' })
const isEditMode = computed(() => !!selectedNormative.value)

async function fetchNormatives(): Promise<void> {
  isLoading.value = true
  try {
    const sortBy = pagination.value.sortBy[0]
    const params: NormativeListParams = {
      page: pagination.value.page - 1,
      size: pagination.value.itemsPerPage,
      sort: sortBy?.key || 'createdAt',
      direction: sortBy?.order || 'desc',
      search: filters.value.search || undefined,
    }
    const response = await normativeService.list(params)
    normatives.value = response.content
    totalNormatives.value = response.totalElements
  } catch {
    showSnackbar('Erro ao carregar normativos', 'error')
  } finally {
    isLoading.value = false
  }
}

function handleFiltersUpdate(newFilters: Filters): void {
  filters.value = newFilters
  pagination.value.page = 1
  fetchNormatives()
}

function handlePaginationUpdate(newPagination: Pagination): void {
  pagination.value = newPagination
  fetchNormatives()
}

function openCreateDialog(): void {
  selectedNormative.value = null
  formDialog.value = true
}

function openEditDialog(normative: Normative): void {
  selectedNormative.value = normative
  formDialog.value = true
}

function openDeleteDialog(normative: Normative): void {
  selectedNormative.value = normative
  deleteDialog.value = true
}

function closeFormDialog(): void {
  formDialog.value = false
  selectedNormative.value = null
}

function closeDeleteDialog(): void {
  deleteDialog.value = false
  selectedNormative.value = null
}

async function handleFormSubmit(data: NormativeCreateRequest | NormativeUpdateRequest): Promise<void> {
  formLoading.value = true
  try {
    if (isEditMode.value && selectedNormative.value) {
      const updateData = data as NormativeUpdateRequest
      if (updateData.removeFile) {
        await normativeService.deleteFile(selectedNormative.value.id)
      }
      await normativeService.update(selectedNormative.value.id, {
        title: updateData.title,
        description: updateData.description,
        externalLink: updateData.externalLink,
        file: updateData.file,
      })
      showSnackbar('Normativo atualizado com sucesso', 'success')
    } else {
      await normativeService.create(data as NormativeCreateRequest)
      showSnackbar('Normativo criado com sucesso', 'success')
    }
    closeFormDialog()
    await fetchNormatives()
  } catch (err: unknown) {
    const axiosError = err as { response?: { data?: { message?: string } } }
    showSnackbar(axiosError.response?.data?.message || 'Erro ao salvar normativo', 'error')
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(): Promise<void> {
  if (!selectedNormative.value) return
  deleteLoading.value = true
  try {
    await normativeService.delete(selectedNormative.value.id)
    showSnackbar('Normativo excluído com sucesso', 'success')
    closeDeleteDialog()
    await fetchNormatives()
  } catch (err: unknown) {
    const axiosError = err as { response?: { data?: { message?: string } } }
    showSnackbar(axiosError.response?.data?.message || 'Erro ao excluir normativo', 'error')
  } finally {
    deleteLoading.value = false
  }
}

function showSnackbar(message: string, color: string): void {
  snackbar.value = { show: true, message, color }
}

onMounted(() => fetchNormatives())
</script>
