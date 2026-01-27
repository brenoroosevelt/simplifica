<template>
  <div class="processes-page">
    <PageHeader
      title="Portfólio de Processos"
      subtitle="Gerencie os processos da instituição"
    >
      <template #actions>
        <v-btn
          color="primary"
          variant="flat"
          prepend-icon="mdi-plus"
          @click="openCreateDialog"
        >
          Novo Processo
        </v-btn>
      </template>
    </PageHeader>

    <v-card variant="flat" border>
      <ProcessList
        :items="processes"
        :total-items="totalProcesses"
        :loading="isLoading"
        :show-filters="true"
        :value-chains="valueChains"
        @update:filters="handleFiltersChange"
        @update:pagination="handlePageChange"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
        @view-mappings="openMappingsDialog"
      />
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog
      v-model="formDialog"
      max-width="900"
      persistent
      scrollable
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          {{ isEditMode ? 'Alterar Processo' : 'Novo Processo' }}
        </v-card-title>

        <v-card-text class="pa-6">
          <ProcessForm
            :process="selectedProcess || undefined"
            :loading="formLoading"
            :value-chains="valueChains"
            :units="units"
            :loading-value-chains="loadingValueChains"
            :loading-units="loadingUnits"
            @submit="handleFormSubmit"
            @cancel="closeFormDialog"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Mappings Dialog -->
    <v-dialog
      v-model="mappingsDialog"
      max-width="1000"
      persistent
      scrollable
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          <div class="d-flex align-center justify-space-between w-100">
            <div>
              <div>Mapeamentos - {{ selectedProcessForMappings?.name }}</div>
              <div class="text-caption text-medium-emphasis font-weight-regular mt-1">
                Gerencie os arquivos HTML de mapeamento do processo
              </div>
            </div>
            <v-btn
              icon="mdi-close"
              variant="text"
              @click="closeMappingsDialog"
            />
          </div>
        </v-card-title>

        <v-card-text class="pa-6">
          <ProcessMappingUpload
            ref="mappingUploadRef"
            :mappings="selectedProcessForMappings?.mappings || []"
            :uploading="uploadingMappings"
            :deleting="deletingMapping"
            @upload="handleMappingsUpload"
            @delete="handleMappingDelete"
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
            Tem certeza que deseja excluir o processo
            <strong>{{ selectedProcess?.name }}</strong>?
          </p>
          <p class="text-body-2 text-grey-darken-1">
            Esta ação irá desativar o processo.
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
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ProcessList from '@/components/process/ProcessList.vue'
import ProcessForm from '@/components/process/ProcessForm.vue'
import ProcessMappingUpload from '@/components/process/ProcessMappingUpload.vue'
import { useSnackbar } from '@/composables/useSnackbar'
import { useProcessList } from '@/composables/useProcessList'
import { useProcessForm } from '@/composables/useProcessForm'
import { useProcessMappings } from '@/composables/useProcessMappings'
import { useProcessReferences } from '@/composables/useProcessReferences'
import type { ProcessCreateRequest, ProcessUpdateRequest } from '@/types/process.types'

// Composables
const { snackbar, showSnackbar } = useSnackbar()

const {
  processes,
  totalProcesses,
  isLoading,
  loadProcesses,
  handleFiltersChange,
  handlePageChange,
} = useProcessList()

const {
  formDialog,
  deleteDialog,
  selectedProcess,
  isEditMode,
  formLoading,
  deleteLoading,
  openCreateDialog,
  openEditDialog,
  closeFormDialog,
  openDeleteDialog,
  closeDeleteDialog,
  handleFormSubmit: submitForm,
  confirmDelete,
} = useProcessForm(loadProcesses)

const {
  mappingsDialog,
  selectedProcessForMappings,
  uploadingMappings,
  deletingMapping,
  openMappingsDialog,
  closeMappingsDialog,
  uploadMappings,
  deleteMapping,
} = useProcessMappings(loadProcesses)

const {
  valueChains,
  units,
  loadingValueChains,
  loadingUnits,
  loadAll: loadReferences,
} = useProcessReferences()

// Refs for child components
const mappingUploadRef = ref<InstanceType<typeof ProcessMappingUpload> | null>(null)

// Wrapper functions to handle errors and show feedback
async function handleFormSubmit(data: ProcessCreateRequest | ProcessUpdateRequest): Promise<void> {
  try {
    await submitForm(data)
    showSnackbar(
      isEditMode.value ? 'Processo atualizado com sucesso' : 'Processo criado com sucesso',
      'success'
    )
  } catch (err) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao salvar processo'
    showSnackbar(message, 'error')
  }
}

async function handleDelete(): Promise<void> {
  try {
    await confirmDelete()
    showSnackbar('Processo excluído com sucesso', 'success')
  } catch (err) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao excluir processo'
    showSnackbar(message, 'error')
  }
}

async function handleMappingsUpload(files: File[]): Promise<void> {
  try {
    await uploadMappings(files)
    showSnackbar(`${files.length} arquivo(s) enviado(s) com sucesso`, 'success')
  } catch (err) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao fazer upload dos mapeamentos'
    showSnackbar(message, 'error')
  }
}

async function handleMappingDelete(mappingId: string): Promise<void> {
  try {
    await deleteMapping(mappingId)
    showSnackbar('Mapeamento excluído com sucesso', 'success')
    mappingUploadRef.value?.closeDeleteDialog()
  } catch (err) {
    const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao excluir mapeamento'
    showSnackbar(message, 'error')
  }
}

// Lifecycle
onMounted(async () => {
  try {
    await Promise.all([loadProcesses(), loadReferences()])
  } catch {
    showSnackbar('Erro ao carregar dados', 'error')
  }
})
</script>

<style scoped>
.processes-page {
  /* Add custom styles if needed */
}
</style>
