<template>
  <v-app>
    <AppHeader variant="private" />

    <v-navigation-drawer
      v-model="drawer"
      :rail="isRail && !isMobile"
      :temporary="isMobile"
      :permanent="!isMobile"
      elevation="0"
      class="sidebar-drawer"
    >
      <AppSidebar />
    </v-navigation-drawer>

    <v-main class="private-main">
      <v-container fluid class="pa-6">
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

<style scoped lang="scss">
.private-main {
  //background: #f8fafc;
}

.sidebar-drawer {
  border-right: 1px solid #e2e8f0 !important;
}
</style>
