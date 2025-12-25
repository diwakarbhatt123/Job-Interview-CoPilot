import { UnauthorizedError } from '@/error/UnauthorizedError'
import { apiFetchRaw } from '@/lib/api'
export default async function handler(req, res) {
  if (req.method !== 'POST') {
    res.setHeader('Allow', ['POST'])
    return res.status(405).json({ error: 'Method not allowed' })
  }

  try {
    const body = typeof req.body === 'string' ? JSON.parse(req.body) : req.body
    const upstream = await apiFetchRaw(`/accounts/auth/login`, {
      method: 'POST',
      body,
    })

    const setCookie = upstream.headers.get('set-cookie')
    if (setCookie) {
      res.setHeader('set-cookie', setCookie)
    }

    const contentType = upstream.headers.get('content-type') || ''
    const data = contentType.includes('application/json')
      ? await upstream.json()
      : await upstream.text()

    return res.status(upstream.status).json(data)
  } catch (error: unknown) {
    console.error(error)
    if (error instanceof UnauthorizedError) {
      res.status(401).json({ error: 'Unauthorized' })
    } else {
      const message =
        error instanceof Error ? error.message : 'Unexpected error'
      res.status(500).json({ error: message })
    }
  }
}
