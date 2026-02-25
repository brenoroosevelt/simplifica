<template>
  <div class="training-list">
    <!-- Filters Section -->
    <v-card flat class="mb-4">
      <v-card-text class="d-flex flex-column flex-md-row align-start align-md-center" style="gap: 16px;">
        <v-text-field
          v-model="filters.search"
          placeholder="Buscar capacitações..."
          prepend-inner-icon="mdi-magnify"
          hide-details
          clearable
          variant="outlined"
          density="compact"
          class="flex-grow-1"
          @update:model-value="debouncedSearch"
        />

        <v-switch
          v-model="filters.active"
          :label="filters.active ? 'Apenas ativas' : 'Apenas inativas'"
          hide-details
          color="primary"
          density="compact"
          :true-value="true"
          :false-value="false"
          @update:model-value="handleActiveFilterChange"
        />
      </v-card-text>
    </v-card>

    <div class="px-6">

    <!-- Loading State -->
    <div v-if="loading" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- Empty State -->
    <v-card v-else-if="trainings.length === 0" flat>
      <v-card-text class="text-center pa-8">
        <v-icon size="64" color="grey-lighten-1" class="mb-4">
          mdi-school-outline
        </v-icon>
        <h3 class="text-h6 mb-2">Nenhuma capacitação encontrada</h3>
        <p class="text-medium-emphasis">
          {{ filters.search ? 'Tente ajustar os filtros de busca' : 'Crie sua primeira capacitação' }}
        </p>
      </v-card-text>
    </v-card>

    <!-- Training Cards Grid -->
    <v-row v-else>
      <v-col
        v-for="training in trainings"
        :key="training.id"
        cols="12"
        md="6"
        lg="4"
      >
        <v-card
          class="training-card"
          :class="{ 'training-card--inactive': !training.active }"
          variant="flat"
          border
          hover
          @click="handleCardClick(training)"
        >
          <div class="training-card__cover">
            <v-img
              v-if="training.coverImageUrl"
              :src="training.coverImageThumbnailUrl || training.coverImageUrl"
              height="200"
              cover
              class="training-card__img"
            />
            <div v-else class="training-card__placeholder">
              <v-icon size="64" color="white" style="opacity: 0.4">mdi-school-outline</v-icon>
            </div>
            <div class="training-card__title-overlay">
              <span class="text-body-1 font-weight-medium text-white">{{ training.title }}</span>
            </div>
          </div>

          <v-card-text>
            <p
              v-if="training.description"
              class="text-body-2 text-medium-emphasis mb-3"
            >
              {{ truncateText(training.description, 120) }}
            </p>

            <div class="d-flex align-center justify-space-between gap-2 flex-wrap">
              <div class="d-flex align-center gap-2">
                <v-chip
                  v-if="training.trainingType === 'LINK'"
                  size="small"
                  prepend-icon="mdi-link"
                  color="secondary"
                  variant="tonal"
                >
                  Link externo
                </v-chip>
                <template v-else>
                  <v-chip size="small" prepend-icon="mdi-play-circle" color="primary" variant="tonal">
                    {{ training.videoCount }} vídeo{{ training.videoCount !== 1 ? 's' : '' }}
                  </v-chip>
                  <v-chip
                    v-if="training.totalDurationMinutes > 0"
                    size="small"
                    prepend-icon="mdi-clock-outline"
                    variant="tonal"
                  >
                    {{ training.totalDurationMinutes }} min
                  </v-chip>
                </template>
              </div>
              <v-chip v-if="!training.active" color="error" size="small">Inativa</v-chip>
            </div>
          </v-card-text>

          <v-divider />

          <v-card-actions class="pa-2">
            <v-spacer />
            <v-btn
              icon="mdi-pencil"
              size="small"
              variant="text"
              color="primary"
              @click.stop="emit('edit', training)"
            />
            <v-btn
              icon="mdi-delete"
              size="small"
              variant="text"
              color="error"
              @click.stop="emit('delete', training)"
            />
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Pagination -->
    <div v-if="trainings.length > 0" class="d-flex justify-center mt-6">
      <v-pagination
        :model-value="page"
        :length="totalPages"
        :total-visible="7"
        @update:model-value="$emit('page-change', $event)"
      />
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import type { Training } from '@/types/training.types'

interface Props {
  trainings: Training[]
  loading?: boolean
  page: number
  totalPages: number
  showFilters?: boolean
}

interface Emits {
  (e: 'view', training: Training): void
  (e: 'edit', training: Training): void
  (e: 'delete', training: Training): void
  (e: 'page-change', page: number): void
  (e: 'filter-change', filters: any): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showFilters: true,
})

const emit = defineEmits<Emits>()

const filters = ref({
  search: '',
  active: true as boolean | null,
})

const debouncedSearch = useDebounceFn(() => {
  emit('filter-change', filters.value)
}, 300)

const handleActiveFilterChange = () => {
  emit('filter-change', filters.value)
}

const truncateText = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const handleCardClick = (training: Training) => {
  if (training.trainingType === 'LINK' && training.externalLink) {
    window.open(training.externalLink, '_blank')
  } else {
    emit('view', training)
  }
}
</script>

<style scoped>
.training-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.training-card--inactive {
  /* Mantém aparência normal, apenas mostra badge */
}

.training-card {
  cursor: pointer;
}

.training-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}


.training-card__cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.training-card__img {
  transition: transform 0.3s ease;
}

.training-card:hover .training-card__img {
  transform: scale(1.04);
}

.training-card__placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.training-card__title-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 12px 8px;
  background: rgba(0, 0, 0, 0.70);
  line-height: 1.3;

  span {
    display: block;
    word-break: break-word;
  }
}

.gap-2 {
  gap: 8px;
}
</style>
