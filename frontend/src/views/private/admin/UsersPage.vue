<template>
  <div class="users-page">
    <PageHeader
      title="Usuários"
      subtitle="Gerencie os usuários do sistema"
    >
      <template #actions>
<!--        <v-btn-->
<!--          color="primary"-->
<!--          variant="flat"-->
<!--          prepend-icon="mdi-plus"-->
<!--          @click="openCreateDialog"-->
<!--        >-->
<!--          Novo Usuário-->
<!--        </v-btn>-->
      </template>
    </PageHeader>

    <v-alert
      type="info"
      variant="tonal"
      class="mb-4"
    >
      Página em desenvolvimento. Em breve você poderá gerenciar usuários aqui.
    </v-alert>

    <v-card variant="flat" border>
      <UserList
        :items="users"
        :total-items="totalUsers"
        :loading="isLoading"
        @update:filters="handleFiltersUpdate"
        @update:pagination="handlePaginationUpdate"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
        @manage-institutions="handleManageInstitutions"
      />
    </v-card>

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
import { ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import UserList from '@/components/user/UserList.vue'

interface User {
  id: string
  name: string
  email: string
  pictureUrl?: string
  provider: string
  role: string
  status: string
  createdAt: string
}

interface Filters {
  search: string
  status: string | null
  role: string | null
}

interface Pagination {
  page: number
  itemsPerPage: number
  sortBy: Array<{ key: string; order: 'asc' | 'desc' }>
}

// State
const users = ref<User[]>([])
const totalUsers = ref(0)
const isLoading = ref(false)

const snackbar = ref({
  show: false,
  message: '',
  color: 'success',
})

// Event handlers
function handleFiltersUpdate(newFilters: Filters): void {
  console.log('Filters updated:', newFilters)
  // TODO: Fetch users with filters
  showSnackbar('Funcionalidade em desenvolvimento', 'info')
}

function handlePaginationUpdate(newPagination: Pagination): void {
  console.log('Pagination updated:', newPagination)
  // TODO: Fetch users with pagination
  showSnackbar('Funcionalidade em desenvolvimento', 'info')
}

// function openCreateDialog(): void {
//   showSnackbar('Funcionalidade em desenvolvimento', 'info')
// }

function openEditDialog(user: User): void {
  console.log('Edit user:', user)
  showSnackbar('Funcionalidade em desenvolvimento', 'info')
}

function openDeleteDialog(user: User): void {
  console.log('Delete user:', user)
  showSnackbar('Funcionalidade em desenvolvimento', 'info')
}

function handleManageInstitutions(user: User): void {
  console.log('Manage institutions for user:', user.id)
  showSnackbar('Funcionalidade em desenvolvimento', 'info')
}

function showSnackbar(message: string, color: string): void {
  snackbar.value = {
    show: true,
    message,
    color,
  }
}
</script>

<style scoped>
.users-page {
  /* Add custom styles if needed */
}
</style>
