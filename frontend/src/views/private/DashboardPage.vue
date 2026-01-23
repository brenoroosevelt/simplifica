<template>
  <div>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h3 font-weight-bold mb-2">
          Olá, {{ userName }}!
        </h1>
        <p class="text-h6 text-grey-darken-1">
          Bem-vindo ao seu dashboard
        </p>
      </v-col>
    </v-row>

    <v-row v-if="isPending" class="mt-4">
      <v-col cols="12">
        <v-alert type="warning" variant="tonal" prominent>
          <v-alert-title>Conta Pendente de Aprovação</v-alert-title>
          <div class="mt-2">
            Sua conta está aguardando aprovação de um administrador. Você terá acesso limitado
            até que sua conta seja ativada.
          </div>
        </v-alert>
      </v-col>
    </v-row>

    <v-row class="mt-6">
      <v-col v-for="card in statsCards" :key="card.title" cols="12" md="4">
        <v-card elevation="2" hover>
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
        <v-card elevation="2">
          <v-card-title class="bg-primary text-white">
            Atividades Recentes
          </v-card-title>
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
        <v-card elevation="2">
          <v-card-title class="bg-secondary text-white">
            Ações Rápidas
          </v-card-title>
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
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { user, isPending } = useAuth()

const userName = computed(() => {
  if (!user.value) return 'Usuário'
  const firstName = user.value.name.split(' ')[0]
  return firstName
})

const statsCards = [
  {
    title: 'Agentes Ativos',
    value: '0',
    icon: 'mdi-robot',
    color: 'primary',
  },
  {
    title: 'Tarefas Concluídas',
    value: '0',
    icon: 'mdi-check-circle',
    color: 'success',
  },
  {
    title: 'Em Andamento',
    value: '0',
    icon: 'mdi-clock-outline',
    color: 'warning',
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
    title: 'Novo Agente',
    icon: 'mdi-plus-circle',
    to: '/agents/new',
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
