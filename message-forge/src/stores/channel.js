import { defineStore } from 'pinia'

export const useChannelStore = defineStore('channel', {
  state: () => ({
    availableChannels: ['Email', 'SMS', 'LinkedIn', 'Slack', 'X', 'Messenger', 'WhatsApp'],
  }),
  actions: {
    getChannelList() {
      return this.availableChannels
    },
  },
})
