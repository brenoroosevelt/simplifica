<template>
  <v-menu offset-y>
    <template v-slot:activator="{ props }">
      <v-btn v-bind="props" icon variant="text">
        <v-avatar v-if="user?.pictureUrl" size="32">
          <v-img :src="user.pictureUrl" :alt="user.name" />
        </v-avatar>
        <v-avatar v-else color="primary" size="32">
          <span class="text-white text-caption">{{ userInitials }}</span>
        </v-avatar>
      </v-btn>
    </template>

    <v-card min-width="250">
      <v-card-text>
        <div class="d-flex align-center mb-2">
          <v-avatar v-if="user?.pictureUrl" size="40" class="mr-3">
            <v-img :src="user.pictureUrl" :alt="user.name" />
          </v-avatar>
          <v-avatar v-else color="primary" size="40" class="mr-3">
            <span class="text-white">{{ userInitials }}</span>
          </v-avatar>

          <div>
            <div class="font-weight-medium">{{ user?.name }}</div>
            <div class="text-caption text-grey">{{ user?.email }}</div>
          </div>
        </div>
      </v-card-text>

      <v-divider />

      <v-list density="compact" nav>
        <v-list-item to="/profile" prepend-icon="mdi-account" title="Meu Perfil" />
        <v-list-item prepend-icon="mdi-cog" title="Configurações" disabled />
      </v-list>

      <v-divider />

      <v-card-actions>
        <v-btn block color="error" variant="text" prepend-icon="mdi-logout" @click="handleLogout">
          Sair
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { user, logout } = useAuth()

const userInitials = computed(() => {
  if (!user.value?.name) return '?'

  const names = user.value.name.split(' ')
  if (names.length >= 2) {
    return `${names[0]?.[0] || ''}${names[names.length - 1]?.[0] || ''}`.toUpperCase()
  }
  return names[0]?.[0]?.toUpperCase() || '?'
})

const handleLogout = () => {
  logout()
  router.push('/login')
}
</script>
