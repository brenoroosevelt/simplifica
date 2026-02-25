<template>
  <v-avatar :color="showImage ? undefined : 'grey-darken-3'" :size="size" v-bind="$attrs">
    <v-img
      v-if="showImage"
      :src="pictureUrl"
      :alt="resolvedName"
      cover
      @error="imgError = true"
    />
    <span v-else :class="textClass" class="text-white">{{ userInitials }}</span>
  </v-avatar>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAuth } from '@/composables/useAuth'

interface Props {
  size?: number | string
  name?: string
  pictureUrl?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 40,
  name: undefined,
  pictureUrl: undefined,
})

const { user } = useAuth()

const imgError = ref(false)

const showImage = computed(() => !!props.pictureUrl && !imgError.value)

// Reset imgError when pictureUrl changes (e.g. user updates photo)
watch(() => props.pictureUrl, () => { imgError.value = false })

const resolvedName = computed(() => props.name ?? user.value?.name ?? '')

const userInitials = computed(() => {
  const source = resolvedName.value
  if (!source) return '?'
  const names = source.split(' ')
  if (names.length >= 2) {
    return `${names[0]?.[0] || ''}${names[names.length - 1]?.[0] || ''}`.toUpperCase()
  }
  return names[0]?.[0]?.toUpperCase() || '?'
})

const textClass = computed(() => {
  const s = Number(props.size)
  if (s <= 32) return 'text-caption'
  if (s <= 48) return 'text-body-2'
  if (s <= 64) return 'text-body-1'
  if (s <= 80) return 'text-h6'
  return 'text-h5'
})
</script>

<style scoped>
.v-avatar {
  -webkit-backface-visibility: hidden;
  backface-visibility: hidden;
}
</style>
