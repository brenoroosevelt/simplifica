<template>
  <div>
    <PageHeader
      :title="`Olá, ${userName}!`"
      subtitle="Bem-vindo ao seu dashboard"
    />

    <v-alert
      v-if="isPending"
      type="warning"
      variant="outlined"
      density="compact"
      class="mb-6"
    >
      <div class="text-body-2">
        <strong>Conta Pendente de Aprovação</strong> - Sua conta está aguardando aprovação de um administrador.
      </div>
    </v-alert>

    <!-- Minhas Instituições -->
    <v-card variant="flat" border class="mb-6">
      <v-card-title class="d-flex align-center justify-space-between pa-4">
        <span class="font-weight-medium">Minhas Instituições</span>
        <v-chip
          v-if="userInstitutions.length > 1"
          size="small"
          variant="tonal"
          prepend-icon="mdi-information-outline"
        >
          Clique para trocar
        </v-chip>
      </v-card-title>
      <v-divider />
      <v-card-text class="pa-4">
        <v-row v-if="userInstitutions.length > 0">
          <v-col
            v-for="userInst in userInstitutions"
            :key="userInst.institution.id"
            cols="12"
            sm="6"
            md="4"
          >
            <InstitutionCard
              :institution="userInst.institution"
              :roles="userInst.roles"
              :is-active="userInst.institution.id === activeInstitutionId"
              :clickable="userInst.institution.id !== activeInstitutionId"
              @click="handleSelectInstitution"
            />
          </v-col>
        </v-row>
        <div v-else class="text-center py-4 text-grey-darken-1">
          <v-icon size="48" class="mb-2">mdi-office-building-outline</v-icon>
          <p class="text-body-2">Nenhuma instituição vinculada</p>
        </div>
      </v-card-text>
    </v-card>

    <!-- Stats Cards -->
    <v-row>
      <v-col v-for="card in statsCards" :key="card.title" cols="12" md="4">
        <v-card variant="flat" border hover>
          <v-card-text>
            <div class="d-flex align-center justify-space-between">
              <div>
                <p class="text-caption text-grey-darken-1 mb-1">
                  {{ card.title }}
                </p>
                <h2 class="text-h4 font-weight-bold">
                  {{ card.value }}
                </h2>
              </div>
              <v-icon :icon="card.icon" size="48" :color="card.color" />
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mt-6">
      <v-col cols="12" md="8">
        <v-card variant="flat" border>
          <v-card-title class="font-weight-medium">
            Atividades Recentes
          </v-card-title>
          <v-divider />
          <v-card-text class="pa-6">
            <div class="text-center py-8">
              <v-icon icon="mdi-information-outline" size="48" color="grey" class="mb-2" />
              <p class="text-body-1 text-grey-darken-1">
                Nenhuma atividade recente
              </p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="4">
        <v-card variant="flat" border>
          <v-card-title class="font-weight-medium">
            Ações Rápidas
          </v-card-title>
          <v-divider />
          <v-card-text class="pa-4">
            <v-list density="compact">
              <v-list-item
                v-for="action in quickActions"
                :key="action.title"
                :to="action.to"
                :prepend-icon="action.icon"
                :disabled="action.disabled"
              >
                <v-list-item-title>{{ action.title }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import { useAuth } from '@/composables/useAuth'
import { useInstitutionStore } from '@/stores/institution.store'

const { user, isPending } = useAuth()
const institutionStore = useInstitutionStore()

const userName = computed(() => {
  if (!user.value) return 'Usuário'
  const firstName = user.value.name.split(' ')[0]
  return firstName
})

const userInstitutions = computed(() => institutionStore.userInstitutions)
const activeInstitutionId = computed(() => institutionStore.activeInstitutionId)

const switching = ref(false)

async function handleSelectInstitution(institutionId: string): Promise<void> {
  if (institutionId === activeInstitutionId.value || switching.value) return

  switching.value = true
  try {
    await institutionStore.selectInstitution(institutionId)
    window.location.reload()
  } catch (error) {
    console.error('Failed to switch institution:', error)
    switching.value = false
  }
}

const statsCards = [
  {
    title: 'Processos Pendentes',
    value: '0',
    icon: 'mdi-file-clock-outline',
    color: 'warning',
  },
  {
    title: 'Riscos Identificados',
    value: '0',
    icon: 'mdi-alert-circle-outline',
    color: 'error',
  },
  {
    title: 'Processos Mapeados',
    value: '0',
    icon: 'mdi-file-check-outline',
    color: 'success',
  },
]

const quickActions = [
  {
    title: 'Meu Perfil',
    icon: 'mdi-account',
    to: '/profile',
    disabled: false,
  },
  {
    title: 'Novo Processo',
    icon: 'mdi-file-plus-outline',
    to: '/processes/new',
    disabled: true,
  },
  {
    title: 'Mapear Risco',
    icon: 'mdi-alert-plus-outline',
    to: '/risks/new',
    disabled: true,
  },
  {
    title: 'Configurações',
    icon: 'mdi-cog',
    to: '/settings',
    disabled: true,
  },
]
</script>
