import { ref, reactive } from 'vue'
import { processService } from '@/services/process.service'
import type {
  Process,
  ProcessListParams,
  ProcessDocumentationStatus,
  ProcessExternalGuidanceStatus,
  ProcessRiskManagementStatus,
  ProcessMappingStatus,
} from '@/types/process.types'

interface Filters {
  search: string
  active: boolean | null
  valueChainId: string | null
  isCritical: boolean | null
  // Advanced filters
  documentationStatus: ProcessDocumentationStatus | null
  externalGuidanceStatus: ProcessExternalGuidanceStatus | null
  riskManagementStatus: ProcessRiskManagementStatus | null
  mappingStatus: ProcessMappingStatus | null
  responsibleUnitId: string | null
  directUnitId: string | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

export function useProcessList() {
  // State
  const processes = ref<Process[]>([])
  const totalProcesses = ref(0)
  const isLoading = ref(false)

  const filters = reactive<Filters>({
    search: '',
    active: null,
    valueChainId: null,
    isCritical: null,
    // Advanced filters
    documentationStatus: null,
    externalGuidanceStatus: null,
    riskManagementStatus: null,
    mappingStatus: null,
    responsibleUnitId: null,
    directUnitId: null,
  })

  const pagination = reactive<Pagination>({
    page: 1,
    itemsPerPage: 10,
    sortBy: [{ key: 'name', order: 'asc' }],
  })

  // Methods
  async function loadProcesses(): Promise<void> {
    isLoading.value = true

    try {
      const sortBy = pagination.sortBy[0]
      const params: ProcessListParams = {
        page: pagination.page - 1,
        size: pagination.itemsPerPage,
        sort: sortBy?.key || 'name',
        direction: sortBy?.order || 'asc',
        search: filters.search || undefined,
        active: filters.active ?? undefined,
        valueChainId: filters.valueChainId || undefined,
        isCritical: filters.isCritical ?? undefined,
        // Advanced filters
        documentationStatus: filters.documentationStatus || undefined,
        externalGuidanceStatus: filters.externalGuidanceStatus || undefined,
        riskManagementStatus: filters.riskManagementStatus || undefined,
        mappingStatus: filters.mappingStatus || undefined,
        responsibleUnitId: filters.responsibleUnitId || undefined,
        directUnitId: filters.directUnitId || undefined,
      }

      const response = await processService.list(params)
      processes.value = response.content
      totalProcesses.value = response.totalElements
    } catch (err) {
      console.error('Failed to load processes:', err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  function handleFiltersChange(newFilters: Filters): void {
    filters.search = newFilters.search
    filters.active = newFilters.active
    filters.valueChainId = newFilters.valueChainId
    filters.isCritical = newFilters.isCritical
    // Advanced filters
    filters.documentationStatus = newFilters.documentationStatus
    filters.externalGuidanceStatus = newFilters.externalGuidanceStatus
    filters.riskManagementStatus = newFilters.riskManagementStatus
    filters.mappingStatus = newFilters.mappingStatus
    filters.responsibleUnitId = newFilters.responsibleUnitId
    filters.directUnitId = newFilters.directUnitId
    pagination.page = 1
    loadProcesses()
  }

  function handlePageChange(newPagination: Pagination): void {
    pagination.page = newPagination.page
    pagination.itemsPerPage = newPagination.itemsPerPage
    pagination.sortBy = newPagination.sortBy
    loadProcesses()
  }

  return {
    processes,
    totalProcesses,
    filters,
    pagination,
    isLoading,
    loadProcesses,
    handleFiltersChange,
    handlePageChange,
  }
}
