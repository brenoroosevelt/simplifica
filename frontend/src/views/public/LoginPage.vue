<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="5" lg="4">
        <v-card elevation="8" class="pa-4">
          <v-card-text>
            <div class="text-center mb-6">
              <v-icon icon="mdi-account-circle" size="64" color="primary" />
              <h1 class="text-h4 font-weight-bold mt-4 mb-2">
                Entrar no {{ appName }}
              </h1>
              <p class="text-body-1 text-grey-darken-1">
                Escolha um método de autenticação
              </p>
            </div>

            <v-alert v-if="errorMessage" type="error" variant="tonal" class="mb-4">
              {{ errorMessage }}
            </v-alert>

            <div class="oauth-buttons">
              <GoogleLoginButton class="mb-3" />
              <MicrosoftLoginButton />
            </div>
          </v-card-text>

          <v-divider class="my-4" />

          <v-card-text class="text-center">
            <p class="text-caption text-grey-darken-1">
              Ao continuar, você concorda com nossos Termos de Serviço e Política de Privacidade
            </p>
          </v-card-text>
        </v-card>

        <div class="text-center mt-4">
          <v-btn to="/" variant="text" prepend-icon="mdi-arrow-left">
            Voltar para página inicial
          </v-btn>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import GoogleLoginButton from '@/components/auth/GoogleLoginButton.vue'
import MicrosoftLoginButton from '@/components/auth/MicrosoftLoginButton.vue'

const route = useRoute()
const appName = import.meta.env.VITE_APP_NAME || 'Simplifica'
const errorMessage = ref<string | null>(null)

onMounted(() => {
  // Verificar se há erro na query string
  const error = route.query.error as string | undefined
  if (error) {
    errorMessage.value = 'Erro ao autenticar. Por favor, tente novamente.'
  }
})
</script>

<style scoped>
.fill-height {
  min-height: calc(100vh - 64px - 52px); /* Altura total - header - footer */
}

.oauth-buttons {
  max-width: 400px;
  margin: 0 auto;
}
</style>
