<template>
  <v-dialog v-model="isOpen" persistent max-width="800px">
    <v-card>
      <v-card-title class="text-h5 py-4 px-6">
        <v-icon start color="primary">mdi-office-building</v-icon>
        Selecione sua Instituição
      </v-card-title>

      <v-divider />

      <v-card-text class="pa-6">
        <v-alert
          v-if="!institutions.length"
          type="info"
          variant="tonal"
          class="mb-4"
        >
          Você não está vinculado a nenhuma instituição. Entre em contato com o administrador.
        </v-alert>

        <v-alert
          v-if="institutions.length === 1"
          type="info"
          variant="tonal"
          class="mb-4"
        >
          Você possui acesso a apenas uma instituição.
        </v-alert>

        <v-row>
          <v-col
            v-for="institution in institutions"
            :key="institution.id"
            cols="12"
            sm="6"
            md="4"
          >
            <v-card
              :disabled="loading"
              :loading="loading && selectedId === institution.id"
              hover
              class="institution-card"
              @click="handleSelect(institution.id)"
            >
              <v-card-text class="d-flex flex-column align-center text-center py-6">
                <div class="selector-logo mb-4">
                  <v-img
                    v-if="institution.logoThumbnailUrl || institution.logoUrl"
                    :src="institution.logoThumbnailUrl || institution.logoUrl"
                    :alt="institution.name"
                    class="selector-logo__img"
                    contain
                  />
                  <div v-else class="selector-logo__placeholder">
                    <v-icon size="40" color="primary">mdi-office-building</v-icon>
                  </div>
                </div>

                <h3 class="text-h6 font-weight-medium mb-2">
                  {{ institution.acronym }}
                </h3>
                <p class="text-body-2 text-medium-emphasis">
                  {{ institution.name }}
                </p>

                <v-chip
                  :color="getTypeColor(institution.type)"
                  size="small"
                  class="mt-2"
                >
                  {{ getTypeLabel(institution.type) }}
                </v-chip>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>

      <v-divider />

      <v-card-actions class="px-6 py-4">
        <v-spacer />
        <v-btn
          v-if="allowClose"
          variant="text"
          @click="$emit('close')"
        >
          Cancelar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Institution, InstitutionType } from '@/types/institution.types'

interface Props {
  institutions: Institution[]
  modelValue: boolean
  allowClose?: boolean
}

interface Emits {
  (event: 'update:modelValue', value: boolean): void
  (event: 'select', institutionId: string): void
  (event: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  allowClose: false,
})

const emit = defineEmits<Emits>()

const loading = ref(false)
const selectedId = ref<string | null>(null)

const isOpen = ref(props.modelValue)

const handleSelect = async (institutionId: string) => {
  loading.value = true
  selectedId.value = institutionId

  try {
    emit('select', institutionId)
  } finally {
    loading.value = false
    selectedId.value = null
  }
}

const getTypeLabel = (type: InstitutionType): string => {
  const labels: Record<InstitutionType, string> = {
    FEDERAL: 'Federal',
    ESTADUAL: 'Estadual',
    MUNICIPAL: 'Municipal',
    PRIVADA: 'Privada',
  }
  return labels[type] || type
}

const getTypeColor = (type: InstitutionType): string => {
  const colors: Record<InstitutionType, string> = {
    FEDERAL: 'blue',
    ESTADUAL: 'green',
    MUNICIPAL: 'orange',
    PRIVADA: 'purple',
  }
  return colors[type] || 'grey'
}
</script>

<style scoped>
.selector-logo {
  width: 96px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.selector-logo__img {
  width: 100%;
  height: 100%;
}

.selector-logo__placeholder {
  width: 100%;
  height: 100%;
  background: rgba(var(--v-theme-primary), 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.institution-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.institution-card:hover {
  border-color: rgb(var(--v-theme-primary));
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.institution-card[disabled] {
  opacity: 0.6;
  pointer-events: none;
}
</style>
