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
            <UserAvatar :size="100" :picture-url="user?.pictureUrl" class="mb-4" />

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

      <!-- Seção: Segurança -->
      <v-col cols="12" md="8">
        <v-row>
          <v-col cols="12">
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
import { computed, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useAuth } from '@/composables/useAuth'

const { user } = useAuth()

const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
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
</script>
