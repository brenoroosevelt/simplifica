<template>
  <v-app-bar elevation="1" color="white">
    <v-app-bar-nav-icon @click="toggleDrawer" />

    <v-toolbar-title class="font-weight-bold text-primary">
      {{ appName }}
    </v-toolbar-title>

    <v-spacer />

    <v-btn icon variant="text" disabled>
      <v-icon>mdi-bell-outline</v-icon>
    </v-btn>

    <UserProfile />
  </v-app-bar>
</template>

<script setup lang="ts">
import UserProfile from './UserProfile.vue'
import { useDrawer } from '@/composables/useDrawer'

const { toggleDrawer } = useDrawer()
const appName = import.meta.env.VITE_APP_NAME || 'Simplifica'
</script>
