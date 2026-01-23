<template>
  <v-app>
    <AppHeader />

    <v-navigation-drawer
      v-model="drawer"
      :rail="isRail && !isMobile"
      :temporary="isMobile"
      :permanent="!isMobile"
    >
      <AppSidebar />
    </v-navigation-drawer>

    <v-main>
      <v-container fluid>
        <slot />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useDisplay } from 'vuetify'
import AppHeader from '@/components/navigation/AppHeader.vue'
import AppSidebar from '@/components/navigation/AppSidebar.vue'
import { useUIStore } from '@/stores/ui.store'

const display = useDisplay()
const uiStore = useUIStore()

const drawer = computed({
  get: () => uiStore.drawerOpen,
  set: (value: boolean) => uiStore.setDrawerOpen(value),
})

const isMobile = computed(() => display.mobile.value)
const isRail = ref(false)

// Atualizar estado mobile na store
const updateMobileState = () => {
  uiStore.setMobile(isMobile.value)
}

onMounted(() => {
  updateMobileState()
  window.addEventListener('resize', updateMobileState)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateMobileState)
})
</script>
