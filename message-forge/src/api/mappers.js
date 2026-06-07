const DEFAULT_DECORATORS = {
  emoji: true,
  hashtag: false,
  signature: true,
}

export const API_TO_UI_CHANNEL = {
  EMAIL: 'Email',
  SMS: 'SMS',
  FACEBOOK: 'Messenger',
  WHATSAPP: 'WhatsApp',
  LINKEDIN: 'LinkedIn',
  SLACK: 'Slack',
  TWITTER: 'X',
  TELEGRAM: 'Telegram',
}

export const UI_TO_API_CHANNEL = Object.fromEntries(
  Object.entries(API_TO_UI_CHANNEL).map(([apiName, uiName]) => [uiName, apiName]),
)

const CHANNEL_ALIASES = {
  email: 'Email',
  sms: 'SMS',
  linkedin: 'LinkedIn',
  slack: 'Slack',
  x: 'X',
  twitter: 'X',
  'twitter/x': 'X',
  facebook: 'Messenger',
  messenger: 'Messenger',
  'facebook messenger': 'Messenger',
  whatsapp: 'WhatsApp',
  telegram: 'Telegram',
}

export function normalizeChannel(channel) {
  const raw = String(channel || '').trim()
  if (!raw) {
    return ''
  }

  const apiName = raw.toUpperCase()
  if (API_TO_UI_CHANNEL[apiName]) {
    return API_TO_UI_CHANNEL[apiName]
  }

  return CHANNEL_ALIASES[raw.toLowerCase()] || raw
}

export function toApiChannel(channel) {
  const normalized = normalizeChannel(channel)
  return UI_TO_API_CHANNEL[normalized] || String(channel || '').trim().toUpperCase()
}

export function fromApiChannel(channel) {
  return normalizeChannel(channel)
}

export function mapApiChannel(channel) {
  const label = fromApiChannel(channel.name)
  return {
    ...channel,
    apiName: channel.name,
    key: label,
    label,
    displayName: channel.displayName || label,
  }
}

export function integrationToAccount(integration) {
  const type = fromApiChannel(integration.channelType)
  const settings = integration.settings || {}

  return {
    id: integration.channelType,
    type,
    label: settings.label || integration.displayName || type,
    value:
      settings.value ||
      settings.from ||
      settings.recipient ||
      settings.username ||
      integration.displayName ||
      type,
    enabled: integration.enabled,
    testStatus: integration.testStatus,
    settings,
  }
}

export function accountToIntegrationPayload(account) {
  return {
    enabled: true,
    credentials: {
      apiKey: account.apiKey || account.password || account.value || 'ui-configured',
      recipient: account.value || '',
      testRecipient: account.value || '',
    },
    settings: {
      label: account.label || account.type,
      value: account.value || '',
    },
  }
}

export function mapPreviewFromApi(preview) {
  const warning = preview.warning ? [preview.warning] : []

  return {
    channel: fromApiChannel(preview.channelType),
    formattedContent: preview.formattedContent || '',
    charCount: preview.characterCount ?? preview.formattedContent?.length ?? 0,
    characterLimit: preview.characterLimit,
    warnings: warning,
    requiresTruncation: preview.requiresTruncation,
  }
}

export function mapDraftToApiPayload(draft) {
  const subject = draft.subject || draft.title || ''

  return {
    title: subject.trim() || 'Untitled Message',
    rawContent: draft.rawContent || '',
    metadata: {
      ...(draft.metadata || {}),
      subject,
      activeChannels: draft.activeChannels || [],
      selectedAccounts: draft.selectedAccounts || {},
      recipients: draft.recipients || [],
      decorators: {
        ...DEFAULT_DECORATORS,
        ...(draft.decorators || {}),
      },
    },
  }
}

const channelsFromMessage = (message) => {
  const metadataChannels = message.metadata?.activeChannels
  if (Array.isArray(metadataChannels) && metadataChannels.length > 0) {
    return metadataChannels.map(normalizeChannel).filter(Boolean)
  }

  return (message.channelMessages || [])
    .map((channelMessage) => fromApiChannel(channelMessage.channelType))
    .filter(Boolean)
}

export function mapApiMessageToDraft(message, userId = 'api-user') {
  const metadata = message.metadata || {}

  return {
    id: message.id,
    remoteId: message.id,
    userId,
    subject: metadata.subject || message.title || '',
    rawContent: message.rawContent || '',
    recipients: metadata.recipients || [],
    activeChannels: channelsFromMessage(message),
    selectedAccounts: metadata.selectedAccounts || {},
    decorators: {
      ...DEFAULT_DECORATORS,
      ...(metadata.decorators || {}),
    },
    metadata: {
      createdAt: message.createdAt,
      updatedAt: message.updatedAt || message.createdAt,
    },
    status: message.status || 'DRAFT',
  }
}

export function mapApiMessageToHistory(message) {
  const metadata = message.metadata || {}

  return {
    id: message.id,
    userId: message.userId || 'api-user',
    subject: metadata.subject || message.title || '',
    rawContent: message.rawContent || '',
    activeChannels: channelsFromMessage(message),
    selectedAccounts: metadata.selectedAccounts || {},
    decorators: metadata.decorators || {},
    metadata: {
      createdAt: message.createdAt,
      updatedAt: message.updatedAt || message.createdAt,
    },
    createdAt: message.createdAt,
    sentAt: message.sentAt || message.createdAt,
    status: message.status || 'DRAFT',
    channelMessages: message.channelMessages || [],
  }
}

export function mapChannelMessagesToProgress(channelMessages = [], fallbackChannels = []) {
  const rows = channelMessages.length
    ? channelMessages
    : fallbackChannels.map((channel) => ({
        channelType: toApiChannel(channel),
        status: 'PENDING',
      }))

  return rows.map((item) => {
    const status = item.status || 'PENDING'
    const isDone = status === 'SENT' || status === 'FAILED'

    return {
      channel: fromApiChannel(item.channelType || item.channel),
      status,
      progress: isDone ? 100 : status === 'SENDING' ? 65 : 25,
      errorMessage: item.errorMessage || null,
    }
  })
}
