import { apiRequest } from './client'

export const channelsApi = {
  listChannels() {
    return apiRequest('/channels')
  },

  getRules(channel) {
    return apiRequest(`/channels/${encodeURIComponent(channel)}/rules`)
  },

  listIntegrations() {
    return apiRequest('/integrations')
  },

  configureIntegration(channel, payload) {
    return apiRequest(`/integrations/${encodeURIComponent(channel)}`, {
      method: 'PUT',
      body: payload,
    })
  },

  deleteIntegration(channel) {
    return apiRequest(`/integrations/${encodeURIComponent(channel)}`, {
      method: 'DELETE',
    })
  },

  testIntegration(channel) {
    return apiRequest(`/integrations/${encodeURIComponent(channel)}/test`, {
      method: 'POST',
    })
  },
}
