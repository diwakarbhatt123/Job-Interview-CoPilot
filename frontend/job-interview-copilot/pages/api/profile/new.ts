import { withAuthHeader } from '@/lib/api/withAuthHeader'
import { NextApiRequest, NextApiResponse } from 'next'
import { apiFetchRaw } from '@/lib/api'
import { UnauthorizedError } from '@/error/UnauthorizedError'

export default withAuthHeader(async function (
  req: NextApiRequest,
  res: NextApiResponse,
  authHeaders: Record<string, string>,
) {
  if (req.method !== 'POST') {
    res.setHeader('Allow', ['POST'])
    return res.status(405).json({ error: 'Method not allowed' })
  }

  try {
    const body = typeof req.body === 'string' ? JSON.parse(req.body) : req.body
    const upstream = await apiFetchRaw(`/profile/profile`, {
      method: 'POST',
      body,
      headers: authHeaders,
    })

    const contentType = upstream.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      const data = await upstream.json()
      return res.status(upstream.status).json(data)
    }

    const text = await upstream.text()
    return res.status(upstream.status).send(text)
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
})
