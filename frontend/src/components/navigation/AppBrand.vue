<template>
  <div class="app-brand">
    <router-link :to="to" class="brand-link">
      <v-icon icon="mdi-chart-timeline-variant-shimmer" :size="iconSize" color="primary" />
      <div class="brand-text">
        <span class="brand-name">{{ appName }}</span>
        <span class="brand-subtitle">{{ subtitle }}</span>
      </div>
    </router-link>
  </div>
</template>

<script setup lang="ts">
interface Props {
  to?: string
  iconSize?: number
  subtitle?: string
}

withDefaults(defineProps<Props>(), {
  to: '/',
  iconSize: 32,
  subtitle: 'Gestão de Processos'
})

const appName = import.meta.env.VITE_APP_NAME || 'Simplifica'
</script>

<style scoped lang="scss">
.app-brand {
  .brand-link {
    display: flex;
    align-items: center;
    gap: 12px;
    text-decoration: none;
    transition: opacity 0.2s ease;

    &:hover {
      opacity: 0.8;
    }
  }

  .brand-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .brand-name {
    font-size: 22px;
    font-weight: 700;
    color: rgb(var(--v-theme-primary));
    letter-spacing: -0.5px;
    line-height: 1;
  }

  .brand-subtitle {
    font-size: 11px;
    font-weight: 500;
    color: #64748b;
    letter-spacing: 0.3px;
    text-transform: uppercase;
    line-height: 1;
  }
}
</style>
