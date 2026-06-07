import { apiRequest } from './client'

export const adminApi = {
  getUsers() {
    return apiRequest('/admin/users')
  },

  toggleUserStatus(userId, isActive) {
    return apiRequest(`/admin/users/${userId}/toggle-status`, {
      method: 'POST',
      body: { active: isActive },
    })
  },

  getAuditLogs() {
    return apiRequest('/admin/audit-logs')
  },
}
