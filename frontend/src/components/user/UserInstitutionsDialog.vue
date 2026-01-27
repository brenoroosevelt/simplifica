<template>
  <v-dialog
    v-model="isOpen"
    max-width="900"
    persistent
    scrollable
  >
    <v-card>
      <v-card-title class="d-flex align-center justify-space-between">
        <div>
          <div class="text-h5">Gerenciar Instituições e Papéis</div>
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

      <v-card-text class="pt-4 px-3 px-sm-6">
        <!-- Instituições vinculadas -->
        <div class="mb-4">
          <div class="d-flex flex-column flex-sm-row align-start align-sm-center justify-space-between mb-3" style="gap: 12px;">
            <div class="text-subtitle-1 font-weight-bold">Instituições Vinculadas</div>
            <v-btn
              v-if="!showAddForm && !showEditForm"
              color="primary"
              variant="tonal"
              size="small"
              prepend-icon="mdi-plus"
              @click="showAddForm = true"
            >
              Adicionar Instituição
            </v-btn>
          </div>

          <div
            v-if="institutions.length > 0"
            class="institutions-list"
          >
            <v-card
              v-for="userInst in institutions"
              :key="userInst.institutionId"
              variant="outlined"
              class="mb-3"
            >
              <v-card-text class="pa-3 pa-sm-4">
                <div class="institution-card-content">
                  <div class="d-flex align-start mb-3" style="gap: 12px;">
                    <v-avatar
                      size="48"
                      color="primary"
                      variant="tonal"
                      class="d-none d-sm-flex"
                    >
                      <v-icon>mdi-office-building</v-icon>
                    </v-avatar>

                    <div class="flex-grow-1">
                      <div class="d-flex align-center flex-wrap" style="gap: 8px;">
                        <div class="text-subtitle-1 font-weight-medium">
                          {{ userInst.institution?.name || 'Instituição' }}
                        </div>
                        <v-chip
                          v-if="userInst.institution?.acronym"
                          size="x-small"
                          variant="tonal"
                          color="grey"
                        >
                          {{ userInst.institution.acronym }}
                        </v-chip>
                      </div>
                      <div class="text-caption text-medium-emphasis">
                        Vinculado em {{ formatDate(userInst.linkedAt) }}
                      </div>
                    </div>
                  </div>

                  <div class="mb-3">
                    <div class="text-caption text-medium-emphasis mb-1">Papéis:</div>
                    <div class="d-flex flex-wrap" style="gap: 6px;">
                      <v-chip
                        v-for="role in userInst.roles"
                        :key="role"
                        size="small"
                        variant="tonal"
                        color="primary"
                      >
                        <v-icon start size="14">{{ getRoleIcon(role) }}</v-icon>
                        {{ getRoleLabel(role) }}
                      </v-chip>
                    </div>
                  </div>

                  <div class="d-flex flex-column flex-sm-row" style="gap: 8px;">
                    <v-btn
                      size="small"
                      variant="tonal"
                      color="primary"
                      prepend-icon="mdi-pencil"
                      class="action-btn"
                      @click="openEditRoles(userInst)"
                    >
                      Editar Papéis
                    </v-btn>
                    <v-btn
                      size="small"
                      variant="tonal"
                      color="error"
                      prepend-icon="mdi-close"
                      class="action-btn"
                      :loading="loadingUnlink[userInst.institutionId]"
                      @click="handleUnlink(userInst.institutionId)"
                    >
                      Remover
                    </v-btn>
                  </div>
                </div>
              </v-card-text>
            </v-card>
          </div>

          <v-alert
            v-else
            type="info"
            variant="tonal"
            density="compact"
          >
            Nenhuma instituição vinculada
          </v-alert>
        </div>

        <!-- Formulário de adicionar instituição -->
        <v-expand-transition>
          <v-card
            v-if="showAddForm"
            variant="outlined"
            class="mt-4"
          >
            <v-card-title class="text-subtitle-1">
              Adicionar Instituição
            </v-card-title>
            <v-card-text>
              <v-form ref="formRef" @submit.prevent="handleAdd">
                <v-autocomplete
                  v-model="addForm.institutionId"
                  label="Instituição"
                  :items="availableInstitutions"
                  :rules="[rules.required]"
                  :loading="loadingInstitutions"
                  variant="outlined"
                  prepend-inner-icon="mdi-office-building"
                  required
                  class="mb-3"
                />

                <div class="mb-2">
                  <div class="text-subtitle-2 mb-2">Papéis</div>
                  <v-chip-group
                    v-model="addForm.roles"
                    column
                    multiple
                    selected-class="text-primary"
                  >
                    <v-chip
                      v-for="role in getAvailableRoles(addForm.institutionId)"
                      :key="role.value"
                      :value="role.value"
                      filter
                      variant="outlined"
                    >
                      <template #prepend>
                        <v-icon>{{ role.icon }}</v-icon>
                      </template>
                      {{ role.title }}
                    </v-chip>
                  </v-chip-group>
                </div>

                <v-alert
                  v-if="addForm.roles.length === 0"
                  type="warning"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
                >
                  Selecione pelo menos um papel
                </v-alert>

                <v-alert
                  v-if="!isAdminInstitution(addForm.institutionId)"
                  type="info"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
                >
                  O papel de Administrador só pode ser atribuído na instituição SIMP-ADMIN
                </v-alert>
              </v-form>
            </v-card-text>
            <v-card-actions class="px-4 pb-4">
              <v-spacer />
              <div class="d-flex flex-wrap" style="gap: 8px;">
                <v-btn
                  variant="text"
                  class="form-action-btn"
                  @click="cancelAdd"
                >
                  Cancelar
                </v-btn>
                <v-btn
                  color="primary"
                  variant="flat"
                  class="form-action-btn"
                  :loading="loadingAdd"
                  :disabled="!addForm.institutionId || addForm.roles.length === 0"
                  @click="handleAdd"
                >
                  Adicionar
                </v-btn>
              </div>
            </v-card-actions>
          </v-card>
        </v-expand-transition>

        <!-- Formulário de editar papéis -->
        <v-expand-transition>
          <v-card
            v-if="showEditForm"
            variant="outlined"
            class="mt-4"
          >
            <v-card-title class="text-subtitle-1">
              Editar Papéis - {{ editForm.institutionName }}
            </v-card-title>
            <v-card-text>
              <v-form ref="editFormRef" @submit.prevent="handleUpdateRoles">
                <div class="mb-2">
                  <div class="text-subtitle-2 mb-2">Papéis</div>
                  <v-chip-group
                    v-model="editForm.roles"
                    column
                    multiple
                    selected-class="text-primary"
                  >
                    <v-chip
                      v-for="role in getAvailableRoles(editForm.institutionId)"
                      :key="role.value"
                      :value="role.value"
                      filter
                      variant="outlined"
                    >
                      <template #prepend>
                        <v-icon>{{ role.icon }}</v-icon>
                      </template>
                      {{ role.title }}
                    </v-chip>
                  </v-chip-group>
                </div>

                <v-alert
                  v-if="editForm.roles.length === 0"
                  type="warning"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
                >
                  Selecione pelo menos um papel
                </v-alert>

                <v-alert
                  v-if="!isAdminInstitution(editForm.institutionId)"
                  type="info"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
                >
                  O papel de Administrador só pode ser atribuído na instituição SIMP-ADMIN
                </v-alert>
              </v-form>
            </v-card-text>
            <v-card-actions class="px-4 pb-4">
              <v-spacer />
              <div class="d-flex flex-wrap" style="gap: 8px;">
                <v-btn
                  variant="text"
                  class="form-action-btn"
                  @click="cancelEdit"
                >
                  Cancelar
                </v-btn>
                <v-btn
                  color="primary"
                  variant="flat"
                  class="form-action-btn"
                  :loading="loadingEdit"
                  :disabled="editForm.roles.length === 0"
                  @click="handleUpdateRoles"
                >
                  Salvar
                </v-btn>
              </div>
            </v-card-actions>
          </v-card>
        </v-expand-transition>
      </v-card-text>

      <v-divider />

      <v-card-actions class="px-4 py-3">
        <v-spacer />
        <v-btn
          variant="text"
          @click="handleClose"
        >
          Fechar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import type { Institution, UserInstitutionRole } from '@/types/institution.types'
import type { UserInstitutionDetail } from '@/types/user.types'
import { institutionService } from '@/services/institution.service'

interface Props {
  modelValue: boolean
  userId: string
  userName: string
  institutions: UserInstitutionDetail[]
  isAdmin?: boolean
}

interface Emits {
  (_event: 'update:modelValue', _value: boolean): void
  (_event: 'link', _data: { institutionId: string; roles: UserInstitutionRole[] }): void
  (_event: 'unlink', _institutionId: string): void
  (_event: 'update-roles', _data: { institutionId: string; roles: UserInstitutionRole[] }): void
}

const props = withDefaults(defineProps<Props>(), {
  isAdmin: false,
})
const emit = defineEmits<Emits>()

const formRef = ref()
const editFormRef = ref()
const showAddForm = ref(false)
const showEditForm = ref(false)
const loadingInstitutions = ref(false)
const loadingAdd = ref(false)
const loadingEdit = ref(false)
const loadingUnlink = ref<Record<string, boolean>>({})

const allInstitutions = ref<Institution[]>([])
const addForm = ref({
  institutionId: '',
  roles: [] as UserInstitutionRole[],
})
const editForm = ref({
  institutionId: '',
  institutionName: '',
  roles: [] as UserInstitutionRole[],
})

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const linkedInstitutionIds = computed(() => {
  return props.institutions.map((ui) => ui.institutionId)
})

const availableInstitutions = computed(() => {
  return allInstitutions.value
    .filter((inst) => !linkedInstitutionIds.value.includes(inst.id))
    .filter((inst) => {
      // SECURITY: SIMP-ADMIN institution should only be visible to system administrators
      // Only ADMIN users can link/manage users in SIMP-ADMIN institution
      if (inst.acronym === 'SIMP-ADMIN') {
        return props.isAdmin
      }
      return true
    })
    .map((inst) => ({
      title: inst.name,
      value: inst.id,
    }))
})

const allRoleOptions = [
  { title: 'Administrador', value: 'ADMIN', icon: 'mdi-shield-crown' },
  { title: 'Gestor', value: 'MANAGER', icon: 'mdi-shield-account' },
  { title: 'Visualizador', value: 'VIEWER', icon: 'mdi-eye' },
]

// Função para verificar se é a instituição SIMP-ADMIN
function isAdminInstitution(institutionId: string): boolean {
  if (!institutionId) return false

  // Verifica se é a instituição id=1 ou com acronym SIMP-ADMIN
  const institution = allInstitutions.value.find(inst => inst.id === institutionId)
  if (!institution) {
    // Se estiver editando, verificar nas instituições vinculadas
    const linkedInst = props.institutions.find(ui => ui.institutionId === institutionId)
    return linkedInst?.institution?.acronym === 'SIMP-ADMIN'
  }

  return institution.acronym === 'SIMP-ADMIN'
}

// Função para retornar papéis disponíveis baseado na instituição
function getAvailableRoles(institutionId: string) {
  if (isAdminInstitution(institutionId)) {
    return allRoleOptions // Todos os papéis, incluindo ADMIN
  }
  // Remover ADMIN se não for SIMP-ADMIN
  return allRoleOptions.filter(role => role.value !== 'ADMIN')
}

const rules = {
  required: (value: string) => !!value || 'Campo obrigatório',
}

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) {
      fetchInstitutions()
    }
  }
)

// Watch para remover ADMIN role se a instituição mudou para não-SIMP-ADMIN
watch(
  () => addForm.value.institutionId,
  (institutionId) => {
    if (institutionId && !isAdminInstitution(institutionId)) {
      // Remover ADMIN role se presente
      addForm.value.roles = addForm.value.roles.filter(role => role !== 'ADMIN')
    }
  }
)

// Reset loading states when institutions list changes
watch(
  () => props.institutions,
  () => {
    // Reset all loading states when institutions are updated
    loadingUnlink.value = {}
  },
  { deep: true }
)

onMounted(() => {
  if (props.modelValue) {
    fetchInstitutions()
  }
})

async function fetchInstitutions(): Promise<void> {
  try {
    loadingInstitutions.value = true
    const response = await institutionService.listInstitutions({
      page: 0,
      size: 1000,
      active: true,
    })
    allInstitutions.value = response.content
  } catch (error) {
    console.error('Failed to fetch institutions:', error)
  } finally {
    loadingInstitutions.value = false
  }
}

async function handleAdd(): Promise<void> {
  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid || addForm.value.roles.length === 0) return

  loadingAdd.value = true
  try {
    emit('link', { ...addForm.value })
    cancelAdd()
  } finally {
    loadingAdd.value = false
  }
}

function cancelAdd(): void {
  showAddForm.value = false
  addForm.value = {
    institutionId: '',
    roles: [],
  }
  formRef.value?.reset()
}

function openEditRoles(userInst: UserInstitutionDetail): void {
  // Fechar o form de adicionar se estiver aberto
  if (showAddForm.value) {
    cancelAdd()
  }

  const instName = userInst.institution?.acronym
    ? `${userInst.institution.acronym} - ${userInst.institution.name}`
    : userInst.institution?.name || 'Instituição'

  editForm.value = {
    institutionId: userInst.institutionId,
    institutionName: instName,
    roles: [...userInst.roles],
  }
  showEditForm.value = true
}

async function handleUpdateRoles(): Promise<void> {
  if (!editFormRef.value) return

  const { valid } = await editFormRef.value.validate()
  if (!valid || editForm.value.roles.length === 0) return

  loadingEdit.value = true
  try {
    emit('update-roles', {
      institutionId: editForm.value.institutionId,
      roles: editForm.value.roles,
    })
    cancelEdit()
  } finally {
    loadingEdit.value = false
  }
}

function cancelEdit(): void {
  showEditForm.value = false
  editForm.value = {
    institutionId: '',
    institutionName: '',
    roles: [],
  }
  editFormRef.value?.reset()
}

function handleUnlink(institutionId: string): void {
  loadingUnlink.value[institutionId] = true
  emit('unlink', institutionId)
}

function handleClose(): void {
  cancelAdd()
  cancelEdit()
  emit('update:modelValue', false)
}

function getRoleLabel(role: UserInstitutionRole): string {
  const labels: Record<UserInstitutionRole, string> = {
    ADMIN: 'Administrador',
    MANAGER: 'Gestor',
    VIEWER: 'Visualizador',
  }
  return labels[role]
}

function getRoleIcon(role: UserInstitutionRole): string {
  const icons: Record<UserInstitutionRole, string> = {
    ADMIN: 'mdi-shield-crown',
    MANAGER: 'mdi-shield-account',
    VIEWER: 'mdi-eye',
  }
  return icons[role]
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date)
}
</script>

<style scoped>
.institutions-list {
  max-height: 400px;
  overflow-y: auto;
}

.institution-card-content {
  width: 100%;
}

.action-btn {
  flex: 1 1 auto;
  min-width: 140px;
}

.form-action-btn {
  min-width: 100px;
}

/* Responsividade para telas pequenas */
@media (max-width: 600px) {
  .action-btn {
    flex: 1 1 100%;
    width: 100%;
  }

  .form-action-btn {
    flex: 1;
  }
}
</style>
