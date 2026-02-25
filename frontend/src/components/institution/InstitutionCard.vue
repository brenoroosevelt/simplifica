<template>
  <v-card
    :variant="variant"
    :class="['institution-card', { 'cursor-pointer': clickable, 'active-card': isActive }]"
    @click="handleClick"
  >
    <v-card-text class="pa-4">
      <div class="d-flex align-center" style="min-height: 48px;">
        <!-- Logo -->
        <div class="institution-card__logo mr-3">
          <v-img
            v-if="institution.logoThumbnailUrl || institution.logoUrl"
            :src="institution.logoThumbnailUrl || institution.logoUrl"
            :alt="institution.acronym"
            class="institution-card__logo-img"
            contain
          />
          <div v-else class="institution-card__logo-placeholder">
            <v-icon size="22" color="primary">mdi-office-building</v-icon>
          </div>
        </div>

        <!-- Informações -->
        <div class="flex-grow-1 institution-info">
          <div class="text-body-2 font-weight-medium">
            {{ institution.acronym }}
          </div>
          <div class="text-caption text-grey-darken-1">
            {{ institution.name }}
          </div>

          <!-- Papéis (se fornecidos) -->
          <div v-if="roles && roles.length > 0" class="d-flex flex-wrap gap-1 mt-1">
            <v-chip
              v-for="role in roles"
              :key="role"
              :color="getRoleColor(role)"
              size="x-small"
              variant="tonal"
            >
              {{ getRoleLabel(role) }}
            </v-chip>
          </div>
        </div>

        <!-- Indicadores laterais -->
        <v-icon v-if="showChevron" color="grey-darken-1" size="20">
          mdi-chevron-right
        </v-icon>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { Institution, UserInstitutionRole } from '@/types/institution.types'

interface Props {
  institution: Institution
  roles?: UserInstitutionRole[]
  isActive?: boolean
  clickable?: boolean
  showChevron?: boolean
  variant?: 'flat' | 'outlined' | 'elevated' | 'tonal' | 'text' | 'plain'
}

const props = withDefaults(defineProps<Props>(), {
  roles: undefined,
  isActive: false,
  clickable: false,
  showChevron: false,
  variant: 'outlined',
})

const emit = defineEmits<{
  click: [institutionId: string]
}>()

const handleClick = () => {
  if (props.clickable) {
    emit('click', props.institution.id)
  }
}

function getRoleColor(role: UserInstitutionRole): string {
  switch (role) {
    case 'ADMIN':
      return 'error'
    case 'MANAGER':
      return 'warning'
    case 'VIEWER':
      return 'info'
    default:
      return 'grey'
  }
}

function getRoleLabel(role: UserInstitutionRole): string {
  switch (role) {
    case 'ADMIN':
      return 'Admin'
    case 'MANAGER':
      return 'Gestor'
    case 'VIEWER':
      return 'Visualizador'
    default:
      return role
  }
}
</script>

<style scoped lang="scss">
.institution-card__logo {
  width: 56px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.institution-card__logo-img {
  width: 100%;
  height: 100%;
}

.institution-card__logo-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(var(--v-theme-primary), 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.institution-card {
  transition: all 0.2s ease;
  border-radius: 12px;

  // Borders mais suaves por padrão
  &.v-card--variant-outlined {
    border-width: 1.5px;
    border-color: rgba(0, 0, 0, 0.08) !important;
  }

  &.cursor-pointer {
    cursor: pointer;
  }

  &.active-card {
    border-width: 2px;
    border-color: rgb(var(--v-theme-primary)) !important;
  }

  &.cursor-pointer:hover {
    border-color: rgba(var(--v-theme-primary), 0.4) !important;
  }

  &.cursor-pointer:active {
    transform: scale(0.98);
  }
}

.institution-info {
  min-width: 0;
  overflow: hidden;

  > div:not(.d-flex) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.gap-1 {
  gap: 4px;
}
</style>
