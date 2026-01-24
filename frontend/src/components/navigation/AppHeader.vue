<template>
  <v-app-bar
    elevation="0"
    color="white"
    height="72"
    class="app-header"
  >
    <!-- Botão menu (apenas no contexto privado) -->
    <v-app-bar-nav-icon
      v-if="showMenu"
      @click="toggleDrawer"
      class="ml-2"
    />

    <!-- Logo/Brand -->
    <v-toolbar-title class="brand-container">
      <AppBrand :to="brandLink" />
    </v-toolbar-title>

    <v-spacer />

    <!-- Actions do contexto privado -->
    <template v-if="showPrivateActions">
      <InstitutionSwitcher />
      <v-btn icon="mdi-bell-outline" variant="text" class="mx-1" />
      <UserProfile />
    </template>

    <!-- Actions do contexto público -->
    <template v-if="showPublicActions">
      <div class="desktop-menu d-none d-md-flex">
        <v-btn
          v-for="item in menuItems"
          :key="item.title"
          :href="item.href"
          variant="text"
          class="menu-item"
          @click.prevent="scrollToSection(item.href)"
        >
          {{ item.title }}
        </v-btn>
      </div>

      <div class="d-none d-md-flex ml-4 mr-3">
        <v-btn
          to="/login"
          variant="outlined"
          color="primary"
          class="mr-2"
        >
          Entrar
        </v-btn>
        <v-btn
          color="primary"
          variant="tonal"
          @click="handleDemoClick"
        >
          Demonstração
        </v-btn>
      </div>

      <!-- Mobile Menu Icon -->
      <v-app-bar-nav-icon
        class="d-md-none"
        @click="emit('update:drawer', !props.drawer)"
      />
    </template>
  </v-app-bar>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppBrand from './AppBrand.vue'
import UserProfile from './UserProfile.vue'
import InstitutionSwitcher from '@/components/institution/InstitutionSwitcher.vue'
import { useDrawer } from '@/composables/useDrawer'

interface Props {
  variant?: 'public' | 'private'
  drawer?: boolean
  menuItems?: Array<{ title: string; href: string }>
  onScrollToSection?: (href: string) => void
  onDemoClick?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'private',
  drawer: false,
  menuItems: () => []
})

const emit = defineEmits<{
  'update:drawer': [value: boolean]
}>()

const { toggleDrawer } = useDrawer()

const showMenu = computed(() => props.variant === 'private')
const showPrivateActions = computed(() => props.variant === 'private')
const showPublicActions = computed(() => props.variant === 'public')
const brandLink = computed(() => props.variant === 'private' ? '/dashboard' : '/')

const scrollToSection = (href: string) => {
  if (props.onScrollToSection) {
    props.onScrollToSection(href)
  }
}

const handleDemoClick = () => {
  if (props.onDemoClick) {
    props.onDemoClick()
  }
}
</script>

<style scoped lang="scss">
.app-header {
  border-bottom: 1px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.brand-container {
  flex: 0 0 auto;
}

.desktop-menu {
  gap: 8px;
}

.menu-item {
  font-weight: 500;
  font-size: 15px;
  letter-spacing: 0;
  text-transform: none;
  color: rgba(0, 0, 0, 0.75);
  transition: all 0.2s ease;

  &:hover {
    color: rgb(var(--v-theme-primary));
    background-color: rgba(var(--v-theme-primary), 0.04);
  }
}
</style>
