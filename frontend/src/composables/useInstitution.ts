import { computed } from 'vue'
import { useInstitutionStore } from '@/stores/institution.store'

/**
 * Composable para acesso simplificado ao InstitutionStore.
 *
 * Fornece uma interface conveniente para acessar o estado e ações
 * relacionadas a instituições sem precisar interagir diretamente
 * com o store.
 *
 * @example
 * ```typescript
 * // Em um componente Vue
 * const {
 *   activeInstitution,
 *   hasMultipleInstitutions,
 *   selectInstitution
 * } = useInstitution()
 *
 * // Verificar se tem instituição ativa
 * if (activeInstitution.value) {
 *   console.log(activeInstitution.value.name)
 * }
 *
 * // Selecionar outra instituição
 * await selectInstitution('uuid-da-instituicao')
 * ```
 *
 * @returns Objeto com propriedades reativas e métodos do store
 */
export function useInstitution() {
  const institutionStore = useInstitutionStore()

  // Computed properties para acesso reativo ao estado
  const activeInstitution = computed(() => institutionStore.activeInstitution)
  const userInstitutions = computed(() => institutionStore.userInstitutions)
  const hasMultipleInstitutions = computed(() => institutionStore.hasMultipleInstitutions)
  const hasActiveInstitution = computed(() => institutionStore.hasActiveInstitution)
  const activeInstitutionId = computed(() => institutionStore.activeInstitutionId)
  const isLoading = computed(() => institutionStore.isLoading)

  /**
   * Busca instituições do usuário.
   * Wrapper para institutionStore.fetchUserInstitutions()
   */
  const fetchUserInstitutions = async () => {
    await institutionStore.fetchUserInstitutions()
  }

  /**
   * Seleciona uma instituição como ativa.
   * Wrapper para institutionStore.selectInstitution()
   *
   * @param institutionId ID da instituição a ser selecionada
   */
  const selectInstitution = async (institutionId: string) => {
    await institutionStore.selectInstitution(institutionId)
  }

  /**
   * Limpa a instituição ativa.
   * Wrapper para institutionStore.clearActiveInstitution()
   */
  const clearActiveInstitution = () => {
    institutionStore.clearActiveInstitution()
  }

  /**
   * Reseta o estado completo do store.
   * Wrapper para institutionStore.reset()
   */
  const reset = () => {
    institutionStore.reset()
  }

  return {
    // State (computed para reatividade)
    activeInstitution,
    userInstitutions,
    hasMultipleInstitutions,
    hasActiveInstitution,
    activeInstitutionId,
    isLoading,
    // Actions (métodos wrapper)
    fetchUserInstitutions,
    selectInstitution,
    clearActiveInstitution,
    reset,
  }
}
