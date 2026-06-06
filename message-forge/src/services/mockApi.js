export function generateUuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function createJwt(payload) {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const now = Math.floor(Date.now() / 1000)
  const body = btoa(JSON.stringify({ ...payload, iat: now, exp: now + 60 * 60 * 8 }))
  return `mock.${header}.${body}`
}

const applyDecorators = (text, decorators = {}) => {
  let formatted = text.trim()

  if (decorators.hashtag) {
    formatted += ' #Notify'
  }

  if (decorators.emoji) {
    formatted += ' 😊'
  }

  if (decorators.signature) {
    formatted += '\n\n— L’équipe Notify'
  }

  return formatted
}

const toParagraphs = (text, maxLength = 85) => {
  const words = text.split(/\s+/).filter(Boolean)
  const paragraphs = []
  let current = []
  let length = 0

  for (const word of words) {
    if (length + word.length + 1 > maxLength && current.length) {
      paragraphs.push(current.join(' '))
      current = []
      length = 0
    }
    current.push(word)
    length += word.length + 1
  }

  if (current.length) {
    paragraphs.push(current.join(' '))
  }

  return paragraphs.join('\n\n')
}

export function formatPreview(rawText, channel, decorators = {}) {
  const content = applyDecorators(rawText, decorators)
  const warnings = []
  const normalizedChannel = String(channel || '').trim()

  switch (normalizedChannel) {
    case 'Email': {
      const subject = content.slice(0, 60)
      const body = toParagraphs(content, 70)
      const formattedContent = `<header><h1>${subject}</h1></header><main><p>${body}</p></main><footer><p>${decorators.signature ? 'Cordialement,<br>Équipe Notify' : 'Notify'}</p></footer>`
      return {
        channel: 'Email',
        formattedContent,
        charCount: content.length,
        warnings,
      }
    }

    case 'SMS': {
      let formattedContent = content
      if (content.length > 155) {
        formattedContent = `${content.slice(0, 155)}[...]`
        warnings.push('SMS tronqué à 155 caractères')
      }
      return {
        channel: 'SMS',
        formattedContent,
        charCount: formattedContent.length,
        warnings,
      }
    }

    case 'LinkedIn': {
      const words = content.split(/\s+/).filter(Boolean)
      const headline = words.slice(0, 3).map((word) => word.toUpperCase()).join(' ')
      const body = words.slice(3).join(' ')
      const formattedContent = [headline, toParagraphs(body, 60)].filter(Boolean).join('\n\n')
      return {
        channel: 'LinkedIn',
        formattedContent,
        charCount: content.length,
        warnings,
      }
    }

    case 'Slack': {
      const formattedContent = content
        .replace(/\b(important|urgent|nouveau|note|action)\b/gi, '**$1**')
        .replace(/`([^`]+)`/g, '``$1``')
      return {
        channel: 'Slack',
        formattedContent,
        charCount: content.length,
        warnings,
      }
    }

    case 'X':
    case 'Twitter': {
      let formattedContent = content
      if (formattedContent.length > 270) {
        formattedContent = formattedContent.slice(0, 270)
        warnings.push('Limite X atteinte : 270 caractères')
      }
      return {
        channel: 'X',
        formattedContent,
        charCount: formattedContent.length,
        warnings,
      }
    }

    default: {
      return {
        channel: normalizedChannel || 'Canal inconnu',
        formattedContent: content,
        charCount: content.length,
        warnings,
      }
    }
  }
}
