import { defineStore } from 'pinia'
import { adminApi } from '../api/admin'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    users: [],
    auditLogs: [],
    stats: null,
    templates: [],
    isLoading: false,
    error: '',
  }),

  actions: {
    async fetchUsers() {
      this.isLoading = true
      this.error = ''
      try {
        this.users = await adminApi.getUsers()
        return this.users
      } catch (error) {
        this.error = error.message || 'Impossible de charger les utilisateurs.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchTemplates() {
      this.isLoading = true
      this.error = ''
      try {
        this.templates = await adminApi.getTemplates()
        return this.templates
      } catch (error) {
        this.error = error.message || 'Impossible de charger les templates.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async createTemplate(payload) {
      this.error = ''
      try {
        const newTemplate = await adminApi.createTemplate(payload)
        this.templates.unshift(newTemplate)
        return newTemplate
      } catch (error) {
        this.error = error.message || 'Création impossible.'
        throw error
      }
    },

    async deleteTemplate(id) {
      this.error = ''
      try {
        await adminApi.deleteTemplate(id)
        this.templates = this.templates.filter((t) => t.id !== id)
      } catch (error) {
        this.error = error.message || 'Suppression impossible.'
        throw error
      }
    },

    async fetchStats() {
      this.isLoading = true
      this.error = ''
      try {
        this.stats = await adminApi.getStats()
        return this.stats
      } catch (error) {
        this.error = error.message || 'Impossible de charger les statistiques.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async toggleUserStatus(userId, currentStatus) {
      this.error = ''
      try {
        const nextStatus = !currentStatus
        const updatedUser = await adminApi.toggleUserStatus(userId, nextStatus)
        
        // Update local state
        this.users = this.users.map((user) =>
          user.id === userId ? updatedUser : user
        )
        
        return updatedUser
      } catch (error) {
        this.error = error.message || 'Action impossible.'
        throw error
      }
    },

    async fetchAuditLogs() {
      this.isLoading = true
      this.error = ''
      try {
        this.auditLogs = await adminApi.getAuditLogs()
        return this.auditLogs
      } catch (error) {
        this.error = error.message || 'Impossible de charger les journaux.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    exportAuditLogsToCSV() {
      const logs = this.auditLogs
      const header = ['id', 'user_id', 'email', 'action', 'entity_type', 'entity_id', 'created_at']
      const rows = logs.map((log) => [
        log.id,
        log.userId,
        log.userEmail,
        log.action,
        log.entityType,
        log.entityId,
        log.createdAt,
      ])

      const escapeCsvCell = (value) => {
        const normalized = typeof value === 'object' ? JSON.stringify(value) : String(value ?? '')
        return `"${normalized.replace(/"/g, '""')}"`
      }

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
