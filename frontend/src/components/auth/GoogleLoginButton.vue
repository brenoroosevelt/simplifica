<template>
  <div>
    <v-btn
      color="white"
      size="large"
      block
      elevation="2"
      :loading="loading"
      :disabled="loading"
      @click="handleLogin"
    >
      <template v-slot:prepend>
        <v-img
          src="https://www.google.com/favicon.ico"
          width="20"
          height="20"
          class="mr-2"
        />
      </template>
      Continuar com Google
    </v-btn>

    <v-alert
      v-if="error"
      type="error"
      variant="tonal"
      closable
      class="mt-3"
      @click:close="error = null"
    >
      {{ error }}
    </v-alert>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { OAuthProvider } from '@/types/auth.types'

const { login } = useAuth()
const loading = ref(false)
const error = ref<string | null>(null)

const handleLogin = async () => {
  loading.value = true
  error.value = null
  try {
    await login(OAuthProvider.GOOGLE)
  } catch (err) {
    console.error('Google login error:', err)
    error.value = err instanceof Error ? err.message : 'Erro ao realizar login com Google'
    loading.value = false
  }
}
</script>
