import { computed } from 'vue'
import { useUIStore } from '@/stores/ui.store'

export function useDrawer() {
  const uiStore = useUIStore()

  const drawerOpen = computed(() => uiStore.drawerOpen)
  const isMobile = computed(() => uiStore.isMobile)

  const toggleDrawer = () => {
    uiStore.toggleDrawer()
  }

  const setDrawerOpen = (value: boolean) => {
    uiStore.setDrawerOpen(value)
  }

  return {
    drawerOpen,
    isMobile,
    toggleDrawer,
    setDrawerOpen,
  }
}
