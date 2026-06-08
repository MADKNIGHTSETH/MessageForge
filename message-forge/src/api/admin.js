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

  getStats() {
    return apiRequest('/admin/stats')
  },

  getTemplates() {
    return apiRequest('/admin/templates')
  },

  createTemplate(payload) {
    return apiRequest('/admin/templates', {
      method: 'POST',
      body: payload,
    })
  },

  updateTemplate(id, payload) {
    return apiRequest(`/admin/templates/${id}`, {
      method: 'PUT',
      body: payload,
    })
  },

  deleteTemplate(id) {
    return apiRequest(`/admin/templates/${id}`, {
      method: 'DELETE',
    })
  },
}
