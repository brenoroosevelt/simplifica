<template>
  <div>
    <PendingUserAlert />
    <component :is="layoutComponent">
      <router-view />
    </component>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PublicLayout from '@/layouts/PublicLayout.vue'
import PrivateLayout from '@/layouts/PrivateLayout.vue'
import PendingUserAlert from '@/components/user/PendingUserAlert.vue'

const route = useRoute()

const layoutComponent = computed(() => {
  const layout = route.meta.layout as string | undefined

  switch (layout) {
    case 'public':
      return PublicLayout
    case 'private':
      return PrivateLayout
    case 'none':
      return 'div'
    default:
      return 'div'
  }
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow-x: hidden;
}

#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100%;
}

/* Improve text rendering for Inter font */
body {
  font-feature-settings: 'cv02', 'cv03', 'cv04', 'cv11';
}
</style>
