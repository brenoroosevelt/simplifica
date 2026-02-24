<template>
  <div class="sidebar-container">
    <v-list density="compact" nav class="sidebar-menu">
      <v-list-item
        v-for="item in menuItems"
        :key="item.title"
        :to="item.to"
        :prepend-icon="item.icon"
        :title="item.title"
        color="primary"
        rounded="lg"
        class="mb-1"
      />

      <template v-if="canManageUsers || isAdmin">
        <v-divider class="my-4" />

        <v-list-subheader class="text-caption text-uppercase font-weight-bold">
          {{ isAdmin ? 'Administração' : 'Gestão' }}
        </v-list-subheader>

        <v-list-item
          v-for="item in adminItems"
          :key="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          color="primary"
          rounded="lg"
          class="mb-1"
        />
      </template>
    </v-list>

    <v-spacer />

    <div class="sidebar-footer">
      <span class="text-caption text-medium-emphasis">v1.0.0</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { isAdmin, canManageUsers } = useAuth()

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
  const items = []

  // Usuários: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: 'Usuários',
      icon: 'mdi-account-group',
      to: '/admin/users',
    })
  }

  // Instituição/Instituições: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: isAdmin.value ? 'Instituições' : 'Instituição',
      icon: 'mdi-office-building',
      to: '/admin/institutions',
    })
  }

  // Cadeias de Valor: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: 'Cadeias de Valor',
      icon: 'mdi-chart-timeline-variant',
      to: '/value-chains',
    })
  }

  // Unidades: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: 'Unidades',
      icon: 'mdi-office-building-outline',
      to: '/units',
    })
  }

  // Processos: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: 'Processos',
      icon: 'mdi-file-tree',
      to: '/processes',
    })
  }

  // Capacitações: disponível para ADMIN e MANAGER
  if (canManageUsers.value) {
    items.push({
      title: 'Capacitações',
      icon: 'mdi-school',
      to: '/trainings',
    })
  }

  // Configurações: apenas para ADMIN
  if (isAdmin.value) {
    items.push({
      title: 'Configurações',
      icon: 'mdi-cog',
      to: '/admin/settings',
    })
  }

  return items
})
</script>

<style scoped lang="scss">
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  //background: white;
}

.sidebar-menu {
  padding: 16px 12px;
  flex: 1;
}

.sidebar-footer {
  padding: 16px;
  text-align: center;
  border-top: 1px solid #e2e8f0;
}

// Ajustar estilos dos itens de menu
:deep(.v-list-item) {
  margin-bottom: 4px;

  &:hover {
    background: rgba(var(--v-theme-primary), 0.04);
  }

  &.v-list-item--active {
    background: rgba(var(--v-theme-primary), 0.08);
    color: rgb(var(--v-theme-primary));
    font-weight: 600;
  }
}

:deep(.v-list-subheader) {
  padding: 8px 16px;
  color: #64748b;
  font-size: 11px;
  letter-spacing: 0.5px;
}
</style>
