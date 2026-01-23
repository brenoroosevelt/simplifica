<template>
  <v-app>
    <v-app-bar elevation="1" color="white">
      <v-toolbar-title class="font-weight-bold text-primary">
        {{ appName }}
      </v-toolbar-title>

      <v-spacer />

      <v-btn to="/login" color="primary" variant="elevated">
        Entrar
      </v-btn>
    </v-app-bar>

    <v-main>
      <slot />
    </v-main>

    <v-footer app color="grey-lighten-4">
      <v-row justify="center" no-gutters>
        <v-col cols="12" class="text-center py-3">
          <span class="text-caption text-grey-darken-1">
            &copy; {{ currentYear }} {{ appName }}. Todos os direitos reservados.
          </span>
        </v-col>
      </v-row>
    </v-footer>
  </v-app>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const appName = import.meta.env.VITE_APP_NAME || 'Simplifica'
const currentYear = computed(() => new Date().getFullYear())
</script>
