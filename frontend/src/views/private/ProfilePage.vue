<template>
  <div>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h3 font-weight-bold mb-2">
          Meu Perfil
        </h1>
        <p class="text-h6 text-grey-darken-1">
          Gerencie suas informações pessoais
        </p>
      </v-col>
    </v-row>

    <v-row class="mt-6">
      <v-col cols="12" md="4">
        <v-card elevation="2">
          <v-card-text class="text-center pa-6">
            <v-avatar v-if="user?.pictureUrl" size="120" class="mb-4">
              <v-img :src="user.pictureUrl" :alt="user.name" />
            </v-avatar>
            <v-avatar v-else color="primary" size="120" class="mb-4">
              <span class="text-h3 text-white">{{ userInitials }}</span>
            </v-avatar>

            <h2 class="text-h5 font-weight-bold mb-1">
              {{ user?.name }}
            </h2>
            <p class="text-body-2 text-grey-darken-1 mb-3">
              {{ user?.email }}
            </p>

            <v-chip :color="statusColor" variant="tonal" class="mb-2">
              {{ statusText }}
            </v-chip>

            <v-chip v-if="user?.role === 'ADMIN'" color="primary" variant="tonal">
              Administrador
            </v-chip>
          </v-card-text>

          <v-divider />

          <v-card-actions>
            <v-btn block variant="text" prepend-icon="mdi-pencil" disabled>
              Editar Foto
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col cols="12" md="8">
        <v-card elevation="2">
          <v-card-title class="bg-primary text-white">
            Informações da Conta
          </v-card-title>

          <v-card-text class="pa-6">
            <v-row>
              <v-col cols="12" md="6">
                <div class="mb-4">
                  <p class="text-caption text-grey-darken-1 mb-1">Nome Completo</p>
                  <p class="text-h6">{{ user?.name }}</p>
                </div>
              </v-col>

              <v-col cols="12" md="6">
                <div class="mb-4">
                  <p class="text-caption text-grey-darken-1 mb-1">Email</p>
                  <p class="text-h6">{{ user?.email }}</p>
                </div>
              </v-col>

              <v-col cols="12" md="6">
                <div class="mb-4">
                  <p class="text-caption text-grey-darken-1 mb-1">Provider de Autenticação</p>
                  <p class="text-h6">
                    <v-chip :color="providerColor" variant="tonal">
                      {{ user?.provider }}
                    </v-chip>
                  </p>
                </div>
              </v-col>

              <v-col cols="12" md="6">
                <div class="mb-4">
                  <p class="text-caption text-grey-darken-1 mb-1">Função</p>
                  <p class="text-h6">
                    {{ user?.role === 'ADMIN' ? 'Administrador' : 'Usuário' }}
                  </p>
                </div>
              </v-col>

              <v-col cols="12" md="6">
                <div class="mb-4">
                  <p class="text-caption text-grey-darken-1 mb-1">Data de Cadastro</p>
                  <p class="text-h6">{{ formattedCreatedAt }}</p>
                </div>
              </v-col>

              <v-col cols="12" md="6">
                <div class="mb-4">
                  <p class="text-caption text-grey-darken-1 mb-1">Última Atualização</p>
                  <p class="text-h6">{{ formattedUpdatedAt }}</p>
                </div>
              </v-col>
            </v-row>
          </v-card-text>

          <v-divider />

          <v-card-actions>
            <v-spacer />
            <v-btn color="primary" variant="text" prepend-icon="mdi-pencil" disabled>
              Editar Informações
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { user } = useAuth()

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

const formattedCreatedAt = computed(() => {
  if (!user.value?.createdAt) return '-'
  return new Date(user.value.createdAt).toLocaleDateString('pt-BR')
})

const formattedUpdatedAt = computed(() => {
  if (!user.value?.updatedAt) return '-'
  return new Date(user.value.updatedAt).toLocaleDateString('pt-BR')
})
</script>
