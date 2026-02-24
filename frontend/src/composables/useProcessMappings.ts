import { ref } from 'vue'
import { processService } from '@/services/process.service'
import type { Process } from '@/types/process.types'

export function useProcessMappings(reloadCallback: () => Promise<void>) {
  // State
  const mappingsDialog = ref(false)
  const selectedProcessForMappings = ref<Process | null>(null)
  const uploadingMappings = ref(false)
  const deletingMapping = ref(false)

  // Methods - Dialog Management
  function openMappingsDialog(process: Process): void {
    selectedProcessForMappings.value = process
    mappingsDialog.value = true
  }

  function closeMappingsDialog(): void {
    mappingsDialog.value = false
    selectedProcessForMappings.value = null
  }

  // Methods - Mappings Operations
  async function uploadMappings(file: File): Promise<void> {
    if (!selectedProcessForMappings.value) return

    uploadingMappings.value = true

    try {
      const updatedProcess = await processService.uploadMappings(
        selectedProcessForMappings.value.id,
        file
      )
      selectedProcessForMappings.value = updatedProcess
      await reloadCallback()
    } catch (err) {
      console.error('Failed to upload mapping:', err)
      throw err
    } finally {
      uploadingMappings.value = false
    }
  }

  async function deleteMapping(mappingId: string): Promise<void> {
    if (!selectedProcessForMappings.value) return

    deletingMapping.value = true

    try {
      await processService.deleteMapping(
        selectedProcessForMappings.value.id,
        mappingId
      )

      // Update local state
      if (selectedProcessForMappings.value.mappings) {
        selectedProcessForMappings.value.mappings =
          selectedProcessForMappings.value.mappings.filter(
            (m) => m.id !== mappingId
          )
      }

      await reloadCallback()
    } catch (err) {
      console.error('Failed to delete mapping:', err)
      throw err
    } finally {
      deletingMapping.value = false
    }
  }

  async function refreshProcess(): Promise<void> {
    await reloadCallback()
  }

  return {
    mappingsDialog,
    selectedProcessForMappings,
    uploadingMappings,
    deletingMapping,
    openMappingsDialog,
    closeMappingsDialog,
    uploadMappings,
    deleteMapping,
    refreshProcess,
  }
}
