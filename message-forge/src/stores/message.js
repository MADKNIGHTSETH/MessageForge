import { defineStore } from 'pinia'
import { generateUuid } from '../services/mockApi'

const STORAGE_KEY = 'messageforge_messages'

const createDraft = (userId = 'local-user') => ({
  id: generateUuid(),
  userId,
  subject: '',
  rawContent: '',
  recipients: [],
  activeChannels: ['Email', 'SMS'],
  selectedAccounts: {}, // Mapping channel -> accountId
  decorators: {
    emoji: true,
    hashtag: false,
    signature: true,
  },
  metadata: {
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  status: 'draft',
})

const loadState = () => {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch {
    return {}
  }
}

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
        })
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

    sendCurrentDraft() {
      if (!this.currentDraft?.id || !this.currentDraft.activeChannels?.length) {
        return
      }

      if (this.sendTimerId) {
        clearInterval(this.sendTimerId)
        this.sendTimerId = null
      }

      const channels = [...this.currentDraft.activeChannels]
      this.sendProgress = channels.map((channel) => ({
        channel,
        status: 'PENDING',
        progress: 0,
        errorMessage: null,
      }))
      this.currentDraft.status = 'sending'
      this.persist()

      let step = 0
      this.sendTimerId = window.setInterval(() => {
        const channel = channels[step]
        if (!channel) {
          clearInterval(this.sendTimerId)
          this.sendTimerId = null
          this.finalizeSend()
          return
        }

        this.sendProgress = this.sendProgress.map((item) =>
          item.channel === channel
            ? {
                ...item,
                status: channel === 'Messenger' ? 'FAILED' : 'SENT',
                progress: 100,
                errorMessage:
                  channel === 'Messenger'
                    ? 'Intégration Messenger indisponible'
                    : null,
              }
            : item
        )
        step += 1

        if (step >= channels.length) {
          clearInterval(this.sendTimerId)
          this.sendTimerId = null
          this.finalizeSend()
        }
      }, 700)
    },

    finalizeSend() {
      const messageRow = {
        id: generateUuid(),
        userId: this.currentDraft.userId,
        createdAt: new Date().toISOString(),
      }
      const document = {
        ...this.documents[this.currentDraft.id],
        sentAt: new Date().toISOString(),
        status: this.sendProgress.some((item) => item.status === 'FAILED') ? 'FAILED' : 'SENT',
      }

      this.messages.unshift(messageRow)
      this.history.unshift(document)
      this.currentDraft = createDraft(this.currentDraft.userId)
      this.documents[this.currentDraft.id] = { ...this.currentDraft }
      this.persist()
    },

    loadHistory() {
      return this.history
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
