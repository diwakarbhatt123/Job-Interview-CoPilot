import handler from '@/pages/api/profile/new'
import { apiFetchRaw } from '@/lib/api'
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
  apiFetchRaw: jest.fn(),
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
  send: (data: unknown) => MockRes
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
    send: (data: unknown) => {
      res.body = data
      return res
    },
    setHeader: (name: string, value: string[]) => {
      res.headers[name] = value
    },
  }
  return res
}

it('rejects non-POST methods', async () => {
  const req = { method: 'GET' } as NextApiRequest
  const res = createRes()

  await handler(req, res as unknown as NextApiResponse)

  expect(res.statusCode).toBe(405)
  expect(res.headers.Allow).toEqual(['POST'])
})

it('proxies POST to profile service', async () => {
  ;(apiFetchRaw as jest.Mock).mockResolvedValue({
    status: 201,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => ({ id: 'profile-1' }),
  })

  const req = {
    method: 'POST',
    body: { displayName: 'Primary', pastedCV: 'resume' },
  } as NextApiRequest
  const res = createRes()

  await handler(req, res as unknown as NextApiResponse)

  expect(apiFetchRaw).toHaveBeenCalledWith(
    '/profile/profile',
    expect.objectContaining({
      method: 'POST',
      body: req.body,
      headers: { Authorization: 'Bearer token' },
    }),
  )
  expect(res.statusCode).toBe(201)
  expect(res.body).toEqual({ id: 'profile-1' })
})

it('returns 401 on UnauthorizedError', async () => {
  ;(apiFetchRaw as jest.Mock).mockRejectedValue(
    new UnauthorizedError('Unauthorized'),
  )

  const req = { method: 'POST', body: {} } as NextApiRequest
  const res = createRes()
  const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {})

  await handler(req, res as unknown as NextApiResponse)

  expect(res.statusCode).toBe(401)
  expect(res.body).toEqual({ error: 'Unauthorized' })
  consoleSpy.mockRestore()
})
