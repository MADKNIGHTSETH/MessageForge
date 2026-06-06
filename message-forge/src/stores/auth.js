import { defineStore } from 'pinia'
import { createJwt, generateUuid } from '../services/mockApi'

const AUTH_STORAGE_KEY = 'messageforge_auth'
const MOCK_ADMIN_EMAILS = ['admin@messageforge.local']

const loadAuthState = () => {
  try {
    return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY) || 'null') || {}
  } catch {
    return {}
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const saved = loadAuthState()
    return {
      user: saved.user || null,
      token: saved.token || null,
      connectedAccounts: saved.connectedAccounts || [
        { id: '1', type: 'Email', value: 'ranya@gmail.com', label: 'Email Personnel' },
        { id: '2', type: 'SMS', value: '+33 6 12 34 56 78', label: 'Mobile Pro' },
        { id: '3', type: 'LinkedIn', value: 'ranya-linked', label: 'LinkedIn Pro' }
      ],
    }
  },
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'admin',
    accountsByType: (state) => (type) => state.connectedAccounts.filter(acc => acc.type === type),
    hasValidJwt: (state) => {
      if (!state.token) return false
      const parts = state.token.split('.')
      if (parts.length < 3) return false

      if (parts[0] !== 'mock') {
        return true
      }

      try {
        const payload = JSON.parse(atob(parts[2]))
        return !payload.exp || payload.exp > Math.floor(Date.now() / 1000)
      } catch {
        return false
      }
    },
  },
  actions: {
    persist() {
      localStorage.setItem(
        AUTH_STORAGE_KEY,
        JSON.stringify({
          user: this.user,
          token: this.token,
          connectedAccounts: this.connectedAccounts,
        })
      )
    },

    addAccount(account) {
      const newAccount = {
        id: generateUuid(),
        ...account
      }
      this.connectedAccounts.push(newAccount)
      this.persist()
    },

    removeAccount(id) {
      this.connectedAccounts = this.connectedAccounts.filter(acc => acc.id !== id)
      this.persist()
    },

    resolveRole(email, explicitRole) {
      if (explicitRole === 'admin' || explicitRole === 'user') {
        return explicitRole
      }

      return MOCK_ADMIN_EMAILS.includes(String(email).toLowerCase()) ? 'admin' : 'user'
    },

    async login({ email, password, role }) {
      const resolvedRole = this.resolveRole(email, role)
      const user = {
        id: generateUuid(),
        email,
        displayName: email.split('@')[0],
        avatarUrl: '',
        isActive: true,
        role: resolvedRole,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }

      this.user = user
      this.token = createJwt({ userId: user.id, email, role: resolvedRole, isActive: user.isActive })
      this.persist()
      return user
    },

    async register({ email, password, displayName, role }) {
      const resolvedRole = this.resolveRole(email, role)
      const user = {
        id: generateUuid(),
        email,
        displayName: displayName || email.split('@')[0],
        avatarUrl: '',
        isActive: true,
        role: resolvedRole,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }

      this.user = user
      this.token = createJwt({ userId: user.id, email, role: resolvedRole, isActive: user.isActive })
      this.persist()
      return user
    },

    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem(AUTH_STORAGE_KEY)
    },
  },
})
