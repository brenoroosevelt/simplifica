<template>
  <div class="advanced-filters">
    <!-- Toggle Button -->
    <v-btn
      v-if="!iconOnly"
      :variant="isExpanded ? 'tonal' : 'text'"
      :color="isExpanded ? 'primary' : 'default'"
      class="advanced-filters-toggle"
      @click="toggle"
    >
      <v-icon start>
        {{ isExpanded ? 'mdi-chevron-up' : 'mdi-filter-variant' }}
      </v-icon>
      Filtros Avançados
      <v-badge
        v-if="activeCount > 0"
        :content="activeCount"
        color="primary"
        inline
        class="ml-2"
      />
    </v-btn>

    <!-- Icon Only Button -->
    <v-btn
      v-else
      :icon="isExpanded ? 'mdi-chevron-up' : 'mdi-filter-variant'"
      variant="tonal"
      :color="isExpanded ? 'primary' : 'default'"
      size="default"
      @click="toggle"
    >
      <v-icon>{{ isExpanded ? 'mdi-chevron-up' : 'mdi-filter-variant' }}</v-icon>
      <v-badge
        v-if="activeCount > 0"
        :content="activeCount"
        color="error"
        floating
      />
    </v-btn>

    <!-- Expandable Content -->
    <v-expand-transition>
      <div v-show="isExpanded" class="advanced-filters-content">
        <v-divider class="mb-4" />

        <!-- Slot for custom filter content -->
        <slot />

        <!-- Actions Row -->
        <div class="d-flex justify-end mt-4" style="gap: 12px;">
          <v-btn
            variant="text"
            color="default"
            prepend-icon="mdi-close"
            :disabled="activeCount === 0"
            @click="handleClear"
          >
            Limpar Filtros
          </v-btn>
        </div>
      </div>
    </v-expand-transition>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  activeCount?: number
  initialExpanded?: boolean
  persistKey?: string // Key para localStorage
  iconOnly?: boolean // Mostrar apenas ícone sem texto
}

interface Emits {
  (_event: 'clear'): void
  (_event: 'update:expanded', _value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  activeCount: 0,
  initialExpanded: false,
  persistKey: undefined,
  iconOnly: false,
})

const emit = defineEmits<Emits>()

// Load initial state from localStorage if persistKey is provided
const loadInitialState = (): boolean => {
  if (props.persistKey) {
    const stored = localStorage.getItem(`advanced-filters-${props.persistKey}`)
    if (stored !== null) {
      return stored === 'true'
    }
  }
  return props.initialExpanded
}

const isExpanded = ref<boolean>(loadInitialState())

// Toggle expansion
const toggle = () => {
  isExpanded.value = !isExpanded.value
}

// Handle clear filters
const handleClear = () => {
  emit('clear')
}

// Watch isExpanded to persist state and emit event
watch(isExpanded, (newValue) => {
  if (props.persistKey) {
    localStorage.setItem(`advanced-filters-${props.persistKey}`, String(newValue))
  }
  emit('update:expanded', newValue)
})
</script>

<style scoped>
.advanced-filters {
  width: 100%;
}

.advanced-filters-toggle {
  margin-top: 8px;
  margin-bottom: 8px;
}

.advanced-filters-content {
  padding: 12px;
  background-color: rgba(var(--v-theme-surface-variant), 0.3);
  border-radius: 8px;
  margin-top: 8px;
}
</style>
