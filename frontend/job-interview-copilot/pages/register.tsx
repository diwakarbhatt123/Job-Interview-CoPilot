import Link from 'next/link'
import { useRouter } from 'next/router'
import { FormEvent, useState } from 'react'

export default function RegisterPage() {
  const router = useRouter()
  const next = (router.query.next as string) || '/login'

  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [password, setPassword] = useState<string>('')

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    setError(null)
    setLoading(true)

    event.preventDefault()
    const formData = new FormData(event.currentTarget)

    const body = JSON.stringify({
      email: formData.get('email'),
      password: formData.get('password'),
    })

    try {
      const response = await fetch('/api/register', {
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

      if (response.status === 409) {
        setError('Email already exists. Please use a different email.')
      } else if (response.status === 400) {
        setError('Invalid input. Please check your email and password.')
      } else {
        setError('Something went Wrong! Please try again.')
      }
    } catch {
      setError('Network error. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  async function onChange(value: string) {
    if (password !== value) {
      setError('Passwords do not match')
    } else {
      setError(null)
    }
  }

  return (
    <section className="bg-gray-50">
      <div className="mx-auto flex flex-col items-center justify-center px-6 py-8 md:h-screen lg:py-0">
        <div className="w-full rounded-lg bg-white shadow sm:max-w-md md:mt-0 xl:p-0">
          <div className="space-y-4 p-6 sm:p-8 md:space-y-6">
            <h1 className="text-center text-3xl leading-tight font-bold tracking-normal text-gray-900 md:text-3xl">
              Register
            </h1>
            <h2 className="text-center leading-tight tracking-tight text-gray-500">
              Get started with job analysis and interview preparation
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
                  onChange={(event) => setPassword(event.target.value)}
                />
              </div>
              <div>
                <label
                  htmlFor="confirmPassword"
                  className="mb-2 block text-sm font-medium text-gray-900"
                >
                  Confirm Password
                </label>
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  placeholder="••••••••"
                  required
                  className="focus:ring-primary-600 focus:border-primary-600 block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-gray-900"
                  onChange={(event) => onChange(event.target.value)}
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
                {loading ? 'Registering...' : 'Register'}
              </button>
              <p className="text-center text-sm font-light text-gray-500">
                Already have an account?{' '}
                <Link
                  href={'/login'}
                  className="text-center font-medium text-blue-600 hover:underline"
                >
                  Sign in
                </Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </section>
  )

  // return (
  //   <section className='bg-gray-50'>
  //     <div
  //       className='flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0'>
  //       <div className='w-full bg-white rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0'>
  //         <div className='p-6 space-y-4 md:space-y-6 sm:p-8'>
  //           <h1
  //             className='text-3xl font-bold text-center leading-tight tracking-normal text-gray-900 md:text-3xl'>Register</h1>
  //           <h2 className='text-center leading-tight tracking-tight text-gray-500'>Get started with
  //             job analysis and interview preparation </h2>
  //         </div>
  //         <form className='space-y-4 md:space-y-6' onSubmit={onSubmit}>
  //           <div>
  //             <label htmlFor='email'
  //                    className='block mb-2 text-sm font-medium text-gray-900'>Email</label>
  //             <input type="email" id="email" name="email" placeholder="name@company.com" required
  //                    className='bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5'/>
  //           </div>
  //           <div>
  //             <label htmlFor='password'
  //                    className='block mb-2 text-sm font-medium text-gray-900'>Password</label>
  //             <input type="password" id="password" name="password" placeholder="••••••••" required
  //                    className='bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5'
  //                    onChange={(e) => setPassword(e.target.value)}/>
  //           </div>
  //           <div>
  //             <label htmlFor='confirmPassword'
  //                    className='block mb-2 text-sm font-medium text-gray-900'>Confirm
  //               Password</label>
  //             <input type="password" id="confirmPassword" name="confirmPassword"
  //                    placeholder="••••••••" required
  //                    className='bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5'
  //                    onChange={event => onChange(event.target.value)}/>
  //           </div>
  //           {error && <p className='text-sm font-light text-red-700'>{error}</p>}
  //           <button
  //             type="submit"
  //             disabled={loading}
  //             className={`w-full flex items-center justify-center gap-2 text-white bg-black hover:bg-gray-800 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center ${
  //               loading ? "opacity-70 cursor-not-allowed" : ""
  //             }`}
  //           >
  //             {loading && (
  //               <svg
  //                 aria-hidden="true"
  //                 className="w-4 h-4 animate-spin"
  //                 viewBox="0 0 24 24"
  //               >
  //                 <circle
  //                   className="opacity-25"
  //                   cx="12"
  //                   cy="12"
  //                   r="10"
  //                   stroke="currentColor"
  //                   strokeWidth="4"
  //                   fill="none"
  //                 />
  //                 <path
  //                   className="opacity-75"
  //                   fill="currentColor"
  //                   d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
  //                 />
  //               </svg>
  //             )}
  //             {loading ? "Registering..." : "Register"}
  //           </button>
  //           <p className='text-sm text-center font-light text-gray-500'>Already have an account?</p>
  //           <Link href={'/login'} className='font-medium text-center text-blue-600 hover:underline'>Sign
  //             in</Link>
  //         </form>
  //       </div>
  //     </div>
  //   </section>
  // )
}
