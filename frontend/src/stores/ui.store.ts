import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // State
  const drawerOpen = ref(true)
  const isMobile = ref(false)

  // Getters
  const isDrawerOpen = computed(() => drawerOpen.value)

  // Actions
  function toggleDrawer() {
    drawerOpen.value = !drawerOpen.value
  }

  function setDrawerOpen(value: boolean) {
    drawerOpen.value = value
  }

  function setMobile(value: boolean) {
    isMobile.value = value
    // Em mobile, drawer começa fechado
    if (value) {
      drawerOpen.value = false
    }
  }

  return {
    // State
    drawerOpen,
    isMobile,
    // Getters
    isDrawerOpen,
    // Actions
    toggleDrawer,
    setDrawerOpen,
    setMobile,
  }
})
