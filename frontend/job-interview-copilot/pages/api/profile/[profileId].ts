import { NextApiRequest, NextApiResponse } from 'next'
import { apiFetchRaw } from '@/lib/api'
import { withAuthHeader } from '@/lib/api/withAuthHeader'

export default withAuthHeader(async function (
  req: NextApiRequest,
  res: NextApiResponse,
  authHeaders: Record<string, string>,
) {
  if (req.method !== 'GET') {
    res.setHeader('Allow', ['GET'])
    return res.status(405).json({ error: 'Method not allowed' })
  }

  const profileId =
    typeof req.query.profileId === 'string' ? req.query.profileId : undefined

  if (!profileId) {
    return res.status(400).json({ error: 'profileId is required' })
  }

  try {
    const upstream = await apiFetchRaw(`/profile/profile/${profileId}`, {
      method: 'GET',
      headers: authHeaders,
    })

    if (upstream.status === 401) {
      return res.status(401).json({ error: 'Unauthorized' })
    }

    if (!upstream.ok) {
      const contentType = upstream.headers.get('content-type') ?? ''
      const payload = contentType.includes('application/json')
        ? await upstream.json()
        : { error: await upstream.text() }
      return res.status(upstream.status).json(payload)
    }

    const contentType = upstream.headers.get('content-type') ?? ''
    const body = contentType.includes('application/json')
      ? await upstream.json()
      : await upstream.text()
    res.status(200).json(body)
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Unexpected error'
    res.status(500).json({ error: message })
  }
})
