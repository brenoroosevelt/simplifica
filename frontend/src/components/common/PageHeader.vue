<template>
  <div class="page-header mb-6">
    <div class="d-flex justify-space-between align-center">
      <div class="page-header__content">
        <h1 class="text-h5 font-weight-medium mb-1">
          {{ title }}
        </h1>
        <p v-if="subtitle || $slots.subtitle" class="text-body-2 text-grey-darken-1">
          <slot name="subtitle">{{ subtitle }}</slot>
        </p>
      </div>

      <div v-if="$slots.actions" class="page-header__actions">
        <slot name="actions" />
      </div>
    </div>

    <div v-if="$slots.tabs" class="page-header__tabs mt-4">
      <slot name="tabs" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<style scoped>
.page-header {
  /* Add custom styles if needed */
}

.page-header__content {
  flex: 1;
  min-width: 0;
}

.page-header__actions {
  flex-shrink: 0;
  margin-left: 1rem;
}
</style>
