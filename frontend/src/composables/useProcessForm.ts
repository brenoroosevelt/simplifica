import { ref, computed } from 'vue'
import { processService } from '@/services/process.service'
import type {
  Process,
  ProcessCreateRequest,
  ProcessUpdateRequest,
} from '@/types/process.types'

export function useProcessForm(reloadCallback: () => Promise<void>) {
  // State
  const formDialog = ref(false)
  const deleteDialog = ref(false)
  const selectedProcess = ref<Process | null>(null)
  const formLoading = ref(false)
  const deleteLoading = ref(false)

  // Computed
  const isEditMode = computed(() => !!selectedProcess.value)

  // Methods - Form Dialog
  function openCreateDialog(): void {
    selectedProcess.value = null
    formDialog.value = true
  }

  function openEditDialog(process: Process): void {
    selectedProcess.value = process
    formDialog.value = true
  }

  function closeFormDialog(): void {
    formDialog.value = false
    selectedProcess.value = null
  }

  // Methods - Delete Dialog
  function openDeleteDialog(process: Process): void {
    selectedProcess.value = process
    deleteDialog.value = true
  }

  function closeDeleteDialog(): void {
    deleteDialog.value = false
    selectedProcess.value = null
  }

  // Methods - CRUD Operations
  async function handleFormSubmit(
    data: ProcessCreateRequest | ProcessUpdateRequest
  ): Promise<void> {
    formLoading.value = true

    try {
      if (isEditMode.value && selectedProcess.value) {
        await processService.update(
          selectedProcess.value.id,
          data as ProcessUpdateRequest
        )
      } else {
        await processService.create(data as ProcessCreateRequest)
      }

      closeFormDialog()
      await reloadCallback()
    } catch (err) {
      console.error('Failed to save process:', err)
      throw err
    } finally {
      formLoading.value = false
    }
  }

  async function confirmDelete(): Promise<void> {
    if (!selectedProcess.value) return

    deleteLoading.value = true

    try {
      await processService.delete(selectedProcess.value.id)
      closeDeleteDialog()
      await reloadCallback()
    } catch (err) {
      console.error('Failed to delete process:', err)
      throw err
    } finally {
      deleteLoading.value = false
    }
  }

  return {
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
    handleFormSubmit,
    confirmDelete,
  }
}
