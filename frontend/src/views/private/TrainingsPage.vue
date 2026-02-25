<template>
  <div class="trainings-page">
    <PageHeader
      title="Capacitações"
      subtitle="Gerencie as capacitações e treinamentos da instituição"
    >
      <template #actions>
        <v-btn
          color="primary"
          variant="flat"
          prepend-icon="mdi-plus"
          @click="handleCreate"
        >
          Nova Capacitação
        </v-btn>
      </template>
    </PageHeader>

    <v-card variant="flat" border>
      <TrainingList
        :trainings="trainings"
        :loading="isLoading"
        :page="pagination.page"
        :total-pages="totalPages"
        @view="handleView"
        @edit="handleEdit"
        @delete="openDeleteDialog"
        @page-change="handlePageChange"
        @filter-change="handleFiltersChange"
      />
    </v-card>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">Confirmar Exclusão</v-card-title>
        <v-card-text class="pa-6">
          <p class="text-body-1 mb-4">
            Tem certeza que deseja excluir a capacitação
            <strong>{{ selectedTraining?.title }}</strong>?
          </p>
          <p class="text-body-2 text-medium-emphasis">
            Esta ação irá desativar a capacitação. Os dados serão preservados no sistema.
          </p>
        </v-card-text>
        <v-card-actions class="pa-4 d-flex" style="gap: 12px;">
          <v-spacer />
          <v-btn variant="text" @click="closeDeleteDialog">Cancelar</v-btn>
          <v-btn color="error" variant="flat" :loading="deleteLoading" @click="handleDelete">
            Excluir
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
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
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import { useTrainingList } from '@/composables/useTrainingList'
import { useTrainingForm } from '@/composables/useTrainingForm'
import TrainingList from '@/components/training/TrainingList.vue'
import type { Training } from '@/types/training.types'

const router = useRouter()

// Composables
const {
  trainings,
  totalPages,
  pagination,
  isLoading,
  loadTrainings,
  handleFiltersChange,
  handlePageChange,
} = useTrainingList()

const {
  training: selectedTraining,
  deleteTraining,
  resetTraining,
} = useTrainingForm()

// State
const deleteDialog = ref(false)
const deleteLoading = ref(false)

const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

// Methods
const showSnackbar = (message: string, color = 'success') => {
  snackbar.value = { show: true, message, color }
}

const handleCreate = () => {
  router.push({ name: 'training-new' })
}

const handleView = (training: Training) => {
  router.push({ name: 'training-detail', params: { id: training.id } })
}

const handleEdit = (training: Training) => {
  router.push({ name: 'training-edit', params: { id: training.id } })
}

const openDeleteDialog = (training: Training) => {
  selectedTraining.value = training
  deleteDialog.value = true
}

const closeDeleteDialog = () => {
  deleteDialog.value = false
  resetTraining()
}

const handleDelete = async () => {
  if (!selectedTraining.value) return

  deleteLoading.value = true
  try {
    await deleteTraining(selectedTraining.value.id)
    showSnackbar('Capacitação excluída com sucesso!')
    closeDeleteDialog()
    await loadTrainings()
  } catch (error: any) {
    showSnackbar(error.response?.data?.message || 'Erro ao excluir capacitação', 'error')
  } finally {
    deleteLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadTrainings()
})
</script>

<style scoped>
.trainings-page {
  /* Add custom styles if needed */
}
</style>
