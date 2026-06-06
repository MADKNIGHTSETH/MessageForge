import { defineStore } from 'pinia'
import { generateUuid } from '../services/mockApi'
import { useAuthStore } from './auth'

const USERS_STORAGE_KEY = 'messageforge_admin_users'
const AUDIT_STORAGE_KEY = 'messageforge_audit_logs'

const parseStorage = (key, fallback) => {
  try {
    return JSON.parse(localStorage.getItem(key) || 'null') || fallback
  } catch {
    return fallback
  }
}

const saveStorage = (key, value) => {
  localStorage.setItem(key, JSON.stringify(value))
}

const escapeCsvCell = (value) => {
  const normalized = typeof value === 'object' ? JSON.stringify(value) : String(value ?? '')
  return `"${normalized.replace(/"/g, '""')}"`
}

export const useAdminStore = defineStore('admin', {
  state: () => ({
    users: [],
    auditLogs: [],
    isLoading: false,
    error: '',
  }),

  actions: {
    ensureSeedData() {
      const authStore = useAuthStore()
      const savedUsers = parseStorage(USERS_STORAGE_KEY, [])
      const savedLogs = parseStorage(AUDIT_STORAGE_KEY, [])

      if (savedUsers.length === 0) {
        const adminUser = authStore.user || {
          id: generateUuid(),
          email: 'admin@messageforge.local',
          displayName: 'Admin',
          role: 'admin',
          isActive: true,
          createdAt: new Date().toISOString(),
        }

        saveStorage(USERS_STORAGE_KEY, [
          {
            id: adminUser.id,
            email: adminUser.email,
            displayName: adminUser.displayName,
            role: adminUser.role || 'admin',
            isActive: adminUser.isActive ?? true,
            createdAt: adminUser.createdAt || new Date().toISOString(),
          },
          {
            id: generateUuid(),
            email: 'user@messageforge.local',
            displayName: 'Utilisateur demo',
            role: 'user',
            isActive: true,
            createdAt: new Date().toISOString(),
          },
        ])
      }

      if (savedLogs.length === 0) {
        saveStorage(AUDIT_STORAGE_KEY, [
          {
            id: generateUuid(),
            adminId: authStore.user?.id || 'system',
            action: 'ADMIN_SPACE_OPENED',
            details: { source: 'mock' },
            createdAt: new Date().toISOString(),
          },
        ])
      }
    },

    async fetchUsers() {
      this.isLoading = true
      this.error = ''
      try {
        this.ensureSeedData()
        this.users = parseStorage(USERS_STORAGE_KEY, [])
        return this.users
      } catch (error) {
        this.error = error.message || 'Impossible de charger les utilisateurs.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async toggleUserStatus(userId, currentStatus) {
      const authStore = useAuthStore()
      const users = parseStorage(USERS_STORAGE_KEY, [])
      const nextStatus = !currentStatus
      const updatedUsers = users.map((user) =>
        user.id === userId
          ? { ...user, isActive: nextStatus, updatedAt: new Date().toISOString() }
          : user
      )

      saveStorage(USERS_STORAGE_KEY, updatedUsers)
      this.users = updatedUsers

      const targetUser = updatedUsers.find((user) => user.id === userId)
      await this.addAuditLog({
        adminId: authStore.user?.id || 'unknown',
        action: nextStatus ? 'USER_ACTIVATED' : 'USER_DEACTIVATED',
        details: {
          userId,
          email: targetUser?.email,
          previousStatus: currentStatus,
          nextStatus,
        },
      })

      return targetUser
    },

    async fetchAuditLogs() {
      this.isLoading = true
      this.error = ''
      try {
        this.ensureSeedData()
        this.auditLogs = parseStorage(AUDIT_STORAGE_KEY, [])
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        return this.auditLogs
      } catch (error) {
        this.error = error.message || 'Impossible de charger les journaux.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async addAuditLog({ adminId, action, details = {} }) {
      const logs = parseStorage(AUDIT_STORAGE_KEY, [])
      const log = {
        id: generateUuid(),
        adminId,
        action,
        details,
        createdAt: new Date().toISOString(),
      }

      const nextLogs = [log, ...logs]
      saveStorage(AUDIT_STORAGE_KEY, nextLogs)
      this.auditLogs = nextLogs
      return log
    },

    exportAuditLogsToCSV() {
      const logs = this.auditLogs.length ? this.auditLogs : parseStorage(AUDIT_STORAGE_KEY, [])
      const header = ['id', 'admin_id', 'action', 'details', 'created_at']
      const rows = logs.map((log) => [
        log.id,
        log.adminId,
        log.action,
        log.details,
        log.createdAt,
      ])

      const csv = [header, ...rows]
        .map((row) => row.map(escapeCsvCell).join(','))
        .join('\n')

      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `messageforge-audit-${new Date().toISOString().slice(0, 10)}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    },
  },
})
