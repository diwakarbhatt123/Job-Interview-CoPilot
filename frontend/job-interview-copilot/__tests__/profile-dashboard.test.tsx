import { render, screen, waitFor } from '@testing-library/react'
import ProfileDashboard from '@/pages/profiles/[profileId]'

const push = jest.fn()

jest.mock('next/router', () => ({
  useRouter: () => ({
    push,
    reload: jest.fn(),
    isReady: true,
    query: { profileId: 'profile-1' },
  }),
}))

const mockFetch = (status: number, body: unknown) => {
  const response = {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as Response
  ;(global.fetch as jest.MockedFunction<typeof fetch>).mockResolvedValue(
    response,
  )
}

beforeEach(() => {
  push.mockReset()
  global.fetch = jest.fn()
})

it('redirects to login on 401', async () => {
  mockFetch(401, { error: 'Unauthorized' })

  render(<ProfileDashboard />)

  await waitFor(() => expect(push).toHaveBeenCalledWith('/login'))
})

it('redirects to switcher on 404', async () => {
  mockFetch(404, { error: 'Not found' })

  render(<ProfileDashboard />)

  await waitFor(() => expect(push).toHaveBeenCalledWith('/profiles'))
})

it('renders profile header on success', async () => {
  mockFetch(200, { id: 'profile-1', displayName: 'Primary', summary: {} })

  render(<ProfileDashboard />)

  expect(await screen.findByText('Primary')).toBeInTheDocument()
})

it('shows error message on server failure', async () => {
  mockFetch(500, { error: 'Server error' })

  render(<ProfileDashboard />)

  expect(
    await screen.findByText(/couldn’t load your profile/i),
  ).toBeInTheDocument()
})
