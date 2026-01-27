<template>
  <div>
    <PageHeader title="Meu Perfil" subtitle="Gerencie suas informações pessoais" />

    <v-row>
      <!-- Seção: Dados Pessoais -->
      <v-col cols="12" md="4">
        <v-card variant="flat" border>
          <v-card-title class="d-flex align-center pa-4">
            <v-icon start size="20">mdi-account-circle</v-icon>
            <span class="text-h6 font-weight-medium">Dados Pessoais</span>
          </v-card-title>

          <v-divider />

          <v-card-text class="text-center pa-6">
            <v-avatar v-if="user?.pictureUrl" size="100" class="mb-4">
              <v-img :src="user.pictureUrl" :alt="user.name" />
            </v-avatar>
            <v-avatar v-else color="primary" size="100" class="mb-4">
              <span class="text-h5 text-white">{{ userInitials }}</span>
            </v-avatar>

            <h2 class="text-h6 font-weight-medium mb-1">
              {{ user?.name }}
            </h2>
            <p class="text-body-2 text-grey-darken-1 mb-3">
              {{ user?.email }}
            </p>

            <div class="d-flex flex-column gap-2 align-center">
              <v-chip :color="statusColor" variant="tonal" size="small">
                {{ statusText }}
              </v-chip>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Seção: Minhas Instituições -->
      <v-col cols="12" md="4">
        <v-card variant="flat" border>
          <v-card-title class="d-flex align-center pa-4">
            <v-icon start size="20">mdi-office-building</v-icon>
            <span class="text-h6 font-weight-medium">Minhas Instituições</span>
          </v-card-title>

          <v-divider />

          <v-card-text v-if="loading" class="pa-4 text-center">
            <v-progress-circular indeterminate color="primary" size="32" />
            <p class="text-caption text-grey-darken-1 mt-2">Carregando...</p>
          </v-card-text>

          <v-card-text v-else-if="userInstitutions.length === 0" class="pa-6 text-center">
            <v-icon size="48" color="grey-lighten-1" class="mb-2">
              mdi-office-building-outline
            </v-icon>
            <p class="text-body-2 text-grey-darken-1">
              Você ainda não está vinculado a nenhuma instituição.
            </p>
            <p class="text-caption text-grey-darken-1 mt-1">
              Aguarde o administrador vincular sua conta.
            </p>
          </v-card-text>

          <v-card-text v-else class="pa-4">
            <InstitutionCard
              v-for="userInstitution in userInstitutions"
              :key="userInstitution.id"
              :institution="userInstitution.institution"
              :roles="userInstitution.roles"
              :is-active="userInstitution.institution.id === activeInstitutionId"
              :clickable="userInstitution.institution.id !== activeInstitutionId"
              class="mb-3"
              @click="handleSelectInstitution"
            />

            <v-alert
              v-if="userInstitutions.length > 1"
              type="info"
              variant="tonal"
              density="compact"
            >
              Clique em uma instituição para trocar
            </v-alert>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Seção: Segurança -->
      <v-col cols="12" md="4">
        <v-card variant="flat" border>
          <v-card-title class="d-flex align-center pa-4">
            <v-icon start size="20">mdi-shield-check</v-icon>
            <span class="text-h6 font-weight-medium">Segurança</span>
          </v-card-title>

          <v-divider />

          <v-card-text class="pa-4">
            <div class="mb-3">
              <p class="text-caption text-grey-darken-1 mb-1">Provider de Autenticação</p>
              <div class="mt-1">
                <v-chip :color="providerColor" variant="tonal" size="small">
                  <v-icon start size="16">{{ providerIcon }}</v-icon>
                  {{ user?.provider }}
                </v-chip>
              </div>
            </div>

            <div class="mb-3">
              <p class="text-caption text-grey-darken-1 mb-1">Data de Cadastro</p>
              <p class="text-body-2">{{ formattedCreatedAt }}</p>
            </div>

            <div>
              <p class="text-caption text-grey-darken-1 mb-1">Última Atualização</p>
              <p class="text-body-2">{{ formattedUpdatedAt }}</p>
            </div>
          </v-card-text>

          <v-divider />

          <v-card-text class="pa-4">
            <v-alert
              type="info"
              variant="tonal"
              density="compact"
              border="start"
              class="text-caption"
            >
              <strong>Autenticação OAuth</strong>
              <p class="mb-0 mt-1">
                Sua conta utiliza autenticação via {{ user?.provider }}. Seus dados são gerenciados
                pelo provedor.
              </p>
            </v-alert>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Snackbar for feedback -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="3000"
      location="top right"
    >
      {{ snackbar.message }}
      <template #actions>
        <v-btn
          variant="text"
          @click="snackbar.show = false"
        >
          Fechar
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import { useAuth } from '@/composables/useAuth'
import { useAuthStore } from '@/stores/auth.store'
import { useInstitutionStore } from '@/stores/institution.store'

const { user } = useAuth()
const authStore = useAuthStore()
const institutionStore = useInstitutionStore()

const loading = ref(false)
const switching = ref(false)

const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

const userInstitutions = computed(() => authStore.institutions)
const activeInstitutionId = computed(() => institutionStore.activeInstitutionId)

const userInitials = computed(() => {
  if (!user.value?.name) return '?'

  const names = user.value.name.split(' ')
  if (names.length >= 2) {
    return `${names[0]?.[0] || ''}${names[names.length - 1]?.[0] || ''}`.toUpperCase()
  }
  return names[0]?.[0]?.toUpperCase() || '?'
})

const statusColor = computed(() => {
  switch (user.value?.status) {
    case 'ACTIVE':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'INACTIVE':
      return 'error'
    default:
      return 'grey'
  }
})

const statusText = computed(() => {
  switch (user.value?.status) {
    case 'ACTIVE':
      return 'Ativo'
    case 'PENDING':
      return 'Pendente'
    case 'INACTIVE':
      return 'Inativo'
    default:
      return 'Desconhecido'
  }
})

const providerColor = computed(() => {
  switch (user.value?.provider) {
    case 'GOOGLE':
      return 'red'
    case 'MICROSOFT':
      return 'blue'
    default:
      return 'grey'
  }
})

const providerIcon = computed(() => {
  switch (user.value?.provider) {
    case 'GOOGLE':
      return 'mdi-google'
    case 'MICROSOFT':
      return 'mdi-microsoft'
    default:
      return 'mdi-shield-account'
  }
})

const formattedCreatedAt = computed(() => {
  if (!user.value?.createdAt) return '-'
  return new Date(user.value.createdAt).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  })
})

const formattedUpdatedAt = computed(() => {
  if (!user.value?.updatedAt) return '-'
  return new Date(user.value.updatedAt).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  })
})

function showSnackbar(message: string, color: string): void {
  snackbar.value = {
    show: true,
    message,
    color,
  }
}

async function handleSelectInstitution(institutionId: string): Promise<void> {
  if (institutionId === activeInstitutionId.value || switching.value) return

  switching.value = true
  try {
    await institutionStore.selectInstitution(institutionId)
    window.location.reload()
  } catch (error) {
    console.error('Failed to switch institution:', error)
    showSnackbar('Erro ao trocar instituição', 'error')
    switching.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await authStore.fetchUserInstitutions()
  } catch (err) {
    console.error('Failed to load user institutions:', err)
    showSnackbar('Erro ao carregar instituições vinculadas', 'error')
  } finally {
    loading.value = false
  }
})
</script>
