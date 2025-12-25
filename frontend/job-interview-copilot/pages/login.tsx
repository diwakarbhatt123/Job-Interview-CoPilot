import Link from 'next/link'
import { FormEvent, useState } from 'react'
import { useRouter } from 'next/router'

export default function Login() {
  const router = useRouter()
  const next = (router.query.next as string) || '/'

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    setError(null)
    setLoading(true)

    event.preventDefault()

    try {
      const formData = new FormData(event.currentTarget)

      const body = JSON.stringify({
        email: formData.get('email'),
        password: formData.get('password'),
      })

      const response = await fetch('/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: body,
      })

      if (response.ok) {
        await router.replace(next)
        return
      }

      if (response.status === 401) {
        setError('Invalid email or password.')
      } else {
        setError(`Something went Wrong! Please try again.`)
      }
    } catch (e) {
      setError('Network error. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="bg-gray-50">
      <div className="mx-auto flex flex-col items-center justify-center px-6 py-8 md:h-screen lg:py-0">
        <div className="w-full rounded-lg bg-white shadow sm:max-w-md md:mt-0 xl:p-0">
          <div className="space-y-4 p-6 sm:p-8 md:space-y-6">
            <h1 className="text-center text-3xl leading-tight font-bold tracking-normal text-gray-900 md:text-3xl">
              Sign in
            </h1>
            <h2 className="text-center leading-tight tracking-tight text-gray-500">
              Access your profiles, job analysis, and preparation plans
            </h2>
            <form className="space-y-4 md:space-y-6" onSubmit={onSubmit}>
              <div>
                <label
                  htmlFor="email"
                  className="mb-2 block text-sm font-medium text-gray-900"
                >
                  Email
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  placeholder="name@company.com"
                  required
                  className="focus:ring-primary-600 focus:border-primary-600 block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-gray-900"
                />
              </div>
              <div>
                <label
                  htmlFor="password"
                  className="mb-2 block text-sm font-medium text-gray-900"
                >
                  Password
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  placeholder="••••••••"
                  required
                  className="focus:ring-primary-600 focus:border-primary-600 block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-gray-900"
                />
              </div>
              {error && (
                <p className="text-sm font-light text-red-700">{error}</p>
              )}
              <button
                type="submit"
                disabled={loading}
                className={`focus:ring-primary-300 flex w-full items-center justify-center gap-2 rounded-lg bg-black px-5 py-2.5 text-center text-sm font-medium text-white hover:bg-gray-800 focus:ring-4 focus:outline-none ${
                  loading ? 'cursor-not-allowed opacity-70' : ''
                }`}
              >
                {loading && (
                  <svg
                    aria-hidden="true"
                    className="h-4 w-4 animate-spin"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                      fill="none"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                    />
                  </svg>
                )}
                {loading ? 'Signing in...' : 'Login'}
              </button>
              <p className="text-center text-sm font-light text-gray-500">
                New here?{' '}
                <Link
                  href={'/register'}
                  className="text-center font-medium text-blue-600 hover:underline"
                >
                  Create an account
                </Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </section>
  )
}
