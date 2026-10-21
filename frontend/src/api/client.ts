import type { ApiErrorResponse } from './types'

const TOKEN_KEY = 'ems.token'

export class ApiError extends Error {
  status: number
  code: string
  details?: string[]

  constructor(body: ApiErrorResponse) {
    super(body.message || body.error)
    this.name = 'ApiError'
    this.status = body.status
    this.code = body.code
    this.details = body.details
  }
}

type UnauthorizedHandler = () => void
type ForbiddenHandler = (message: string) => void

let onUnauthorized: UnauthorizedHandler | null = null
let onForbidden: ForbiddenHandler | null = null

export function setAuthHandlers(handlers: {
  onUnauthorized?: UnauthorizedHandler
  onForbidden?: ForbiddenHandler
}) {
  onUnauthorized = handlers.onUnauthorized ?? null
  onForbidden = handlers.onForbidden ?? null
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

function apiBase(): string {
  const url = import.meta.env.VITE_API_URL
  if (import.meta.env.DEV && !url) return ''
  return url || ''
}

export function isMockMode(): boolean {
  return import.meta.env.VITE_MOCK === 'true'
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit & { auth?: boolean } = {},
): Promise<T> {
  const { auth = true, ...init } = options
  const headers = new Headers(init.headers)
  if (!headers.has('Content-Type') && init.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (auth) {
    const token = getToken()
    if (token) headers.set('Authorization', `Bearer ${token}`)
  }

  const res = await fetch(`${apiBase()}${path}`, { ...init, headers })

  if (res.status === 204) {
    return undefined as T
  }

  const text = await res.text()
  let data: unknown = null
  if (text) {
    try {
      data = JSON.parse(text) as unknown
    } catch {
      throw new ApiError({
        timestamp: new Date().toISOString(),
        status: res.status,
        error: 'Bad Gateway',
        code: 'INVALID_JSON',
        message: 'Server returned invalid JSON',
        path,
      })
    }
  }

  if (!res.ok) {
    const errBody = (data ?? {
      status: res.status,
      error: res.statusText,
      code: 'UNKNOWN',
      message: res.statusText,
      path,
    }) as ApiErrorResponse

    if (res.status === 401) {
      onUnauthorized?.()
    }
    if (res.status === 403) {
      onForbidden?.(errBody.message)
    }
    throw new ApiError(errBody)
  }

  return data as T
}
