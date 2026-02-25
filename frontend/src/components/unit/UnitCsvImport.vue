<template>
  <div class="unit-csv-import">
    <v-btn
      color="secondary"
      variant="outlined"
      prepend-icon="mdi-upload"
      @click="openDialog"
    >
      Importar CSV
    </v-btn>

    <v-dialog
      v-model="dialog"
      max-width="800"
      persistent
      scrollable
    >
      <v-card>
        <v-card-title class="text-h6 font-weight-medium pa-5">
          Importar Unidades via CSV
        </v-card-title>

        <v-card-text class="pa-6">
          <!-- Informações sobre o formato -->
          <v-alert
            type="info"
            variant="tonal"
            class="mb-4"
            border="start"
          >
            <div class="text-subtitle-2 font-weight-medium mb-2">
              Formato esperado do CSV:
            </div>
            <div class="text-body-2">
              <strong>Colunas obrigatórias:</strong> nome, sigla
              <br>
              <strong>Colunas opcionais:</strong> unidadeSuperior, descricao, status{{ isAdmin ? ', instituicaoId, instituicaoSigla' : '' }}
              <br>
              <strong>Valores aceitos em status:</strong> 1, sim, true (ativo) · 0, não, false (inativo)
            </div>
          </v-alert>

          <!-- Botão para baixar template -->
          <div class="mb-4">
            <v-btn
              color="primary"
              variant="outlined"
              prepend-icon="mdi-download"
              @click="downloadTemplate"
            >
              Baixar Template CSV
            </v-btn>
          </div>

          <!-- File input -->
          <v-file-input
            v-model="selectedFile"
            label="Selecione o arquivo CSV"
            accept=".csv"
            prepend-icon="mdi-file-delimited"
            :show-size="1000"
            clearable
            :disabled="uploading"
            @update:model-value="(file) => handleFileSelection(file as File | null)"
          />

          <!-- Preview do CSV -->
          <div v-if="preview.lines.length > 0" class="mt-4">
            <v-card variant="outlined">
              <v-card-title class="text-subtitle-2 font-weight-medium pa-3">
                Preview ({{ preview.totalLines }} linhas detectadas)
              </v-card-title>
              <v-card-text class="pa-0">
                <v-table density="compact">
                  <thead>
                    <tr>
                      <th class="text-left">#</th>
                      <th
                        v-for="header in preview.headers"
                        :key="header"
                        class="text-left"
                      >
                        {{ header }}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(line, index) in preview.lines"
                      :key="index"
                    >
                      <td class="text-caption">{{ index + 1 }}</td>
                      <td
                        v-for="(cell, cellIndex) in line"
                        :key="cellIndex"
                        class="text-body-2"
                      >
                        {{ cell }}
                      </td>
                    </tr>
                  </tbody>
                </v-table>
              </v-card-text>
            </v-card>
          </div>

          <!-- Progress bar durante upload -->
          <div v-if="uploading" class="mt-4">
            <v-progress-linear
              indeterminate
              color="primary"
            />
            <p class="text-center text-body-2 mt-2">
              Processando importação...
            </p>
          </div>

          <!-- Resultado da importação -->
          <div v-if="result" class="mt-4">
            <!-- Alert de sucesso total -->
            <v-alert
              v-if="result.failedCount === 0"
              type="success"
              variant="tonal"
              border="start"
              class="mb-4"
            >
              <div class="text-subtitle-2 font-weight-medium">
                Importação concluída com sucesso!
              </div>
              <div class="text-body-2 mt-2">
                {{ result.successCount }} de {{ result.totalRows }} unidades importadas.
              </div>
            </v-alert>

            <!-- Alert de sucesso parcial -->
            <v-alert
              v-else-if="result.successCount > 0"
              type="warning"
              variant="tonal"
              border="start"
              class="mb-4"
            >
              <div class="text-subtitle-2 font-weight-medium">
                Importação concluída com avisos
              </div>
              <div class="text-body-2 mt-2">
                <strong>{{ result.successCount }}</strong> de {{ result.totalRows }} unidades importadas.
                <br>
                <strong>{{ result.failedCount }}</strong> unidades falharam.
              </div>
            </v-alert>

            <!-- Alert de falha total -->
            <v-alert
              v-else
              type="error"
              variant="tonal"
              border="start"
              class="mb-4"
            >
              <div class="text-subtitle-2 font-weight-medium">
                Falha na importação
              </div>
              <div class="text-body-2 mt-2">
                Todas as {{ result.failedCount }} linhas falharam.
              </div>
            </v-alert>

            <!-- Lista de unidades importadas com sucesso -->
            <div v-if="result.successfulUnits.length > 0" class="mb-4">
              <v-expansion-panels variant="accordion">
                <v-expansion-panel>
                  <v-expansion-panel-title>
                    <v-icon start color="success">mdi-check-circle</v-icon>
                    Unidades importadas com sucesso ({{ result.successfulUnits.length }})
                  </v-expansion-panel-title>
                  <v-expansion-panel-text>
                    <v-list density="compact">
                      <v-list-item
                        v-for="(unitName, index) in result.successfulUnits"
                        :key="index"
                      >
                        <template #prepend>
                          <v-icon size="small" color="success">mdi-check</v-icon>
                        </template>
                        <v-list-item-title class="text-body-2">
                          {{ unitName }}
                        </v-list-item-title>
                      </v-list-item>
                    </v-list>
                  </v-expansion-panel-text>
                </v-expansion-panel>
              </v-expansion-panels>
            </div>

            <!-- Lista de erros -->
            <div v-if="result.errors.length > 0">
              <v-expansion-panels variant="accordion">
                <v-expansion-panel>
                  <v-expansion-panel-title>
                    <v-icon start color="error">mdi-alert-circle</v-icon>
                    Erros encontrados ({{ result.errors.length }})
                  </v-expansion-panel-title>
                  <v-expansion-panel-text>
                    <v-list density="compact">
                      <v-list-item
                        v-for="error in result.errors"
                        :key="error.lineNumber"
                        class="mb-2"
                      >
                        <template #prepend>
                          <v-icon size="small" color="error">mdi-close</v-icon>
                        </template>
                        <v-list-item-title class="text-body-2 font-weight-medium">
                          Linha {{ error.lineNumber }}{{ error.field ? ` (${error.field})` : '' }}
                        </v-list-item-title>
                        <v-list-item-subtitle class="text-caption">
                          {{ error.errorMessage }}
                        </v-list-item-subtitle>
                      </v-list-item>
                    </v-list>

                    <v-btn
                      color="error"
                      variant="outlined"
                      prepend-icon="mdi-download"
                      block
                      class="mt-4"
                      @click="downloadErrorReport"
                    >
                      Baixar Relatório de Erros (CSV)
                    </v-btn>
                  </v-expansion-panel-text>
                </v-expansion-panel>
              </v-expansion-panels>
            </div>
          </div>
        </v-card-text>

        <v-card-actions class="pa-4 d-flex" style="gap: 12px;">
          <v-spacer />
          <v-btn
            variant="text"
            :disabled="uploading"
            @click="closeDialog"
          >
            {{ result ? 'Fechar' : 'Cancelar' }}
          </v-btn>
          <v-btn
            v-if="!result"
            color="primary"
            variant="flat"
            :disabled="!selectedFile || uploading"
            :loading="uploading"
            @click="handleImport"
          >
            Importar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { unitService } from '@/services/unit.service'
import type { UnitImportResult } from '@/types/unit.types'

interface Emits {
  (_event: 'import-completed'): void
}

const emit = defineEmits<Emits>()

// Composables
const authStore = useAuthStore()

// Computed
const isAdmin = computed(() => authStore.isAdmin)

// State
const dialog = ref(false)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const result = ref<UnitImportResult | null>(null)

const preview = ref<{
  headers: string[]
  lines: string[][]
  totalLines: number
}>({
  headers: [],
  lines: [],
  totalLines: 0,
})

// Methods
const openDialog = () => {
  dialog.value = true
  resetState()
}

const closeDialog = () => {
  dialog.value = false
  if (result.value && result.value.successCount > 0) {
    emit('import-completed')
  }
  resetState()
}

const resetState = () => {
  selectedFile.value = null
  result.value = null
  preview.value = {
    headers: [],
    lines: [],
    totalLines: 0,
  }
}

const handleFileSelection = (file: File | null) => {
  if (!file) {
    preview.value = {
      headers: [],
      lines: [],
      totalLines: 0,
    }
    return
  }

  // Parsear CSV para preview
  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target?.result as string
    if (!text) return

    const lines = text.split('\n').filter(line => line.trim())
    if (lines.length === 0) return

    // Primeira linha como headers
    const firstLine = lines[0]
    if (!firstLine) return
    const headers = firstLine.split(',').map(h => h.trim())

    // Primeiras 5 linhas de dados
    const dataLines = lines.slice(1, 6).map(line => {
      return line.split(',').map(cell => cell.trim())
    })

    preview.value = {
      headers,
      lines: dataLines,
      totalLines: lines.length - 1, // -1 para remover header
    }
  }
  reader.readAsText(file)
}

const handleImport = async () => {
  if (!selectedFile.value) return

  uploading.value = true
  result.value = null

  try {
    const importResult = await unitService.importUnitsFromCsv(selectedFile.value)
    result.value = importResult
  } catch (err) {
    console.error('Failed to import units:', err)
    // Criar resultado de erro
    result.value = {
      totalRows: 0,
      successCount: 0,
      failedCount: 1,
      errors: [{
        lineNumber: 0,
        rowData: '',
        errorMessage: (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erro ao processar arquivo',
      }],
      successfulUnits: [],
    }
  } finally {
    uploading.value = false
  }
}

const downloadTemplate = () => {
  let csvContent: string
  if (isAdmin.value) {
    csvContent = 'nome,sigla,unidadeSuperior,descricao,status,instituicaoId,instituicaoSigla\n'
    csvContent += 'Tecnologia da Informação,TI,Secretaria de Administração,Unidade responsável por TI,sim,,\n'
    csvContent += 'Recursos Humanos,RH,,Gestão de pessoas,1,,SIMP-ADMIN\n'
    csvContent += 'Financeiro,FIN,,Gestão financeira,true,uuid-opcional-aqui,'
  } else {
    csvContent = 'nome,sigla,unidadeSuperior,descricao,status\n'
    csvContent += 'Tecnologia da Informação,TI,Secretaria de Administração,Unidade responsável por TI,sim\n'
    csvContent += 'Recursos Humanos,RH,,Gestão de pessoas,1\n'
    csvContent += 'Financeiro,FIN,,Gestão financeira,true'
  }

  downloadCsv(csvContent, 'template-importacao-unidades.csv')
}

const downloadErrorReport = () => {
  if (!result.value || result.value.errors.length === 0) return

  let csvContent = 'Linha,Campo,Erro,Dados\n'
  result.value.errors.forEach(error => {
    const line = error.lineNumber.toString()
    const field = error.field || '-'
    const message = error.errorMessage.replace(/"/g, '""') // Escape quotes
    const data = error.rowData.replace(/"/g, '""') // Escape quotes
    csvContent += `${line},"${field}","${message}","${data}"\n`
  })

  downloadCsv(csvContent, 'relatorio-erros-importacao.csv')
}

const downloadCsv = (content: string, filename: string) => {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.unit-csv-import {
  /* Add custom styles if needed */
}
</style>
