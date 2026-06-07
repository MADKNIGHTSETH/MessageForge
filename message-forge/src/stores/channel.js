import { defineStore } from 'pinia'
import { channelsApi } from '../api/channels'
import { mapApiChannel } from '../api/mappers'

const DEFAULT_CHANNELS = ['Email', 'SMS', 'LinkedIn', 'Slack', 'X', 'Messenger', 'WhatsApp']

export const useChannelStore = defineStore('channel', {
  state: () => ({
    channels: [],
    availableChannels: DEFAULT_CHANNELS,
    isLoading: false,
    error: '',
  }),

  actions: {
    async fetchChannels() {
      this.isLoading = true
      this.error = ''

      try {
        const channels = await channelsApi.listChannels()
        this.channels = channels.map(mapApiChannel)
        this.availableChannels = this.channels.map((channel) => channel.label)
        return this.channels
      } catch (error) {
        this.error = error.message || 'Impossible de charger les canaux.'
        return this.channels
      } finally {
        this.isLoading = false
      }
    },

    getChannelList() {
      return this.availableChannels
    },
  },
})
