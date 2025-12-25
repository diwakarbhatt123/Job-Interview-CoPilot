import type { NextApiRequest, NextApiResponse } from 'next'

const AUTH_COOKIE_NAME = 'AuthToken'

export type AuthHeaderHandler = (
  req: NextApiRequest,
  res: NextApiResponse,
  authHeaders: Record<string, string>,
) => Promise<void> | void

export function withAuthHeader(handler: AuthHeaderHandler) {
  return async function wrapped(req: NextApiRequest, res: NextApiResponse) {
    const authHeaders = getAuthHeader(req)
    return handler(req, res, authHeaders)
  }
}

function getAuthHeader(req: NextApiRequest): Record<string, string> {
  const token = getCookieValue(req.headers.cookie, AUTH_COOKIE_NAME)
  if (!token) {
    return {}
  }
  return { Authorization: `Bearer ${token}` }
}

function getCookieValue(
  cookieHeader: string | undefined,
  name: string,
): string | null {
  if (!cookieHeader) {
    return null
  }

  const cookies = cookieHeader.split(';')
  for (const cookie of cookies) {
    const [key, ...valueParts] = cookie.trim().split('=')
    if (key === name) {
      return decodeURIComponent(valueParts.join('='))
    }
  }

  return null
}
