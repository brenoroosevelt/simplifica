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
        <v-card variant="flat" border hover :to="card.to" class="stat-card">
          <v-card-text>
            <div class="d-flex align-center justify-space-between">
              <div>
                <p class="text-caption text-grey-darken-1 mb-1">{{ card.title }}</p>
                <h2 class="text-h4 font-weight-bold">
                  <v-progress-circular v-if="loadingStats" indeterminate size="32" width="3" :color="card.color" />
                  <span v-else>{{ card.value }}</span>
                </h2>
              </div>
              <v-icon :icon="card.icon" size="48" :color="card.color" />
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import { useAuth } from '@/composables/useAuth'
import { useInstitutionStore } from '@/stores/institution.store'
import { processService } from '@/services/process.service'
import { trainingService } from '@/services/training.service'
import { normativeService } from '@/services/normative.service'

const { user, isPending } = useAuth()
const institutionStore = useInstitutionStore()

const userName = computed(() => {
  if (!user.value) return 'Usuário'
  return user.value.name.split(' ')[0]
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

const loadingStats = ref(true)
const processoCount = ref(0)
const capacitacaoCount = ref(0)
const normativoCount = ref(0)

const statsCards = computed(() => [
  {
    title: 'Processos',
    value: processoCount.value,
    icon: 'mdi-file-tree',
    color: 'primary',
    to: '/processes',
  },
  {
    title: 'Capacitações',
    value: capacitacaoCount.value,
    icon: 'mdi-school',
    color: 'secondary',
    to: '/trainings',
  },
  {
    title: 'Normativos',
    value: normativoCount.value,
    icon: 'mdi-file-document-multiple',
    color: 'success',
    to: '/normatives',
  },
])

onMounted(async () => {
  try {
    const [processes, trainings, normatives] = await Promise.all([
      processService.list({ page: 0, size: 1 }),
      trainingService.list({ page: 0, size: 1 }),
      normativeService.list({ page: 0, size: 1 }),
    ])
    processoCount.value = processes.totalElements
    capacitacaoCount.value = trainings.totalElements
    normativoCount.value = normatives.totalElements
  } catch (e) {
    // silently ignore count errors
  } finally {
    loadingStats.value = false
  }
})
</script>

<style scoped>
.stat-card {
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
</style>
