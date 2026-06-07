import { apiRequest } from './client'

export const authApi = {
  register(payload) {
    return apiRequest('/auth/register', {
      method: 'POST',
      body: payload,
      auth: false,
    })
  },

  login(payload) {
    return apiRequest('/auth/login', {
      method: 'POST',
      body: payload,
      auth: false,
    })
  },

  refresh(refreshToken) {
    return apiRequest('/auth/refresh', {
      method: 'POST',
      body: { refreshToken },
      auth: false,
    })
  },

  logout() {
    return apiRequest('/auth/logout', {
      method: 'POST',
    })
  },

  me() {
    return apiRequest('/auth/me')
  },
}
