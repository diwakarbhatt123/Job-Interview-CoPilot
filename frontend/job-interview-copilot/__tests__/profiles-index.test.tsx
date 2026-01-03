import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import Profiles from '@/pages/profiles'

const push = jest.fn()

jest.mock('next/router', () => ({
  useRouter: () => ({
    push,
    reload: jest.fn(),
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

  render(<Profiles />)

  await waitFor(() => expect(push).toHaveBeenCalledWith('/login'))
})

it('shows error message on non-401 failures', async () => {
  mockFetch(500, { error: 'Server error' })

  render(<Profiles />)

  expect(
    await screen.findByText(/couldn’t load your profiles/i),
  ).toBeInTheDocument()
})

it('retries fetch when clicking try again', async () => {
  mockFetch(500, { error: 'Server error' })

  render(<Profiles />)

  await screen.findByText(/couldn’t load your profiles/i)
  ;(global.fetch as jest.Mock).mockClear()
  fireEvent.click(screen.getByRole('button', { name: /try again/i }))

  await waitFor(() => expect(global.fetch).toHaveBeenCalled())
})

it('renders profiles when fetch succeeds', async () => {
  mockFetch(200, {
    profiles: [
      { id: 'p1', displayName: 'Primary', summary: { yearsOfExperience: 5 } },
      { id: 'p2', displayName: 'Secondary', summary: { yearsOfExperience: 2 } },
    ],
  })

  render(<Profiles />)

  expect(await screen.findByText('Primary')).toBeInTheDocument()
  expect(screen.getByText('Secondary')).toBeInTheDocument()
  expect(screen.getByRole('link', { name: /add profile/i })).toBeInTheDocument()
})
