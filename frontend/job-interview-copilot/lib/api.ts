import {UnauthorizedError} from "@/error/UnauthorizedError";

function getBaseUrl(): string {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL
  if (!baseUrl) {
    throw new Error('NEXT_PUBLIC_API_BASE_URL is not defined')
  }
  return baseUrl
}

type ApiOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  headers?: Record<string, string>
  body?: unknown
}

export async function apiFetch<T = unknown>(
  path: string,
  options: ApiOptions = {},
): Promise<T> {
  const baseUrl = getBaseUrl()
  const url = `${baseUrl}${path.startsWith('/') ? path : `/${path}`}`

  const res = await fetch(url, {
    method: options.method ?? 'GET',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  })

  if (!res.ok) {
    if (res.status === 401) {
      throw new UnauthorizedError('Unauthorized access - 401 Unauthorized')
    } else {
      const text = await res.text()
      throw new Error(`API error ${res.status}: ${text}`)
    }
  }

  // Some endpoints (like /healthz) may return plain text
  const contentType = res.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    return res.json()
  }

  return (await res.text()) as T
}

export async function apiFetchRaw(
  path: string,
  options: ApiOptions = {},
): Promise<Response> {
  const baseUrl = getBaseUrl()
  const url = `${baseUrl}${path.startsWith('/') ? path : `/${path}`}`

  return fetch(url, {
    method: options.method ?? 'GET',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  })
}
