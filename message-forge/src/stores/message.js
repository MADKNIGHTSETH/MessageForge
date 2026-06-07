import { defineStore } from 'pinia'
import { messagesApi } from '../api/messages'
import { ApiError } from '../api/client'
import {
  mapApiMessageToDraft,
  mapApiMessageToHistory,
  mapChannelMessagesToProgress,
  mapDraftToApiPayload,
  toApiChannel,
} from '../api/mappers'

const STORAGE_KEY = 'messageforge_messages'

const generateUuid = () => {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }

  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

const createDraft = (userId = 'api-user') => ({
  id: generateUuid(),
  remoteId: null,
  userId,
  subject: '',
  rawContent: '',
  recipients: [],
  activeChannels: ['Email', 'SMS'],
  selectedAccounts: {},
  decorators: {
    emoji: true,
    hashtag: false,
    signature: true,
  },
  metadata: {
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  status: 'DRAFT',
})

const loadState = () => {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch {
    return {}
  }
}

const wait = (duration) => new Promise((resolve) => window.setTimeout(resolve, duration))
const isFinalStatus = (status) => status === 'SENT' || status === 'FAILED'

export const useMessageStore = defineStore('message', {
  state: () => {
    const saved = loadState()
    return {
      currentDraft: saved.currentDraft || createDraft(),
      drafts: saved.drafts || [],
      messages: saved.messages || [],
      documents: saved.documents || {},
      history: saved.history || [],
      sendProgress: [],
      sendTimerId: null,
      isSaving: false,
      isSending: false,
      isHistoryLoading: false,
      error: '',
    }
  },

  actions: {
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          currentDraft: this.currentDraft,
          drafts: this.drafts,
          messages: this.messages,
          documents: this.documents,
          history: this.history,
        }),
      )
    },

    createDraft(userId) {
      const draft = createDraft(userId)
      this.currentDraft = draft
      this.drafts.unshift(draft)
      this.documents[draft.id] = { ...draft }
      this.persist()
      return draft
    },

    updateDraft(changes) {
      this.currentDraft = {
        ...this.currentDraft,
        ...changes,
        metadata: {
          ...this.currentDraft.metadata,
          updatedAt: new Date().toISOString(),
        },
      }
      this.documents[this.currentDraft.id] = {
        ...(this.documents[this.currentDraft.id] || {}),
        ...this.currentDraft,
      }
      this.persist()
    },

    async saveCurrentDraft() {
      if (!this.currentDraft.rawContent?.trim()) {
        throw new Error('Saisissez un message avant de continuer.')
      }

      this.isSaving = true
      const payload = mapDraftToApiPayload(this.currentDraft)

      try {
        let message
        if (this.currentDraft.remoteId) {
          try {
            message = await messagesApi.updateMessage(this.currentDraft.remoteId, payload)
          } catch (error) {
            if (!(error instanceof ApiError) || error.status !== 404) {
              throw error
            }
            message = await messagesApi.createMessage(payload)
          }
        } else {
          message = await messagesApi.createMessage(payload)
        }

        const draft = mapApiMessageToDraft(message, this.currentDraft.userId)
        this.currentDraft = draft
        this.documents[draft.id] = { ...draft }
        this.persist()
        return message
      } finally {
        this.isSaving = false
      }
    },

    async sendCurrentDraft() {
      if (!this.currentDraft?.activeChannels?.length) {
        this.error = 'Selectionnez au moins un canal.'
        return null
      }

      if (this.sendTimerId) {
        clearInterval(this.sendTimerId)
        this.sendTimerId = null
      }

      const channels = [...this.currentDraft.activeChannels]
      this.isSending = true
      this.error = ''
      this.sendProgress = mapChannelMessagesToProgress([], channels)
      this.currentDraft.status = 'SENDING'
      this.persist()

      try {
        const savedMessage = await this.saveCurrentDraft()
        const response = await messagesApi.sendMessage(savedMessage.id, {
          channels: channels.map(toApiChannel),
          testMode: true,
        })

        this.sendProgress = mapChannelMessagesToProgress(response.channelMessages, channels)
        const finalMessage = await this.pollMessageStatus(savedMessage.id, channels)
        const historyItem = mapApiMessageToHistory(finalMessage || response)

        this.history = [
          historyItem,
          ...this.history.filter((item) => item.id !== historyItem.id),
        ]
        this.messages = [
          {
            id: historyItem.id,
            userId: historyItem.userId,
            createdAt: historyItem.createdAt,
          },
          ...this.messages.filter((item) => item.id !== historyItem.id),
        ]

        this.currentDraft = createDraft(this.currentDraft.userId)
        this.documents[this.currentDraft.id] = { ...this.currentDraft }
        this.persist()
        return historyItem
      } catch (error) {
        this.error = error.message || 'Envoi impossible.'
        this.currentDraft.status = 'FAILED'
        this.sendProgress = this.sendProgress.map((item) => ({
          ...item,
          status: 'FAILED',
          progress: 100,
          errorMessage: this.error,
        }))
        this.persist()
        return null
      } finally {
        this.isSending = false
      }
    },

    async pollMessageStatus(messageId, fallbackChannels) {
      let latest = null

      for (let attempt = 0; attempt < 6; attempt += 1) {
        await wait(700)
        latest = await messagesApi.getMessage(messageId)
        this.sendProgress = mapChannelMessagesToProgress(latest.channelMessages, fallbackChannels)

        if (isFinalStatus(latest.status)) {
          return latest
        }
      }

      return latest
    },

    async loadHistory() {
      this.isHistoryLoading = true
      this.error = ''

      try {
        const response = await messagesApi.listMessages({ page: 0, size: 50 })
        this.history = (response.messages || []).map(mapApiMessageToHistory)
        this.messages = this.history.map((item) => ({
          id: item.id,
          userId: item.userId,
          createdAt: item.createdAt,
        }))
        this.persist()
        return this.history
      } catch (error) {
        this.error = error.message || "Impossible de charger l'historique."
        return this.history
      } finally {
        this.isHistoryLoading = false
      }
    },

    clearProgress() {
      if (this.sendTimerId) {
        clearInterval(this.sendTimerId)
        this.sendTimerId = null
      }
      this.sendProgress = []
    },
  },
})
