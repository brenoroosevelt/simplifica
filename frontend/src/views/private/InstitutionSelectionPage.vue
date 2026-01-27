<template>
  <v-container fluid class="fill-height selection-container">
    <v-row align="center" justify="center" class="ma-0">
      <v-col cols="12" sm="10" md="8" lg="6" xl="5" class="pa-3 pa-sm-4">
        <v-card variant="outlined" class="selection-card pa-4 pa-sm-6">
          <v-card-text class="pa-0">
            <!-- Header -->
            <div class="text-center mb-6 mb-sm-8">
              <div class="icon-wrapper">
                <v-icon icon="mdi-office-building" :size="$vuetify.display.xs ? 40 : 48" color="primary" />
              </div>
              <h1 class="selection-title mt-4 mt-sm-6 mb-2">
                Selecione uma Instituição
              </h1>
              <p class="selection-subtitle">
                Você está vinculado a múltiplas instituições. Escolha uma para continuar.
              </p>
            </div>

            <!-- Loading -->
            <v-progress-linear
              v-if="isLoading"
              indeterminate
              color="primary"
              class="mb-4"
            />

            <!-- Error Alert -->
            <v-alert
              v-if="error"
              type="error"
              variant="tonal"
              density="compact"
              closable
              class="mb-4"
              @click:close="error = null"
            >
              {{ error }}
            </v-alert>

            <!-- Institution List -->
            <div v-if="!isLoading && userInstitutions.length > 0" class="institutions-list">
              <InstitutionCard
                v-for="userInst in userInstitutions"
                :key="userInst.institution.id"
                :institution="userInst.institution"
                :roles="userInst.roles"
                :show-chevron="true"
                :clickable="true"
                variant="outlined"
                class="mb-3"
                @click="handleSelect"
              />
            </div>

            <!-- Empty State -->
            <div v-if="!isLoading && userInstitutions.length === 0" class="text-center py-12">
              <v-icon icon="mdi-office-building-outline" size="64" color="grey-lighten-1" class="mb-4" />
              <p class="text-body-1 font-weight-medium mb-2">
                Nenhuma instituição vinculada
              </p>
              <p class="text-body-2 text-grey-darken-1">
                Entre em contato com um administrador para obter acesso.
              </p>
            </div>
          </v-card-text>

          <v-divider class="my-4 my-sm-6" />

          <!-- Footer -->
          <v-card-text class="pa-0">
            <v-btn
              color="error"
              variant="text"
              prepend-icon="mdi-logout"
              size="small"
              class="text-none"
              @click="handleLogout"
            >
              Sair da conta
            </v-btn>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useInstitution } from '@/composables/useInstitution'
import { useAuth } from '@/composables/useAuth'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'

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
</script>

<style scoped lang="scss">
.selection-container {
  min-height: calc(100vh - 64px);
  padding: 16px;

  @media (min-width: 600px) {
    padding: 24px;
  }
}

.selection-card {
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
  border-radius: 16px;
  background: white;
  border-color: rgba(0, 0, 0, 0.08) !important;
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(var(--v-theme-primary), 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;

  @media (min-width: 600px) {
    width: 80px;
    height: 80px;
    border-radius: 20px;
  }
}

.selection-title {
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: -0.5px;

  @media (min-width: 600px) {
    font-size: 28px;
  }
}

.selection-subtitle {
  font-size: 14px;
  color: #64748b;
  font-weight: 400;
  line-height: 1.5;

  @media (min-width: 600px) {
    font-size: 15px;
  }
}

.institutions-list {
  width: 100%;
}
</style>
