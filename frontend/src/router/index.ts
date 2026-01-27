import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupGuards } from './guards'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'landing',
    component: () => import('@/views/public/LandingPage.vue'),
    meta: {
      layout: 'public',
      requiresAuth: false,
    },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/public/LoginPage.vue'),
    meta: {
      layout: 'public',
      requiresAuth: false,
    },
  },
  {
    path: '/auth/callback',
    name: 'auth-callback',
    component: () => import('@/views/auth/OAuthCallbackPage.vue'),
    meta: {
      layout: 'none',
      requiresAuth: false,
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/private/DashboardPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: true,
    },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/private/ProfilePage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
    },
  },
  {
    path: '/institution-selection',
    name: 'institution-selection',
    component: () => import('@/views/private/InstitutionSelectionPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: false,
    },
  },
  {
    path: '/admin/institutions',
    name: 'admin-institutions',
    component: () => import('@/views/private/admin/InstitutionsPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: true,
      requiresUserManagement: true, // ADMIN ou MANAGER
    },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('@/views/private/admin/UsersPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: true,
      requiresUserManagement: true, // ADMIN ou MANAGER podem acessar
    },
  },
  {
    path: '/value-chains',
    name: 'value-chains',
    component: () => import('@/views/private/ValueChainsPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: true,
      requiresUserManagement: true, // ADMIN ou MANAGER
    },
  },
  {
    path: '/units',
    name: 'units',
    component: () => import('@/views/private/UnitsPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: true,
      requiresUserManagement: true, // ADMIN ou MANAGER
    },
  },
  {
    path: '/admin/settings',
    name: 'admin-settings',
    component: () => import('@/views/private/admin/SettingsPage.vue'),
    meta: {
      layout: 'private',
      requiresAuth: true,
      requiresInstitution: true,
      requiresAdmin: true,
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, _from, savedPosition) {
    // Se há uma posição salva (botão voltar), use-a
    if (savedPosition) {
      return savedPosition
    }

    // Se há hash na URL (navegação por âncoras), scroll para o elemento
    if (to.hash) {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            el: to.hash,
            behavior: 'smooth',
            top: 80, // Offset para compensar o header
          })
        }, 100)
      })
    }

    // Por padrão, sempre scroll para o topo
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ top: 0, behavior: 'instant' })
      }, 0)
    })
  },
})

// Setup navigation guards
setupGuards(router)

export default router
