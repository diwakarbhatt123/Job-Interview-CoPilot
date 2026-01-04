import handler from '@/pages/api/profile/[profileId]'
import { apiFetch } from '@/lib/api'
import { UnauthorizedError } from '@/error/UnauthorizedError'
import type { NextApiRequest, NextApiResponse } from 'next'

jest.mock('@/lib/api/withAuthHeader', () => ({
  withAuthHeader:
    (
      fn: (
        req: NextApiRequest,
        res: NextApiResponse,
        authHeaders: Record<string, string>,
      ) => Promise<void> | void,
    ) =>
    (req: NextApiRequest, res: NextApiResponse) =>
      fn(req, res, { Authorization: 'Bearer token' }),
}))

jest.mock('@/lib/api', () => ({
  apiFetch: jest.fn(),
}))

jest.mock('@/error/UnauthorizedError', () => ({
  UnauthorizedError: class UnauthorizedError extends Error {},
}))

type MockRes = {
  statusCode: number
  headers: Record<string, string[]>
  body?: unknown
  status: (code: number) => MockRes
  json: (data: unknown) => MockRes
  setHeader: (name: string, value: string[]) => void
}

function createRes(): MockRes {
  const res: MockRes = {
    statusCode: 200,
    headers: {},
    status: (code: number) => {
      res.statusCode = code
      return res
    },
    json: (data: unknown) => {
      res.body = data
      return res
    },
    setHeader: (name: string, value: string[]) => {
      res.headers[name] = value
    },
  }
  return res
}

it('rejects non-GET methods', async () => {
  const req = { method: 'POST' } as NextApiRequest
  const res = createRes()

  await handler(req, res as unknown as NextApiResponse)

  expect(res.statusCode).toBe(405)
  expect(res.headers.Allow).toEqual(['GET'])
})

it('returns 400 when profileId is missing', async () => {
  const req = { method: 'GET', query: {} } as NextApiRequest
  const res = createRes()

  await handler(req, res as unknown as NextApiResponse)

  expect(res.statusCode).toBe(400)
  expect(res.body).toEqual({ error: 'profileId is required' })
})

it('proxies GET to profile service', async () => {
  ;(apiFetch as jest.Mock).mockResolvedValue({ id: 'profile-1' })

  const req = {
    method: 'GET',
    query: { profileId: 'profile-1' },
  } as unknown as NextApiRequest
  const res = createRes()

  await handler(req, res as unknown as NextApiResponse)

  expect(apiFetch).toHaveBeenCalledWith('/profile/profile/profile-1', {
    method: 'GET',
    headers: { Authorization: 'Bearer token' },
  })
  expect(res.statusCode).toBe(200)
  expect(res.body).toEqual({ id: 'profile-1' })
})

it('returns 401 on UnauthorizedError', async () => {
  ;(apiFetch as jest.Mock).mockRejectedValue(
    new UnauthorizedError('Unauthorized'),
  )

  const req = {
    method: 'GET',
    query: { profileId: 'profile-1' },
  } as unknown as NextApiRequest
  const res = createRes()

  await handler(req, res as unknown as NextApiResponse)

  expect(res.statusCode).toBe(401)
  expect(res.body).toEqual({ error: 'Unauthorized' })
})
