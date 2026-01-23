<template>
  <div>
    <v-list density="compact" nav>
      <v-list-item
        v-for="item in menuItems"
        :key="item.title"
        :to="item.to"
        :prepend-icon="item.icon"
        :title="item.title"
        color="primary"
      />

      <template v-if="isAdmin">
        <v-divider class="my-2" />

        <v-list-subheader>Administração</v-list-subheader>

        <v-list-item
          v-for="item in adminItems"
          :key="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          color="primary"
        />
      </template>
    </v-list>

    <div class="pa-2 text-center">
      <span class="text-caption text-grey">v1.0.0</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { isAdmin } = useAuth()

const menuItems = [
  {
    title: 'Dashboard',
    icon: 'mdi-view-dashboard',
    to: '/dashboard',
  },
  {
    title: 'Perfil',
    icon: 'mdi-account',
    to: '/profile',
  },
]

const adminItems = computed(() => {
  if (!isAdmin.value) return []

  return [
    {
      title: 'Usuários',
      icon: 'mdi-account-group',
      to: '/admin/users',
    },
    {
      title: 'Configurações',
      icon: 'mdi-cog',
      to: '/admin/settings',
    },
  ]
})
</script>
