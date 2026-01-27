<template>
  <v-dialog
    v-model="isOpen"
    max-width="600"
    persistent
  >
    <v-card>
      <v-card-title class="d-flex align-center justify-space-between">
        <span class="text-h5">Editar Usuário</span>
        <v-btn
          icon="mdi-close"
          variant="text"
          size="small"
          @click="handleClose"
        />
      </v-card-title>

      <v-divider />

      <v-card-text class="pt-4">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-text-field
            v-model="formData.name"
            label="Nome"
            :rules="[rules.required]"
            variant="outlined"
            prepend-inner-icon="mdi-account"
            required
            class="mb-3"
          />

          <v-select
            v-model="formData.status"
            label="Status"
            :items="statusOptions"
            :rules="[rules.required]"
            :disabled="isPending"
            variant="outlined"
            prepend-inner-icon="mdi-check-circle"
            required
            :hint="isPending ? 'Status PENDENTE não pode ser alterado. Vincule o usuário a uma instituição primeiro.' : 'PENDENTE é gerenciado automaticamente (usuário sem vínculos)'"
            persistent-hint
          />

          <v-alert
            v-if="isPending"
            type="warning"
            variant="tonal"
            density="compact"
            class="mt-3"
          >
            Usuário está PENDENTE (sem vínculos). Vincule-o a uma instituição para ativá-lo.
          </v-alert>

          <v-alert
            v-if="formData.status === 'INACTIVE'"
            type="error"
            variant="tonal"
            density="compact"
            class="mt-3"
          >
            Usuários inativos não podem fazer login no sistema.
          </v-alert>

          <v-alert
            v-if="formData.status === 'ACTIVE'"
            type="info"
            variant="tonal"
            density="compact"
            class="mt-3"
          >
            Usuário precisa estar vinculado a pelo menos uma instituição para acessar o sistema.
          </v-alert>
        </v-form>
      </v-card-text>

      <v-divider />

      <v-card-actions class="px-4 py-3">
        <v-spacer />
        <v-btn
          variant="text"
          @click="handleClose"
        >
          Cancelar
        </v-btn>
        <v-btn
          color="primary"
          variant="flat"
          :loading="loading"
          @click="handleSubmit"
        >
          Salvar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { UserStatus } from '@/types/auth.types'

interface User {
  id: string
  name: string
  status: UserStatus
}

interface Props {
  modelValue: boolean
  user: User | null
  loading?: boolean
}

interface Emits {
  (_event: 'update:modelValue', _value: boolean): void
  (_event: 'submit', _data: { name: string; status: UserStatus }): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<Emits>()

const formRef = ref()
const formData = ref({
  name: '',
  status: 'ACTIVE' as UserStatus,
})

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

// Verifica se o usuário está com status PENDING
const isPending = computed(() => formData.value.status === 'PENDING')

// PENDING é gerenciado automaticamente pelo sistema (usuário sem vínculos)
// Apenas ACTIVE e INACTIVE podem ser definidos manualmente
const statusOptions = [
  { title: 'Ativo', value: 'ACTIVE' },
  { title: 'Inativo', value: 'INACTIVE' },
]

const rules = {
  required: (value: string) => !!value || 'Campo obrigatório',
}

watch(
  () => props.user,
  (user) => {
    if (user) {
      formData.value = {
        name: user.name,
        status: user.status,
      }
    }
  },
  { immediate: true }
)

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid) return

  emit('submit', { ...formData.value })
}

function handleClose(): void {
  formRef.value?.reset()
  emit('update:modelValue', false)
}
</script>
