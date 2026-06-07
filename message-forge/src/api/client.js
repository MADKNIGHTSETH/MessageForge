const trimTrailingSlash = (value) => String(value || '').replace(/\/+$/, '')

export const API_BASE_URL = trimTrailingSlash(import.meta.env.VITE_API_BASE_URL || '/api')

let accessToken = null
let refreshToken = null

export class ApiError extends Error {
  constructor(message, { status = 0, body = null } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export function setApiTokens(tokens = {}) {
  accessToken = tokens.accessToken || null
  refreshToken = tokens.refreshToken || null
}

export function clearApiTokens() {
  accessToken = null
  refreshToken = null
}

export function getApiTokens() {
  return { accessToken, refreshToken }
}

const buildUrl = (path) => {
  if (/^https?:\/\//i.test(path)) {
    return path
  }

  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

const parseResponseBody = async (response) => {
  const text = await response.text()
  if (!text) {
    return null
  }

  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    try {
      return JSON.parse(text)
    } catch {
      return text
    }
  }

  return text
}

const getErrorMessage = (body, fallback) => {
  if (!body) {
    return fallback
  }

  if (typeof body === 'string') {
    return body
  }

  return body.message || body.error || body.detail || fallback
}

export async function apiRequest(path, options = {}) {
  const {
    method = 'GET',
    body,
    headers = {},
    auth = true,
    signal,
  } = options

  const requestHeaders = {
    Accept: 'application/json',
    ...headers,
  }

  if (body !== undefined && body !== null) {
    requestHeaders['Content-Type'] = requestHeaders['Content-Type'] || 'application/json'
  }

  if (auth && accessToken) {
    requestHeaders.Authorization = `Bearer ${accessToken}`
  }

  const response = await fetch(buildUrl(path), {
    method,
    headers: requestHeaders,
    body: body !== undefined && body !== null ? JSON.stringify(body) : undefined,
    signal,
  })

  const responseBody = await parseResponseBody(response)
  if (!response.ok) {
    throw new ApiError(
      getErrorMessage(responseBody, `API request failed with status ${response.status}`),
      { status: response.status, body: responseBody },
    )
  }

  return responseBody
}
