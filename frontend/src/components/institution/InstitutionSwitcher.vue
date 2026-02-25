<template>
  <v-menu offset-y>
    <template #activator="{ props: menuProps }">
      <v-btn
        v-bind="menuProps"
        class="text-none mx-2"
        :disabled="!hasInstitutions"
      >
        <div class="switcher-logo mr-2">
          <v-img
            v-if="activeInstitution?.logoThumbnailUrl || activeInstitution?.logoUrl"
            :src="activeInstitution.logoThumbnailUrl || activeInstitution.logoUrl"
            :alt="activeInstitution?.acronym"
            class="switcher-logo__img"
            contain
          />
          <div v-else class="switcher-logo__placeholder">
            <v-icon size="16" :color="activeInstitution ? 'primary' : 'grey'">mdi-office-building</v-icon>
          </div>
        </div>

        <div class="d-flex flex-column align-start">
          <span class="text-caption text-medium-emphasis">Instituição</span>
          <span class="text-body-2 font-weight-medium">
            {{ activeInstitution?.acronym || 'Selecionar' }}
          </span>
        </div>

        <v-icon end>mdi-chevron-down</v-icon>
      </v-btn>
    </template>

    <v-card min-width="360" max-width="420">
      <v-card-title class="text-subtitle-2 text-medium-emphasis pa-4 pb-2">
        Minhas Instituições
      </v-card-title>

      <v-card-text v-if="userInstitutions.length === 0" class="text-center pa-6">
        <v-icon icon="mdi-office-building-outline" size="48" color="grey-lighten-1" class="mb-2" />
        <p class="text-body-2 text-grey-darken-1">
          Nenhuma instituição disponível
        </p>
      </v-card-text>

      <v-card-text v-else class="pa-3">
        <InstitutionCard
          v-for="userInstitution in userInstitutions"
          :key="userInstitution.institution.id"
          :institution="userInstitution.institution"
          :roles="userInstitution.roles"
          :is-active="activeInstitutionId === userInstitution.institution.id"
          :clickable="activeInstitutionId !== userInstitution.institution.id"
          class="mb-3"
          @click="handleSwitchInstitution"
        />
      </v-card-text>
    </v-card>
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
          <div class="dialog-logo mr-3">
            <v-img
              v-if="selectedInstitution?.logoThumbnailUrl || selectedInstitution?.logoUrl"
              :src="selectedInstitution.logoThumbnailUrl || selectedInstitution.logoUrl"
              :alt="selectedInstitution?.acronym"
              class="dialog-logo__img"
              contain
            />
            <div v-else class="dialog-logo__placeholder">
              <v-icon color="primary">mdi-office-building</v-icon>
            </div>
          </div>
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
import InstitutionCard from './InstitutionCard.vue'

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

<style scoped>
.switcher-logo {
  width: 38px;
  height: 30px;
  border-radius: 5px;
  overflow: hidden;
  flex-shrink: 0;
}

.switcher-logo__img {
  width: 100%;
  height: 100%;
}

.switcher-logo__placeholder {
  width: 100%;
  height: 100%;
  background: rgba(var(--v-theme-primary), 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-logo {
  width: 64px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.dialog-logo__img {
  width: 100%;
  height: 100%;
}

.dialog-logo__placeholder {
  width: 100%;
  height: 100%;
  background: rgba(var(--v-theme-primary), 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
