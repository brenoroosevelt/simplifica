<template>
  <div class="institution-selection-page">
    <v-container fluid class="fill-height">
      <v-row align="center" justify="center">
        <v-col cols="12" sm="10" md="8" lg="6" xl="4">
          <v-card elevation="4" rounded="lg">
            <v-card-title class="text-h5 text-center pa-6 font-weight-bold">
              Selecione uma Instituição
            </v-card-title>
            <v-divider />

            <v-card-text class="pa-6">
              <p class="text-body-1 text-center mb-6 text-grey-darken-1">
                Você está vinculado a múltiplas instituições. Selecione uma para continuar.
              </p>

              <v-progress-linear
                v-if="isLoading"
                indeterminate
                color="primary"
                class="mb-4"
              />

              <v-alert
                v-if="error"
                type="error"
                variant="tonal"
                closable
                class="mb-4"
                @click:close="error = null"
              >
                {{ error }}
              </v-alert>

              <v-list v-if="!isLoading && userInstitutions.length > 0" class="elevation-0">
                <v-list-item
                  v-for="userInst in userInstitutions"
                  :key="userInst.institution.id"
                  :title="userInst.institution.name"
                  :subtitle="userInst.institution.acronym"
                  :prepend-avatar="userInst.institution.logoThumbnailUrl || undefined"
                  class="mb-2 rounded border"
                  hover
                  @click="handleSelect(userInst.institution.id)"
                >
                  <template #prepend>
                    <v-avatar v-if="userInst.institution.logoThumbnailUrl" size="48">
                      <v-img :src="userInst.institution.logoThumbnailUrl" />
                    </v-avatar>
                    <v-avatar v-else color="primary" size="48">
                      <v-icon color="white">mdi-office-building</v-icon>
                    </v-avatar>
                  </template>

                  <template #append>
                    <v-chip
                      :color="getTypeColor(userInst.institution.type)"
                      size="small"
                      variant="flat"
                    >
                      {{ getTypeLabel(userInst.institution.type) }}
                    </v-chip>
                    <v-icon>mdi-chevron-right</v-icon>
                  </template>
                </v-list-item>
              </v-list>

              <div v-if="!isLoading && userInstitutions.length === 0" class="text-center py-8">
                <v-icon icon="mdi-alert-circle-outline" size="64" color="warning" class="mb-2" />
                <p class="text-body-1 text-grey-darken-1">
                  Você não está vinculado a nenhuma instituição.
                </p>
                <p class="text-body-2 text-grey">
                  Entre em contato com um administrador para obter acesso.
                </p>
              </div>
            </v-card-text>

            <v-divider />

            <v-card-actions class="pa-4">
              <v-btn
                color="error"
                variant="text"
                prepend-icon="mdi-logout"
                @click="handleLogout"
              >
                Sair
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useInstitution } from '@/composables/useInstitution'
import { useAuth } from '@/composables/useAuth'
import { InstitutionType } from '@/types/institution.types'

const router = useRouter()
const route = useRoute()
const { userInstitutions, isLoading, selectInstitution } = useInstitution()
const { logout } = useAuth()

const error = ref<string | null>(null)

onMounted(async () => {
  if (userInstitutions.value.length === 0) {
    error.value = 'Nenhuma instituição encontrada'
  }
})

async function handleSelect(institutionId: string): Promise<void> {
  try {
    error.value = null
    await selectInstitution(institutionId)

    const redirectPath = (route.query.redirect as string) || '/dashboard'
    await router.push(redirectPath)
  } catch (err) {
    console.error('Failed to select institution:', err)
    error.value = 'Erro ao selecionar instituição. Tente novamente.'
  }
}

function handleLogout(): void {
  logout()
  router.push({ name: 'login' })
}

function getTypeLabel(type: InstitutionType): string {
  const labels: Record<InstitutionType, string> = {
    [InstitutionType.FEDERAL]: 'Federal',
    [InstitutionType.ESTADUAL]: 'Estadual',
    [InstitutionType.MUNICIPAL]: 'Municipal',
    [InstitutionType.PRIVADA]: 'Privada',
  }
  return labels[type] || type
}

function getTypeColor(type: InstitutionType): string {
  const colors: Record<InstitutionType, string> = {
    [InstitutionType.FEDERAL]: 'blue',
    [InstitutionType.ESTADUAL]: 'green',
    [InstitutionType.MUNICIPAL]: 'orange',
    [InstitutionType.PRIVADA]: 'purple',
  }
  return colors[type] || 'grey'
}
</script>

<style scoped>
.institution-selection-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
