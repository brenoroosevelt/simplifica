import { ref } from 'vue'
import { valueChainService } from '@/services/valueChain.service'
import { unitService } from '@/services/unit.service'
import type { ValueChain } from '@/types/valueChain.types'
import type { Unit } from '@/types/unit.types'

export function useProcessReferences() {
  // State
  const valueChains = ref<ValueChain[]>([])
  const units = ref<Unit[]>([])
  const loadingValueChains = ref(false)
  const loadingUnits = ref(false)

  // Methods
  async function loadValueChains(): Promise<void> {
    loadingValueChains.value = true

    try {
      const response = await valueChainService.list({
        page: 0,
        size: 1000,
        active: true,
      })
      valueChains.value = response.content
    } catch (err) {
      console.error('Failed to load value chains:', err)
      throw err
    } finally {
      loadingValueChains.value = false
    }
  }

  async function loadUnits(): Promise<void> {
    loadingUnits.value = true

    try {
      const response = await unitService.list({
        page: 0,
        size: 1000,
        active: true,
      })
      units.value = response.content
    } catch (err) {
      console.error('Failed to load units:', err)
      throw err
    } finally {
      loadingUnits.value = false
    }
  }

  async function loadAll(): Promise<void> {
    await Promise.all([loadValueChains(), loadUnits()])
  }

  return {
    valueChains,
    units,
    loadingValueChains,
    loadingUnits,
    loadValueChains,
    loadUnits,
    loadAll,
  }
}
