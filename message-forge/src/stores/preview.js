import { defineStore } from 'pinia'
import { formatPreview } from '../services/mockApi'

export const usePreviewStore = defineStore('preview', {
  state: () => ({
    previews: [],
    isLoading: false,
    debounceId: null,
  }),
  actions: {
    requestPreview(rawText, activeChannels = [], decorators = {}) {
      if (!rawText || !activeChannels.length) {
        this.previews = []
        this.isLoading = false
        return
      }

      if (this.debounceId) {
        clearTimeout(this.debounceId)
      }

      this.isLoading = true
      this.debounceId = window.setTimeout(() => {
        this.previews = activeChannels.map((channel) => formatPreview(rawText, channel, decorators))
        this.isLoading = false
        this.debounceId = null
      }, 220)
    },

    clear() {
      if (this.debounceId) {
        clearTimeout(this.debounceId)
        this.debounceId = null
      }
      this.previews = []
      this.isLoading = false
    },
  },
})
