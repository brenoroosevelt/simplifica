<template>
  <v-container class="fill-height login-container">
    <v-row align="center" justify="center" class="ma-0">
      <v-col cols="12" sm="10" md="6" lg="5" xl="4" class="pa-3 pa-sm-4">
        <v-card elevation="0" class="login-card pa-4 pa-sm-6">
          <v-card-text class="pa-0">
            <div class="text-center mb-6 mb-sm-8">
              <div class="logo-wrapper">
                <v-icon icon="mdi-chart-timeline-variant-shimmer" :size="$vuetify.display.xs ? 40 : 48" color="primary" />
              </div>
              <h1 class="login-title mt-4 mt-sm-6 mb-2">
                Entrar no {{ appName }}
              </h1>
              <p class="login-subtitle">
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

          <v-divider class="my-4 my-sm-6" />

          <v-card-text class="pa-0 text-center">
            <p class="login-terms">
              Ao continuar, você concorda com nossos Termos de Serviço e Política de Privacidade
            </p>
          </v-card-text>
        </v-card>

        <div class="text-center mt-3 mt-sm-4">
          <v-btn to="/" variant="text" prepend-icon="mdi-arrow-left" size="small" class="text-none">
            Voltar para página inicial
          </v-btn>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeMount } from 'vue'
import { useRoute } from 'vue-router'
import GoogleLoginButton from '@/components/auth/GoogleLoginButton.vue'
import MicrosoftLoginButton from '@/components/auth/MicrosoftLoginButton.vue'

const route = useRoute()
const appName = import.meta.env.VITE_APP_NAME || 'Simplifica'
const errorMessage = ref<string | null>(null)

// Forçar scroll antes mesmo de montar
onBeforeMount(() => {
  window.scrollTo(0, 0)
})

onMounted(() => {
  // Forçar scroll para o topo de múltiplas formas para garantir
  window.scrollTo(0, 0)
  document.documentElement.scrollTop = 0
  document.body.scrollTop = 0

  // Forçar novamente após um pequeno delay (aguarda renderização)
  requestAnimationFrame(() => {
    window.scrollTo(0, 0)
    document.documentElement.scrollTop = 0
    document.body.scrollTop = 0
  })

  // E mais uma vez após render completo
  setTimeout(() => {
    window.scrollTo(0, 0)
    document.documentElement.scrollTop = 0
  }, 100)

  // Verificar se há erro na query string
  const error = route.query.error as string | undefined
  if (error) {
    errorMessage.value = 'Erro ao autenticar. Por favor, tente novamente.'
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: calc(100vh - 72px);
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 16px;

  @media (min-width: 600px) {
    padding: 24px;
  }
}

.login-card {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: white;
}

.logo-wrapper {
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

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: -0.5px;

  @media (min-width: 600px) {
    font-size: 28px;
  }
}

.login-subtitle {
  font-size: 14px;
  color: #64748b;
  font-weight: 400;

  @media (min-width: 600px) {
    font-size: 15px;
  }
}

.login-terms {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
  padding: 0 8px;

  @media (min-width: 600px) {
    font-size: 13px;
    padding: 0;
  }
}

.oauth-buttons {
  width: 100%;
  max-width: 100%;
  margin: 0;
}
</style>
