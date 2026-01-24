<template>
  <v-menu offset-y>
    <template #activator="{ props: menuProps }">
      <v-btn
        v-bind="menuProps"
        class="text-none mx-2"
        :disabled="!hasInstitutions"
      >
        <v-avatar
          size="32"
          :color="activeInstitution ? 'primary' : 'grey'"
          variant="tonal"
          class="mr-2"
        >
          <v-img
            v-if="activeInstitution?.logoThumbnailUrl || activeInstitution?.logoUrl"
            :src="activeInstitution.logoThumbnailUrl || activeInstitution.logoUrl"
            :alt="activeInstitution.acronym"
            cover
          />
          <v-icon v-else size="20">mdi-office-building</v-icon>
        </v-avatar>

        <div class="d-flex flex-column align-start">
          <span class="text-caption text-medium-emphasis">Instituição</span>
          <span class="text-body-2 font-weight-medium">
            {{ activeInstitution?.acronym || 'Selecionar' }}
          </span>
        </div>

        <v-icon end>mdi-chevron-down</v-icon>
      </v-btn>
    </template>

    <v-list min-width="280">
      <v-list-subheader>Suas Instituições</v-list-subheader>

      <v-list-item
        v-for="userInstitution in userInstitutions"
        :key="userInstitution.institution.id"
        :active="activeInstitutionId === userInstitution.institution.id"
        @click="handleSwitchInstitution(userInstitution.institution.id)"
      >
        <template #prepend>
          <v-avatar
            size="40"
            color="primary"
            variant="tonal"
          >
            <v-img
              v-if="userInstitution.institution.logoThumbnailUrl || userInstitution.institution.logoUrl"
              :src="userInstitution.institution.logoThumbnailUrl || userInstitution.institution.logoUrl"
              :alt="userInstitution.institution.acronym"
              cover
            />
            <v-icon v-else>mdi-office-building</v-icon>
          </v-avatar>
        </template>

        <v-list-item-title class="font-weight-medium">
          {{ userInstitution.institution.acronym }}
        </v-list-item-title>

        <v-list-item-subtitle class="text-caption">
          {{ userInstitution.institution.name }}
        </v-list-item-subtitle>

        <template #append>
          <v-chip
            v-if="activeInstitutionId === userInstitution.institution.id"
            color="primary"
            size="x-small"
            variant="tonal"
          >
            Ativa
          </v-chip>
        </template>
      </v-list-item>

      <v-divider v-if="userInstitutions.length === 0" class="my-2" />

      <v-list-item v-if="userInstitutions.length === 0" disabled>
        <v-list-item-title class="text-medium-emphasis text-center">
          Nenhuma instituição disponível
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>

  <!-- Confirmation Dialog -->
  <v-dialog v-model="showConfirmDialog" max-width="400">
    <v-card>
      <v-card-title class="text-h6 font-weight-medium pa-5">
        Trocar Instituição?
      </v-card-title>

      <v-card-text class="px-5 pb-5">
        <p class="mb-2">
          Você está prestes a trocar para:
        </p>
        <div class="d-flex align-center pa-3 bg-grey-lighten-4 rounded">
          <v-avatar
            size="48"
            color="primary"
            variant="tonal"
            class="mr-3"
          >
            <v-img
              v-if="selectedInstitution?.logoThumbnailUrl || selectedInstitution?.logoUrl"
              :src="selectedInstitution.logoThumbnailUrl || selectedInstitution.logoUrl"
              :alt="selectedInstitution.acronym"
              cover
            />
            <v-icon v-else>mdi-office-building</v-icon>
          </v-avatar>
          <div>
            <div class="font-weight-bold">
              {{ selectedInstitution?.acronym }}
            </div>
            <div class="text-caption text-medium-emphasis">
              {{ selectedInstitution?.name }}
            </div>
          </div>
        </div>
        <p class="mt-3 text-caption text-medium-emphasis">
          A página será recarregada para aplicar as mudanças.
        </p>
      </v-card-text>

      <v-card-actions class="px-5 pb-4 d-flex" style="gap: 12px;">
        <v-spacer />
        <v-btn
          variant="text"
          @click="cancelSwitch"
        >
          Cancelar
        </v-btn>
        <v-btn
          color="primary"
          variant="flat"
          :loading="switching"
          @click="confirmSwitch"
        >
          Confirmar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useInstitutionStore } from '@/stores/institution.store'
import type { Institution } from '@/types/institution.types'

const institutionStore = useInstitutionStore()

const showConfirmDialog = ref(false)
const selectedInstitution = ref<Institution | null>(null)
const switching = ref(false)

const activeInstitution = computed(() => institutionStore.activeInstitution)
const activeInstitutionId = computed(() => institutionStore.activeInstitutionId)
const userInstitutions = computed(() => institutionStore.userInstitutions)
const hasInstitutions = computed(() => userInstitutions.value.length > 0)

const handleSwitchInstitution = (institutionId: string) => {
  if (institutionId === activeInstitutionId.value) {
    return
  }

  const userInstitution = userInstitutions.value.find(
    (ui) => ui.institution.id === institutionId
  )

  if (userInstitution) {
    selectedInstitution.value = userInstitution.institution
    showConfirmDialog.value = true
  }
}

const cancelSwitch = () => {
  showConfirmDialog.value = false
  selectedInstitution.value = null
}

const confirmSwitch = async () => {
  if (!selectedInstitution.value) return

  switching.value = true

  try {
    await institutionStore.selectInstitution(selectedInstitution.value.id)

    // Reload the page to refresh all data with new institution context
    window.location.reload()
  } catch (error) {
    console.error('Failed to switch institution:', error)
    switching.value = false
    // TODO: Show error notification
  }
}
</script>
