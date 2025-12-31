import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import NewProfile from '@/pages/profiles/new'

const push = jest.fn()
const replace = jest.fn()

jest.mock('next/router', () => ({
  useRouter: () => ({
    push,
    replace,
  }),
}))

beforeEach(() => {
  push.mockReset()
  replace.mockReset()
  global.fetch = jest.fn()
})

it('requires display name', async () => {
  render(<NewProfile />)

  fireEvent.click(screen.getByRole('button', { name: /create profile/i }))

  expect(
    await screen.findByText(/display name is required/i),
  ).toBeInTheDocument()
})

it('requires pasted CV in paste mode', async () => {
  render(<NewProfile />)

  fireEvent.change(screen.getByLabelText(/display name/i), {
    target: { value: 'Primary' },
  })

  fireEvent.click(screen.getByRole('button', { name: /create profile/i }))

  expect(await screen.findByText(/please paste your cv/i)).toBeInTheDocument()
})

it('clears pasted CV when switching to upload', () => {
  render(<NewProfile />)

  const textarea = screen.getByLabelText(/paste your cv/i)
  fireEvent.change(textarea, { target: { value: 'text' } })

  fireEvent.click(screen.getByRole('button', { name: /upload cv/i }))

  expect((textarea as HTMLTextAreaElement).value).toBe('')
})

it('redirects to login on 401', async () => {
  ;(global.fetch as jest.Mock).mockResolvedValue({
    ok: false,
    status: 401,
    json: async () => ({ error: 'Unauthorized' }),
  })

  render(<NewProfile />)

  fireEvent.change(screen.getByLabelText(/display name/i), {
    target: { value: 'Primary' },
  })
  fireEvent.change(screen.getByLabelText(/paste your cv/i), {
    target: { value: 'resume' },
  })

  fireEvent.click(screen.getByRole('button', { name: /create profile/i }))

  await waitFor(() => expect(push).toHaveBeenCalledWith('/login'))
})

it('shows error on non-PDF upload', async () => {
  render(<NewProfile />)

  fireEvent.click(screen.getByRole('button', { name: /upload cv/i }))

  const input = screen.getByLabelText(/click to upload/i) as HTMLInputElement
  const file = new File(['hello'], 'resume.txt', { type: 'text/plain' })
  fireEvent.change(input, { target: { files: [file] } })

  expect(
    await screen.findByText(/only pdf files are supported/i),
  ).toBeInTheDocument()
  expect(global.fetch).not.toHaveBeenCalled()
})

it('submits upload as FormData', async () => {
  ;(global.fetch as jest.Mock).mockResolvedValue({
    ok: true,
    status: 201,
    json: async () => ({ id: 'profile-1' }),
  })

  render(<NewProfile />)

  fireEvent.change(screen.getByLabelText(/display name/i), {
    target: { value: 'Primary' },
  })
  fireEvent.click(screen.getByRole('button', { name: /upload cv/i }))

  const input = document.querySelector(
    'input[name="cvFile"]',
  ) as HTMLInputElement
  const file = new File(['%PDF-1.4'], 'resume.pdf', { type: 'application/pdf' })
  Object.defineProperty(input, 'files', { value: [file] })
  Object.defineProperty(input, 'value', { value: 'resume.pdf' })
  fireEvent.change(input)
  expect(input.files?.[0]).toBe(file)

  fireEvent.click(screen.getByRole('button', { name: /create profile/i }))

  await waitFor(() => expect(global.fetch).toHaveBeenCalled())
  const [, options] = (global.fetch as jest.Mock).mock.calls[0]
  expect(options.method).toBe('POST')
  expect(options.body).toBeInstanceOf(FormData)
})
