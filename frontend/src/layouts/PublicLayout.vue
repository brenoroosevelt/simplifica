<template>
  <v-app>
    <AppHeader
      variant="public"
      v-model:drawer="drawer"
      :menu-items="menuItems"
      :on-scroll-to-section="scrollToSection"
      :on-demo-click="handleDemoClick"
    />

    <!-- Mobile Drawer -->
    <v-navigation-drawer
      v-model="drawer"
      location="right"
      temporary
      width="280"
    >
      <v-list nav>
        <v-list-item
          v-for="item in menuItems"
          :key="item.title"
          :href="item.href"
          @click="scrollToSection(item.href)"
        >
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>
      </v-list>

      <v-divider class="my-4" />

      <div class="px-4">
        <v-btn
          to="/login"
          variant="outlined"
          color="primary"
          block
          class="mb-2"
        >
          Entrar
        </v-btn>
        <v-btn
          color="primary"
          variant="tonal"
          block
          @click="handleDemoClick"
        >
          Demonstração
        </v-btn>
      </div>
    </v-navigation-drawer>

    <v-main class="landing-main">
      <slot />
    </v-main>

    <LandingFooter />
  </v-app>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppHeader from '@/components/navigation/AppHeader.vue'
import LandingFooter from '@/components/landing/LandingFooter.vue'

const router = useRouter()
const route = useRoute()
const drawer = ref(false)

const menuItems = [
  { title: 'Início', href: '#inicio' },
  { title: 'Soluções', href: '#solucoes' },
  { title: 'Funcionalidades', href: '#funcionalidades' },
  { title: 'Benefícios', href: '#beneficios' },
  { title: 'Casos de Uso', href: '#casos-de-uso' },
  { title: 'Contato', href: '#contato' },
]

const scrollToSection = async (href: string) => {
  drawer.value = false
  const id = href.replace('#', '')

  // Verificar se estamos na landing page
  const isLandingPage = route.name === 'landing' || route.path === '/'

  if (!isLandingPage) {
    // Se não estamos na landing, redirecionar com hash
    await router.push(`/${href}`)
    await nextTick()
  } else {
    // Se já estamos na landing, apenas atualizar o hash na URL
    window.location.hash = href
  }

  // Aguardar um pouco para garantir que o DOM está pronto
  await nextTick()

  // Fazer scroll suave
  const element = document.getElementById(id)
  if (element) {
    const headerOffset = 80
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

const handleDemoClick = () => {
  scrollToSection('#contato')
}

onMounted(() => {
  // Verificar se há hash na URL ao montar
  if (window.location.hash) {
    nextTick(() => {
      scrollToSection(window.location.hash)
    })
  }
})
</script>

<style scoped>
.landing-main {
  padding: 0 !important;
}
</style>
