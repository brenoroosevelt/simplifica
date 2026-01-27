<template>
  <v-alert
    v-if="show"
    type="warning"
    variant="tonal"
    border="start"
    density="compact"
    class="ma-0 rounded-0"
    closable
    :model-value="true"
    @update:model-value="handleClose"
  >
    <div class="d-flex align-center">
      <v-icon start>mdi-clock-alert-outline</v-icon>
      <div class="flex-grow-1">
        <strong>Conta Pendente de Aprovação</strong>
        <p class="text-body-2 mb-0 mt-1">
          Sua conta está aguardando aprovação. Entre em contato com o administrador para ser
          vinculado a uma instituição.
        </p>
      </div>
    </div>
  </v-alert>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()

// Controla se o alerta foi fechado temporariamente (apenas na sessão)
const dismissed = ref(false)

const show = computed(() => {
  return authStore.isPending && !dismissed.value
})

const handleClose = (value: boolean) => {
  // Se o usuário fechar o alerta, ocultar apenas nesta sessão
  if (!value) {
    dismissed.value = true
  }
}
</script>
