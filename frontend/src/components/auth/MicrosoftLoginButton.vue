<template>
  <div>
    <v-btn
      color="white"
      size="large"
      block
      variant="outlined"
      :loading="loading"
      :disabled="loading"
      class="oauth-button"
      @click="handleLogin"
    >
      <template v-slot:prepend>
        <v-img
          src="https://www.microsoft.com/favicon.ico"
          width="20"
          height="20"
          style="flex-shrink: 0;"
        />
      </template>
      <span class="oauth-button-text">Continuar com Microsoft</span>
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
    await login(OAuthProvider.MICROSOFT)
  } catch (err) {
    console.error('Microsoft login error:', err)
    error.value = err instanceof Error ? err.message : 'Erro ao realizar login com Microsoft'
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.oauth-button {
  height: 48px !important;
  border: 1.5px solid #e2e8f0 !important;
  border-radius: 12px !important;
  text-transform: none !important;
  font-weight: 500 !important;
  letter-spacing: 0 !important;
  transition: all 0.2s ease !important;
  box-shadow: none !important;

  &:hover {
    border-color: #cbd5e1 !important;
    background: #f8fafc !important;
    box-shadow: none !important;
  }

  &:active {
    transform: scale(0.98);
  }
}

.oauth-button-text {
  font-size: 15px;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.v-btn__prepend) {
  margin-inline-end: 12px !important;
}
</style>
