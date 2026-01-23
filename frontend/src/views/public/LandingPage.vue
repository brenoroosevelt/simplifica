<template>
  <div>
    <!-- Hero Section -->
    <v-container class="py-16">
      <v-row align="center" justify="center">
        <v-col cols="12" md="8" class="text-center">
          <h1 class="text-h2 font-weight-bold mb-4">
            Bem-vindo ao {{ appName }}
          </h1>
          <p class="text-h6 text-grey-darken-1 mb-8">
            Gestão integrada de processos e riscos.
          </p>
          <v-btn
            to="/login"
            color="primary"
            size="x-large"
            elevation="4"
            prepend-icon="mdi-rocket-launch"
          >
            Começar agora
          </v-btn>
        </v-col>
      </v-row>
    </v-container>

    <!-- Features Section -->
    <v-container class="py-12">
      <v-row>
        <v-col cols="12" class="text-center mb-8">
          <h2 class="text-h3 font-weight-bold">Recursos</h2>
          <p class="text-h6 text-grey-darken-1 mt-2">
            Tudo que você precisa em um só lugar
          </p>
        </v-col>
      </v-row>

      <v-row>
        <v-col v-for="feature in features" :key="feature.title" cols="12" md="4">
          <v-card elevation="2" height="100%">
            <v-card-text class="text-center pa-6">
              <v-icon :icon="feature.icon" size="64" color="primary" class="mb-4" />
              <h3 class="text-h5 font-weight-bold mb-3">{{ feature.title }}</h3>
              <p class="text-body-1 text-grey-darken-1">{{ feature.description }}</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- CTA Section -->
    <v-container class="py-16">
      <v-row justify="center">
        <v-col cols="12" md="8" class="text-center">
          <v-card color="primary" elevation="8">
            <v-card-text class="pa-12">
              <h2 class="text-h3 font-weight-bold text-white mb-4">
                Pronto para começar?
              </h2>
              <p class="text-h6 text-white mb-6">
                Faça login agora e experimente todas as funcionalidades
              </p>
              <v-btn
                to="/login"
                color="white"
                size="x-large"
                variant="elevated"
              >
                Entrar
              </v-btn>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
const appName = import.meta.env.VITE_APP_NAME || 'Simplifica'

const features = [
  {
    icon: 'mdi-file-tree',
    title: 'Portfólio de Processos',
    description: 'Mapeie, documente e otimize todos os processos da sua organização',
  },
  {
    icon: 'mdi-shield-alert',
    title: 'Mapeamento de Riscos',
    description: 'Identifique, analise e mitigue riscos operacionais e estratégicos',
  },
  {
    icon: 'mdi-chart-timeline-variant',
    title: 'Cadeia de Valor',
    description: 'Visualize e analise a cadeia de valor completa da sua instituição',
  },
]
</script>
