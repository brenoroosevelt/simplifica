import { ref, reactive } from 'vue'
import { trainingService } from '@/services/training.service'
import type { Training, TrainingListParams } from '@/types/training.types'

interface Filters {
  search: string
  active: boolean | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: string
  sortOrder: 'asc' | 'desc'
}

export function useTrainingList() {
  // State
  const trainings = ref<Training[]>([])
  const totalTrainings = ref(0)
  const totalPages = ref(0)
  const isLoading = ref(false)

  const filters = reactive<Filters>({
    search: '',
    active: null,
  })

  const pagination = reactive<Pagination>({
    page: 1,
    itemsPerPage: 12,
    sortBy: 'createdAt',
    sortOrder: 'desc',
  })

  // Methods
  async function loadTrainings(): Promise<void> {
    isLoading.value = true

    try {
      const params: TrainingListParams = {
        page: pagination.page - 1,
        size: pagination.itemsPerPage,
        sort: pagination.sortBy,
        direction: pagination.sortOrder,
        search: filters.search || undefined,
        active: filters.active ?? undefined,
      }

      const response = await trainingService.list(params)
      trainings.value = response.content
      totalTrainings.value = response.totalElements
      totalPages.value = response.totalPages
    } catch (err) {
      console.error('Failed to load trainings:', err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  function handleFiltersChange(newFilters: Partial<Filters>): void {
    Object.assign(filters, newFilters)
    pagination.page = 1
    loadTrainings()
  }

  function handlePageChange(newPage: number): void {
    pagination.page = newPage
    loadTrainings()
  }

  function resetFilters(): void {
    filters.search = ''
    filters.active = null
    pagination.page = 1
    loadTrainings()
  }

  return {
    trainings,
    totalTrainings,
    totalPages,
    filters,
    pagination,
    isLoading,
    loadTrainings,
    handleFiltersChange,
    handlePageChange,
    resetFilters,
  }
}
