import { defineStore } from 'pinia'
import { messagesApi } from '../api/messages'
import { mapPreviewFromApi, toApiChannel } from '../api/mappers'

export const usePreviewStore = defineStore('preview', {
  state: () => ({
    previews: [],
    isLoading: false,
    error: '',
    debounceId: null,
    requestId: 0,
  }),

  actions: {
    requestPreview(rawText, activeChannels = [], decorators = {}) {
      if (!rawText || !activeChannels.length) {
        this.previews = []
        this.error = ''
        this.isLoading = false
        return
      }

      if (this.debounceId) {
        clearTimeout(this.debounceId)
      }

      const requestId = this.requestId + 1
      this.requestId = requestId
      this.isLoading = true
      this.error = ''

      this.debounceId = window.setTimeout(async () => {
        try {
          const previews = await messagesApi.preview({
            rawContent: rawText,
            activeChannels: activeChannels.map(toApiChannel),
            decorators,
          })

          if (requestId === this.requestId) {
            this.previews = previews.map(mapPreviewFromApi)
          }
        } catch (error) {
          if (requestId === this.requestId) {
            this.previews = []
            this.error = error.message || 'Impossible de generer la previsualisation.'
          }
        } finally {
          if (requestId === this.requestId) {
            this.isLoading = false
            this.debounceId = null
          }
        }
      }, 220)
    },

    clear() {
      if (this.debounceId) {
        clearTimeout(this.debounceId)
        this.debounceId = null
      }
      this.requestId += 1
      this.previews = []
      this.error = ''
      this.isLoading = false
    },
  },
})
