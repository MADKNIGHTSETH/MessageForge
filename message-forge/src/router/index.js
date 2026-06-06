import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import ComposeView from '../views/compose/ComposeView.vue'
import HistoryView from '../views/history/HistoryView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminTemplatesView from '../views/admin/AdminTemplatesView.vue'
import AdminStatsView from '../views/admin/AdminStatsView.vue'
import AdminAuditView from '../views/admin/AdminAuditView.vue'

const routes = [
  {
    path: '/compose',
    name: 'Compose',
    component: ComposeView,
    meta: { requiresAuth: true },
  },
  {
    path: '/history',
    name: 'History',
    component: HistoryView,
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/users',
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: AdminUsersView,
      },
      {
        path: 'templates',
        name: 'AdminTemplates',
        component: AdminTemplatesView,
      },
      {
        path: 'stats',
        name: 'AdminStats',
        component: AdminStatsView,
      },
      {
        path: 'audit',
        name: 'AdminAudit',
        component: AdminAuditView,
      },
    ],
  },
  {
    path: '/:catchAll(.*)*',
    redirect: '/compose',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && (!authStore.isAuthenticated || !authStore.hasValidJwt)) {
    return next({ path: '/login' })
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return next({ path: '/compose' })
  }

  if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    return next({ path: '/compose' })
  }

  return next()
})

export default router
