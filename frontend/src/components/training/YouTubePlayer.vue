<template>
  <div class="youtube-player">
    <div v-if="videoId" class="player-wrapper">
      <iframe
        :src="`https://www.youtube.com/embed/${videoId}`"
        :title="title"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
        class="player-iframe"
      ></iframe>
    </div>
    <v-alert v-else type="error" variant="tonal" class="mt-2">
      Vídeo inválido ou ID não encontrado
    </v-alert>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  videoId: string
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: 'YouTube Video',
})

const videoId = computed(() => props.videoId)
</script>

<style scoped>
.youtube-player {
  width: 100%;
}

.player-wrapper {
  position: relative;
  padding-bottom: 56.25%; /* 16:9 aspect ratio */
  height: 0;
  overflow: hidden;
  border-radius: 8px;
  background-color: #000;
}

.player-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 8px;
}
</style>
