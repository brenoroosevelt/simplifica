<template>
  <div class="process-mapping-upload">
    <!-- Upload Section -->
    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-subtitle-1 font-weight-medium">
        <v-icon class="mr-2">mdi-upload</v-icon>
        Upload de Mapeamento (Bizagi)
      </v-card-title>
      <v-card-text>
        <v-alert
          type="info"
          variant="tonal"
          density="compact"
          class="mb-4"
        >
          <div class="text-caption">
            <strong>Instruções:</strong> Exporte o processo do Bizagi e compacte toda a pasta em um arquivo <strong>.zip</strong> antes de fazer o upload.
          </div>
        </v-alert>

        <v-file-input
          v-model="selectedFile"
          label="Selecione o arquivo ZIP"
          accept=".zip,application/zip"
          prepend-icon="mdi-folder-zip"
          :show-size="1000"
          chips
          clearable
          hint="Arquivo ZIP contendo o export do Bizagi (máx. 50MB)"
          persistent-hint
          variant="outlined"
          density="compact"
          @update:model-value="handleFileChange"
        />

        <div class="d-flex justify-end mt-4">
          <v-btn
            color="primary"
            variant="flat"
            :disabled="!selectedFile || selectedFile.length === 0"
            :loading="uploading"
            @click="handleUpload"
          >
            <v-icon start>mdi-cloud-upload</v-icon>
            Enviar arquivo
          </v-btn>
        </div>
      </v-card-text>
    </v-card>

    <!-- Existing Mappings Section -->
    <v-card variant="outlined">
      <v-card-title class="text-subtitle-1 font-weight-medium">
        <v-icon class="mr-2">mdi-file-tree-outline</v-icon>
        Mapeamento Atual {{ mappings.length > 0 ? '(1)' : '' }}
      </v-card-title>
      <v-card-text>
        <v-list v-if="mappings.length > 0" lines="two">
          <v-list-item
            v-for="mapping in mappings"
            :key="mapping.id"
            class="mb-2"
          >
            <template #prepend>
              <v-avatar color="primary" variant="tonal">
                <v-icon>mdi-folder-zip</v-icon>
              </v-avatar>
            </template>

            <v-list-item-title class="font-weight-medium">
              {{ mapping.filename }}
            </v-list-item-title>

            <v-list-item-subtitle>
              <div class="d-flex flex-column" style="gap: 4px;">
                <span>{{ formatFileSize(mapping.fileSize) }}</span>
                <span class="text-caption">Enviado em {{ formatDate(mapping.uploadedAt) }}</span>
              </div>
            </v-list-item-subtitle>

            <template #append>
              <div class="d-flex align-center" style="gap: 8px;">
                <v-tooltip text="Visualizar Mapeamento" location="top">
                  <template #activator="{ props: tooltipProps }">
                    <v-btn
                      v-bind="tooltipProps"
                      icon="mdi-eye"
                      size="small"
                      variant="text"
                      color="info"
                      @click="openPreviewDialog(mapping)"
                    />
                  </template>
                </v-tooltip>

                <v-tooltip text="Substituir Mapeamento" location="top">
                  <template #activator="{ props: tooltipProps }">
                    <v-btn
                      v-bind="tooltipProps"
                      icon="mdi-delete"
                      size="small"
                      variant="text"
                      color="error"
                      @click="openDeleteDialog(mapping)"
                    />
                  </template>
                </v-tooltip>
              </div>
            </template>
          </v-list-item>
        </v-list>

        <div v-else class="text-center py-8">
          <v-icon size="64" color="grey-lighten-1" class="mb-4">
            mdi-folder-zip-outline
          </v-icon>
          <p class="text-body-1 text-medium-emphasis">
            Nenhum mapeamento carregado
          </p>
          <p class="text-caption text-medium-emphasis">
            Faça upload de um arquivo ZIP exportado do Bizagi
          </p>
        </div>
      </v-card-text>
    </v-card>

    <!-- Delete Confirmation Dialog -->
    <v-dialog
      v-model="deleteDialog"
      max-width="500"
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          Confirmar Exclusão
        </v-card-title>

        <v-card-text class="pa-6">
          <p class="text-body-1 mb-4">
            Tem certeza que deseja excluir o arquivo
            <strong>{{ selectedMapping?.filename }}</strong>?
          </p>
          <p class="text-body-2 text-grey-darken-1">
            Esta ação não pode ser desfeita.
          </p>
        </v-card-text>

        <v-card-actions class="pa-4 d-flex" style="gap: 12px;">
          <v-spacer />
          <v-btn
            variant="text"
            @click="closeDeleteDialog"
          >
            Cancelar
          </v-btn>
          <v-btn
            color="error"
            variant="flat"
            :loading="deleting"
            @click="handleDelete"
          >
            Excluir
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { ProcessMapping } from '@/types/process.types'

interface Props {
  mappings: ProcessMapping[]
  uploading?: boolean
  deleting?: boolean
}

interface Emits {
  (_event: 'upload', _file: File): void
  (_event: 'delete', _mappingId: string): void
  (_event: 'view', _mapping: ProcessMapping): void
}

withDefaults(defineProps<Props>(), {
  uploading: false,
  deleting: false,
})

const emit = defineEmits<Emits>()

// State
const selectedFile = ref<File[] | null>(null)
const deleteDialog = ref(false)
const selectedMapping = ref<ProcessMapping | null>(null)

// Methods
const handleFileChange = (files: File[] | null) => {
  // File change handled by v-model
}

const handleUpload = () => {
  if (!selectedFile.value) {
    return
  }

  let file: File | undefined

  if (Array.isArray(selectedFile.value)) {
    if (selectedFile.value.length === 0) {
      return
    }
    file = selectedFile.value[0]
  } else {
    file = selectedFile.value as unknown as File
  }

  if (!file) {
    return
  }

  emit('upload', file)
  selectedFile.value = null
}

const openPreviewDialog = (mapping: ProcessMapping) => {
  emit('view', mapping)
}

const openDeleteDialog = (mapping: ProcessMapping) => {
  selectedMapping.value = mapping
  deleteDialog.value = true
}

const closeDeleteDialog = () => {
  deleteDialog.value = false
  selectedMapping.value = null
}

const handleDelete = () => {
  if (!selectedMapping.value) return

  emit('delete', selectedMapping.value.id)
}

// Utility functions
const formatFileSize = (bytes?: number): string => {
  if (!bytes) return '-'

  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }

  return `${size.toFixed(2)} ${units[unitIndex]}`
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

// Expose methods to parent if needed
defineExpose({
  closeDeleteDialog,
})
</script>

<style scoped>
.process-mapping-upload {
  /* Add custom styles if needed */
}
</style>
