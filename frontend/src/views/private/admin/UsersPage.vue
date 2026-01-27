<template>
  <div class="users-page">
    <PageHeader
      title="Usuários"
      subtitle="Gerencie os usuários do sistema"
    />

    <v-card variant="flat" border>
      <UserList
        :items="users"
        :total-items="totalUsers"
        :loading="isLoading"
        :is-admin="isAdmin"
        @update:filters="handleFiltersUpdate"
        @update:pagination="handlePaginationUpdate"
        @edit="openEditDialog"
        @manage-institutions="openInstitutionsDialog"
      />
    </v-card>

    <!-- Dialog de edição de usuário -->
    <UserEditDialog
      v-model="dialogs.edit.show"
      :user="dialogs.edit.user"
      :loading="dialogs.edit.loading"
      @submit="handleUpdateUser"
    />

    <!-- Dialog de gerenciamento de instituições e papéis -->
    <UserInstitutionsDialog
      v-model="dialogs.institutions.show"
      :user-id="dialogs.institutions.userId"
      :user-name="dialogs.institutions.userName"
      :institutions="dialogs.institutions.institutions"
      :is-admin="isAdmin"
      @link="handleLinkInstitution"
      @unlink="handleUnlinkInstitution"
      @update-roles="handleUpdateRoles"
    />

    <!-- Snackbar de feedback -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="3000"
      location="top right"
    >
      {{ snackbar.message }}
      <template #actions>
        <v-btn
          variant="text"
          @click="snackbar.show = false"
        >
          Fechar
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import UserList from '@/components/user/UserList.vue'
import UserEditDialog from '@/components/user/UserEditDialog.vue'
import UserInstitutionsDialog from '@/components/user/UserInstitutionsDialog.vue'
import { userService } from '@/services/user.service'
import { useAuthStore } from '@/stores/auth.store'
import { useInstitutionStore } from '@/stores/institution.store'
import type { UserListItem, UserInstitutionDetail } from '@/types/user.types'
import type { UserStatus } from '@/types/auth.types'
import type { UserInstitutionRole } from '@/types/institution.types'

interface Filters {
  search?: string
  status?: UserStatus | null
  institutionId?: string | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

// Stores
const authStore = useAuthStore()
const institutionStore = useInstitutionStore()

// State
const users = ref<UserListItem[]>([])
const totalUsers = ref(0)
const isLoading = ref(false)

const filters = ref<Filters>({
  search: '',
  status: null,
  institutionId: null,
})

const pagination = ref<Pagination>({
  page: 1,
  itemsPerPage: 25,
  sortBy: [{ key: 'name', order: 'asc' }],
})

const dialogs = ref({
  edit: {
    show: false,
    user: null as { id: string; name: string; status: UserStatus } | null,
    loading: false,
  },
  institutions: {
    show: false,
    userId: '',
    userName: '',
    institutions: [] as UserInstitutionDetail[],
  },
})

const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

// Computed
const isAdmin = computed(() => authStore.isAdmin)
const currentInstitutionId = computed(() => institutionStore.activeInstitution?.id || null)

// Methods
async function fetchUsers(): Promise<void> {
  try {
    isLoading.value = true

    // Se não é ADMIN, filtrar automaticamente pela instituição atual
    const effectiveFilters = { ...filters.value }
    if (!isAdmin.value && currentInstitutionId.value) {
      effectiveFilters.institutionId = currentInstitutionId.value
    }

    // Montar sort string
    let sortStr = 'name,asc'
    if (pagination.value.sortBy.length > 0) {
      const sort = pagination.value.sortBy[0]
      if (sort) {
        sortStr = `${sort.key},${sort.order}`
      }
    }

    const response = await userService.listUsers(effectiveFilters, {
      page: pagination.value.page,
      size: pagination.value.itemsPerPage,
      sort: sortStr,
    })

    users.value = response.content
    totalUsers.value = response.totalElements
  } catch (error) {
    console.error('Failed to fetch users:', error)
    showSnackbar('Erro ao carregar usuários', 'error')
  } finally {
    isLoading.value = false
  }
}

function handleFiltersUpdate(newFilters: Filters): void {
  filters.value = { ...newFilters }
  pagination.value.page = 1 // Reset para primeira página
  fetchUsers()
}

function handlePaginationUpdate(newPagination: Pagination): void {
  pagination.value = { ...newPagination }
  fetchUsers()
}

async function openEditDialog(user: UserListItem): Promise<void> {
  dialogs.value.edit.user = {
    id: user.id,
    name: user.name,
    status: user.status,
  }
  dialogs.value.edit.show = true
}

async function openInstitutionsDialog(user: UserListItem): Promise<void> {
  try {
    // Buscar detalhes do usuário com instituições
    const userDetail = await userService.getUserById(user.id)

    dialogs.value.institutions.userId = user.id
    dialogs.value.institutions.userName = user.name
    dialogs.value.institutions.institutions = userDetail.institutions
    dialogs.value.institutions.show = true
  } catch (error) {
    console.error('Failed to fetch user details:', error)
    showSnackbar('Erro ao carregar dados do usuário', 'error')
  }
}

async function handleUpdateUser(data: { name: string; status: UserStatus }): Promise<void> {
  if (!dialogs.value.edit.user) return

  try {
    dialogs.value.edit.loading = true
    await userService.updateUser(dialogs.value.edit.user.id, data)

    showSnackbar('Usuário atualizado com sucesso', 'success')
    dialogs.value.edit.show = false
    await fetchUsers()
  } catch (error) {
    console.error('Failed to update user:', error)
    showSnackbar('Erro ao atualizar usuário', 'error')
  } finally {
    dialogs.value.edit.loading = false
  }
}

async function handleUpdateRoles(data: { institutionId: string; roles: UserInstitutionRole[] }): Promise<void> {
  try {
    await userService.updateUserRoles(dialogs.value.institutions.userId, data)

    showSnackbar('Papéis atualizados com sucesso', 'success')

    // Recarregar instituições do usuário
    const userDetail = await userService.getUserById(dialogs.value.institutions.userId)
    dialogs.value.institutions.institutions = userDetail.institutions

    await fetchUsers()
  } catch (error) {
    console.error('Failed to update roles:', error)
    showSnackbar('Erro ao atualizar papéis', 'error')
  }
}

async function handleLinkInstitution(data: { institutionId: string; roles: UserInstitutionRole[] }): Promise<void> {
  try {
    await userService.linkUserToInstitution(dialogs.value.institutions.userId, data)

    showSnackbar('Instituição vinculada com sucesso', 'success')

    // Recarregar instituições do usuário
    const userDetail = await userService.getUserById(dialogs.value.institutions.userId)
    dialogs.value.institutions.institutions = userDetail.institutions

    await fetchUsers()
  } catch (error) {
    console.error('Failed to link institution:', error)
    showSnackbar('Erro ao vincular instituição', 'error')
  }
}

async function handleUnlinkInstitution(institutionId: string): Promise<void> {
  try {
    await userService.unlinkUserFromInstitution(dialogs.value.institutions.userId, institutionId)

    showSnackbar('Instituição desvinculada com sucesso', 'success')

    // Recarregar instituições do usuário
    const userDetail = await userService.getUserById(dialogs.value.institutions.userId)
    dialogs.value.institutions.institutions = userDetail.institutions

    await fetchUsers()
  } catch (error) {
    console.error('Failed to unlink institution:', error)
    showSnackbar('Erro ao desvincular instituição', 'error')
  }
}

function showSnackbar(message: string, color: string): void {
  snackbar.value = {
    show: true,
    message,
    color,
  }
}

// Lifecycle
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-page {
  /* Add custom styles if needed */
}
</style>
