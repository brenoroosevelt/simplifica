<template>
  <v-switch
    :model-value="modelValue"
    :label="modelValue ? 'Ativo' : 'Inativo'"
    :color="modelValue ? 'primary' : undefined"
    hide-details
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean
}>()

defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()
</script>
