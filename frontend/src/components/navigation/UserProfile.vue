<template>
  <v-menu offset-y>
    <template v-slot:activator="{ props }">
      <v-btn v-bind="props" icon variant="text">
        <UserAvatar :size="32" :picture-url="user?.pictureUrl" />
      </v-btn>
    </template>

    <v-card min-width="250">
      <v-card-text>
        <div class="d-flex align-center mb-2">
          <UserAvatar :size="40" :picture-url="user?.pictureUrl" class="mr-3" />

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
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import UserAvatar from '@/components/common/UserAvatar.vue'

const router = useRouter()
const { user, logout } = useAuth()

const handleLogout = () => {
  logout()
  router.push('/login')
}
</script>
