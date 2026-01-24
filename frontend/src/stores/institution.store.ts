import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Institution, UserInstitution } from '@/types/institution.types'
import { institutionService } from '@/services/institution.service'

/**
 * Store Pinia para gerenciamento de instituições.
 *
 * Responsabilidades:
 * - Gerenciar instituição ativa do usuário
 * - Armazenar lista de instituições vinculadas ao usuário
 * - Persistir seleção em localStorage
 * - Auto-selecionar instituição quando usuário tem apenas uma
 *
 * @example
 * ```typescript
 * const institutionStore = useInstitutionStore()
 *
 * // Buscar instituições do usuário
 * await institutionStore.fetchUserInstitutions()
 *
 * // Selecionar instituição
 * await institutionStore.selectInstitution(institutionId)
 *
 * // Verificar se tem instituição ativa
 * if (institutionStore.hasActiveInstitution) {
 *   console.log(institutionStore.activeInstitution.name)
 * }
 * ```
 */
export const useInstitutionStore = defineStore('institution', () => {
  // State
  const activeInstitution = ref<Institution | null>(null)
  const userInstitutions = ref<UserInstitution[]>([])
  const isLoading = ref(false)

  // Getters
  const hasActiveInstitution = computed(() => !!activeInstitution.value)
  const hasMultipleInstitutions = computed(() => userInstitutions.value.length > 1)
  const activeInstitutionId = computed(() => activeInstitution.value?.id || null)

  /**
   * Busca instituições vinculadas ao usuário logado.
   * Se o usuário tiver apenas uma instituição, seleciona automaticamente.
   * Se houver instituição salva em localStorage, tenta restaurá-la.
   *
   * @throws Error se falhar ao buscar instituições
   */
  async function fetchUserInstitutions(): Promise<void> {
    isLoading.value = true
    try {
      userInstitutions.value = await institutionService.getUserInstitutions()

      // Auto-seleciona se o usuário tem apenas uma instituição
      const firstInstitution = userInstitutions.value[0]
      if (userInstitutions.value.length === 1 && !activeInstitution.value && firstInstitution) {
        await selectInstitution(firstInstitution.institution.id)
      } else if (userInstitutions.value.length > 1) {
        // Tenta restaurar instituição do localStorage
        const storedId = localStorage.getItem('active_institution_id')
        if (storedId) {
          const exists = userInstitutions.value.find(
            (ui) => ui.institution.id === storedId
          )
          if (exists) {
            await selectInstitution(storedId)
          } else {
            // Instituição salva não existe mais, limpar
            localStorage.removeItem('active_institution_id')
          }
        }
      }
    } catch (error) {
      console.error('Failed to fetch user institutions:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Seleciona uma instituição como ativa.
   * Valida se a instituição pertence ao usuário.
   * Persiste a seleção em localStorage.
   *
   * @param institutionId ID da instituição a ser selecionada
   * @throws Error se instituição não for encontrada nas instituições do usuário
   */
  async function selectInstitution(institutionId: string): Promise<void> {
    const userInstitution = userInstitutions.value.find(
      (ui) => ui.institution.id === institutionId
    )

    if (!userInstitution) {
      throw new Error('Institution not found in user institutions')
    }

    activeInstitution.value = userInstitution.institution
    localStorage.setItem('active_institution_id', institutionId)
  }

  /**
   * Limpa a instituição ativa.
   * Remove do state e do localStorage.
   */
  function clearActiveInstitution(): void {
    activeInstitution.value = null
    localStorage.removeItem('active_institution_id')
  }

  /**
   * Reseta o estado completo do store.
   * Limpa todas as referências e remove do localStorage.
   * Usado principalmente no logout.
   */
  function reset(): void {
    activeInstitution.value = null
    userInstitutions.value = []
    isLoading.value = false
    localStorage.removeItem('active_institution_id')
  }

  return {
    // State
    activeInstitution,
    userInstitutions,
    isLoading,
    // Getters
    hasActiveInstitution,
    hasMultipleInstitutions,
    activeInstitutionId,
    // Actions
    fetchUserInstitutions,
    selectInstitution,
    clearActiveInstitution,
    reset,
  }
})
