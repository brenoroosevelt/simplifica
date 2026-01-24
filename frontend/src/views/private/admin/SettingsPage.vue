<template>
  <div class="settings-page">
    <PageHeader
      title="Configurações"
      subtitle="Configure as opções do sistema"
    />

    <v-card variant="flat" border>
      <v-card-text class="pa-6">
        <v-alert
          type="info"
          variant="tonal"
        >
          Página em desenvolvimento. Em breve você poderá gerenciar configurações do sistema aqui.
        </v-alert>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
</script>

<style scoped>
.settings-page {
  /* Add custom styles if needed */
}
</style>
