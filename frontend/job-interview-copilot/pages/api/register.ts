import type { NextApiRequest, NextApiResponse } from 'next'
import { apiFetchRaw } from '@/lib/api'

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse
) {
  if (req.method !== 'POST') {
    res.setHeader('Allow', ['POST'])
    return res.status(405).json({ error: 'Method not allowed' })
  }

  try {
    const body = typeof req.body === 'string' ? JSON.parse(req.body) : req.body
    const upstream = await apiFetchRaw(`/accounts/auth/register`, {
      method: 'POST',
      body,
    })

    const contentType = upstream.headers.get('content-type') || ''
    const data = contentType.includes('application/json')
      ? await upstream.json()
      : await upstream.text()

    if (data === '') {
      return res.status(upstream.status).end()
    }

    return res.status(upstream.status).json(data)
  } catch (error: unknown) {
    console.error(error)
    const message = error instanceof Error ? error.message : 'Unexpected error'
    res.status(500).json({ error: message })
  }
}
