import { defineStore } from 'pinia'
import { authApi } from '../api/auth'
import { channelsApi } from '../api/channels'
import { clearApiTokens, setApiTokens } from '../api/client'
import {
  accountToIntegrationPayload,
  integrationToAccount,
  toApiChannel,
} from '../api/mappers'

const AUTH_STORAGE_KEY = 'messageforge_auth'

const loadAuthState = () => {
  try {
    return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY) || 'null') || {}
  } catch {
    return {}
  }
}

const decodeJwtPayload = (token) => {
  const parts = token.split('.')
  if (parts.length < 3) {
    return null
  }

  const payloadPart = parts[1]
  const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=')
  return JSON.parse(atob(padded))
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const saved = loadAuthState()
    setApiTokens({ accessToken: saved.token, refreshToken: saved.refreshToken })

    return {
      user: saved.user || null,
      token: saved.token || null,
      refreshToken: saved.refreshToken || null,
      connectedAccounts: saved.connectedAccounts || [],
      isLoading: false,
      error: '',
    }
  },

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    accountsByType: (state) => (type) => state.connectedAccounts.filter((acc) => acc.type === type),
    hasValidJwt: (state) => {
      if (!state.token) return false

      try {
        const payload = decodeJwtPayload(state.token)
        return !payload?.exp || payload.exp > Math.floor(Date.now() / 1000)
      } catch {
        return true
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
          refreshToken: this.refreshToken,
          connectedAccounts: this.connectedAccounts,
        }),
      )
    },

    normalizeUser(user) {
      return {
        ...user,
        isActive: user.isActive ?? true,
        role: user.role || 'USER',
      }
    },

    setSession(response) {
      this.user = this.normalizeUser(response.user)
      this.token = response.accessToken
      this.refreshToken = response.refreshToken
      setApiTokens({ accessToken: this.token, refreshToken: this.refreshToken })
      this.persist()
    },

    async fetchConnectedAccounts() {
      const integrations = await channelsApi.listIntegrations()
      this.connectedAccounts = integrations
        .filter((integration) => integration.enabled)
        .map(integrationToAccount)
      this.persist()
      return this.connectedAccounts
    },

    async login({ email, password }) {
      this.isLoading = true
      this.error = ''

      try {
        const response = await authApi.login({ email, password })
        this.setSession(response)
        await this.fetchConnectedAccounts()
        return this.user
      } catch (error) {
        this.error = error.message || 'Connexion impossible.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async register({ email, password, displayName }) {
      this.isLoading = true
      this.error = ''

      try {
        await authApi.register({ email, password, displayName })
      } catch (error) {
        this.isLoading = false
        this.error = error.message || 'Inscription impossible.'
        throw error
      }

      this.isLoading = false
      return this.login({ email, password })
    },

    async refreshSession() {
      if (!this.refreshToken) {
        return null
      }

      const response = await authApi.refresh(this.refreshToken)
      this.setSession(response)
      return this.user
    },

    async addAccount(account) {
      this.error = ''
      const apiChannel = toApiChannel(account.type)
      const integration = await channelsApi.configureIntegration(
        apiChannel,
        accountToIntegrationPayload(account),
      )
      const nextAccount = integrationToAccount(integration)

      this.connectedAccounts = [
        ...this.connectedAccounts.filter((existing) => existing.type !== nextAccount.type),
        nextAccount,
      ]
      this.persist()
      return nextAccount
    },

    async removeAccount(id) {
      const account = this.connectedAccounts.find((item) => item.id === id)
      if (!account) {
        return
      }

      await channelsApi.deleteIntegration(toApiChannel(account.type))
      this.connectedAccounts = this.connectedAccounts.filter((item) => item.id !== id)
      this.persist()
    },

    async logout() {
      try {
        if (this.token) {
          await authApi.logout()
        }
      } finally {
        this.user = null
        this.token = null
        this.refreshToken = null
        this.connectedAccounts = []
        clearApiTokens()
        localStorage.removeItem(AUTH_STORAGE_KEY)
      }
    },
  },
})
