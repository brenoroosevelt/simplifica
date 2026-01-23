<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4" class="text-center">
        <v-card elevation="4" class="pa-8">
          <v-card-text>
            <template v-if="!error">
              <v-progress-circular
                indeterminate
                color="primary"
                size="64"
                class="mb-4"
              />
              <h2 class="text-h5 font-weight-bold mb-2">
                Autenticando...
              </h2>
              <p class="text-body-1 text-grey-darken-1">
                Aguarde enquanto processamos seu login
              </p>
            </template>

            <template v-else>
              <v-icon icon="mdi-alert-circle" size="64" color="error" class="mb-4" />
              <h2 class="text-h5 font-weight-bold mb-2">
                Erro na autenticação
              </h2>
              <p class="text-body-1 text-grey-darken-1 mb-4">
                {{ error }}
              </p>
              <v-btn to="/login" color="primary" size="large">
                Tentar novamente
              </v-btn>
            </template>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const error = ref<string | null>(null)

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search)
  const token = urlParams.get('token')
  const errorParam = urlParams.get('error')

  if (errorParam) {
    error.value = 'Falha na autenticação. Por favor, tente novamente.'
    return
  }

  if (!token) {
    error.value = 'Token não encontrado. Por favor, tente fazer login novamente.'
    return
  }

  try {
    await authStore.handleCallback(token)

    // Redirecionar para dashboard ou página solicitada
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || '/dashboard')
  } catch (err) {
    console.error('Callback error:', err)
    error.value = 'Erro ao processar autenticação. Por favor, tente novamente.'
  }
})
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
}
</style>
