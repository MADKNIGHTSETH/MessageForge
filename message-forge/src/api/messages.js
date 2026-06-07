import { apiRequest } from './client'

const withQuery = (path, params = {}) => {
  const query = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      query.set(key, value)
    }
  })

  const queryString = query.toString()
  return queryString ? `${path}?${queryString}` : path
}

export const messagesApi = {
  listMessages({ page = 0, size = 50 } = {}) {
    return apiRequest(withQuery('/messages', { page, size }))
  },

  createMessage(payload) {
    return apiRequest('/messages', {
      method: 'POST',
      body: payload,
    })
  },

  getMessage(id) {
    return apiRequest(`/messages/${encodeURIComponent(id)}`)
  },

  updateMessage(id, payload) {
    return apiRequest(`/messages/${encodeURIComponent(id)}`, {
      method: 'PUT',
      body: payload,
    })
  },

  deleteMessage(id) {
    return apiRequest(`/messages/${encodeURIComponent(id)}`, {
      method: 'DELETE',
    })
  },

  duplicateMessage(id) {
    return apiRequest(`/messages/${encodeURIComponent(id)}/duplicate`, {
      method: 'POST',
    })
  },

  preview(payload) {
    return apiRequest('/messages/preview', {
      method: 'POST',
      body: payload,
    })
  },

  getSavedPreview(id) {
    return apiRequest(`/messages/${encodeURIComponent(id)}/preview`)
  },

  sendMessage(id, payload) {
    return apiRequest(`/messages/${encodeURIComponent(id)}/send`, {
      method: 'POST',
      body: payload,
    })
  },
}
