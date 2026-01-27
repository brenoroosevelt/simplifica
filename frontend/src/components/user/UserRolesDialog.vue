<template>
  <v-dialog
    v-model="isOpen"
    max-width="600"
    persistent
  >
    <v-card>
      <v-card-title class="d-flex align-center justify-space-between">
        <div>
          <div class="text-h5">Gerenciar Papéis</div>
          <div class="text-caption text-medium-emphasis">{{ userName }}</div>
        </div>
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
          <v-select
            v-model="formData.institutionId"
            label="Instituição"
            :items="institutionOptions"
            :rules="[rules.required]"
            :disabled="isGestor"
            variant="outlined"
            prepend-inner-icon="mdi-office-building"
            required
            class="mb-4"
          />

          <div class="mb-2">
            <div class="text-subtitle-2 mb-2">Papéis</div>
            <v-chip-group
              v-model="formData.roles"
              column
              multiple
              selected-class="text-primary"
            >
              <v-chip
                v-for="role in availableRoleOptions"
                :key="role.value"
                :value="role.value"
                :disabled="role.disabled"
                filter
                variant="outlined"
              >
                <template #prepend>
                  <v-icon>{{ role.icon }}</v-icon>
                </template>
                {{ role.title }}
              </v-chip>
            </v-chip-group>
            <div class="text-caption text-medium-emphasis mt-2">
              Selecione um ou mais papéis para este usuário na instituição
            </div>
            <v-alert
              v-if="isAdminRoleDisabled"
              type="info"
              variant="tonal"
              density="compact"
              class="mt-2"
            >
              O papel de Administrador só pode ser atribuído na instituição SIMP-ADMIN
            </v-alert>
          </div>

          <v-alert
            v-if="formData.roles.length === 0"
            type="warning"
            variant="tonal"
            density="compact"
            class="mt-4"
          >
            Selecione pelo menos um papel
          </v-alert>

          <v-expansion-panels class="mt-4">
            <v-expansion-panel>
              <v-expansion-panel-title>
                <v-icon start>mdi-information-outline</v-icon>
                Descrição dos papéis
              </v-expansion-panel-title>
              <v-expansion-panel-text>
                <div class="py-2">
                  <div class="mb-3">
                    <div class="font-weight-bold">Administrador</div>
                    <div class="text-caption">Acesso total à instituição, incluindo configurações e gerenciamento de usuários.</div>
                  </div>
                  <div class="mb-3">
                    <div class="font-weight-bold">Gestor</div>
                    <div class="text-caption">Pode gerenciar recursos e usuários da instituição, mas não alterar configurações.</div>
                  </div>
                  <div>
                    <div class="font-weight-bold">Visualizador</div>
                    <div class="text-caption">Acesso somente leitura aos recursos da instituição.</div>
                  </div>
                </div>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
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
          :disabled="formData.roles.length === 0"
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
import type { UserInstitutionRole } from '@/types/institution.types'
import type { UserInstitutionDetail } from '@/types/user.types'

interface Props {
  modelValue: boolean
  userName: string
  institutions: UserInstitutionDetail[]
  isGestor?: boolean
  gestorInstitutionId?: string | null
  loading?: boolean
}

interface Emits {
  (_event: 'update:modelValue', _value: boolean): void
  (_event: 'submit', _data: { institutionId: string; roles: UserInstitutionRole[] }): void
}

const props = withDefaults(defineProps<Props>(), {
  isGestor: false,
  gestorInstitutionId: null,
  loading: false,
})

const emit = defineEmits<Emits>()

const formRef = ref()
const formData = ref({
  institutionId: '',
  roles: [] as UserInstitutionRole[],
})

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const institutionOptions = computed(() => {
  return props.institutions
    .filter((ui) => ui.institution) // Filtrar apenas instituições válidas
    .map((ui) => ({
      title: ui.institution.name,
      value: ui.institutionId,
    }))
})

const roleOptions = [
  { title: 'Administrador', value: 'ADMIN', icon: 'mdi-shield-crown' },
  { title: 'Gestor', value: 'MANAGER', icon: 'mdi-shield-account' },
  { title: 'Visualizador', value: 'VIEWER', icon: 'mdi-eye' },
]

// Verifica se a instituição selecionada é a SIMP-ADMIN
const selectedInstitution = computed(() => {
  if (!formData.value.institutionId) return null
  return props.institutions.find((ui) => ui.institutionId === formData.value.institutionId)
})

const isAdminInstitution = computed(() => {
  return selectedInstitution.value?.institution?.acronym === 'SIMP-ADMIN'
})

// ADMIN role só pode ser atribuída na instituição SIMP-ADMIN
const isAdminRoleDisabled = computed(() => {
  return !isAdminInstitution.value
})

const availableRoleOptions = computed(() => {
  return roleOptions.map((role) => ({
    ...role,
    disabled: role.value === 'ADMIN' && isAdminRoleDisabled.value,
  }))
})

const rules = {
  required: (value: string) => !!value || 'Campo obrigatório',
}

watch(
  () => props.institutions,
  (institutions) => {
    if (institutions.length > 0) {
      // Se é gestor, já selecionar sua instituição
      if (props.isGestor && props.gestorInstitutionId) {
        formData.value.institutionId = props.gestorInstitutionId
        const userInst = institutions.find((ui) => ui.institutionId === props.gestorInstitutionId)
        if (userInst) {
          formData.value.roles = [...userInst.roles]
        }
      } else if (!formData.value.institutionId && institutions[0]) {
        // Selecionar primeira instituição
        formData.value.institutionId = institutions[0].institutionId
        formData.value.roles = [...institutions[0].roles]
      }
    }
  },
  { immediate: true }
)

watch(
  () => formData.value.institutionId,
  (institutionId) => {
    if (institutionId) {
      const userInst = props.institutions.find((ui) => ui.institutionId === institutionId)
      if (userInst) {
        formData.value.roles = [...userInst.roles]
      } else {
        formData.value.roles = []
      }

      // Se mudou para uma instituição que não é SIMP-ADMIN, remover role ADMIN se presente
      const institution = props.institutions.find((ui) => ui.institutionId === institutionId)
      if (institution?.institution?.acronym !== 'SIMP-ADMIN') {
        formData.value.roles = formData.value.roles.filter((role) => role !== 'ADMIN')
      }
    }
  }
)

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid || formData.value.roles.length === 0) return

  emit('submit', { ...formData.value })
}

function handleClose(): void {
  formRef.value?.reset()
  emit('update:modelValue', false)
}
</script>
