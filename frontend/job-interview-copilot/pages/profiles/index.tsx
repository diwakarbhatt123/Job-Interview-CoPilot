import { useRouter } from 'next/router'
import { useEffect, useMemo, useState } from 'react'
import Link from 'next/link'

type Profile = {
  id: string
  displayName: string
  summary?: {
    yearsOfExperience?: number
    experienceLevel?: string
    skills?: string[]
    domain?: string
  }
}

function prettyExperience(level?: string) {
  if (!level) return ''
  const normalized = level
    .replace(/_LEVEL$/, '')
    .replace(/_/g, ' ')
    .toLowerCase()
  return normalized.charAt(0).toUpperCase() + normalized.slice(1)
}

function expLine(years?: number, level?: string) {
  const l = prettyExperience(level)
  const y = typeof years === 'number' ? `${years} yrs` : ''
  return [l, y].filter(Boolean).join(' • ')
}

export default function Profiles() {
  const router = useRouter()

  const [profiles, setProfiles] = useState<Profile[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [retryToken, setRetryToken] = useState(0)

  useEffect(() => {
    let cancelled = false

    ;(async () => {
      try {
        setError(null)
        const res = await fetch('/api/profile/all', {
          method: 'GET',
        })

        if (cancelled) return

        if (res.ok) {
          const json = await res.json()
          setProfiles(json.profiles ?? [])
        } else if (res.status === 401) {
          await router.push('/login')
        } else {
          setError('We couldn’t load your profiles. Please try again.')
        }
      } catch {
        if (!cancelled) {
          setError('We couldn’t load your profiles. Please try again.')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [router, retryToken])

  const subtitle = useMemo(() => {
    if (loading) return 'Loading profiles…'
    if (error) return 'Something went wrong.'
    if (!profiles.length) return 'No profiles found.'
    return profiles.length > 1
      ? 'Select a profile to continue'
      : 'Profile ready.'
  }, [error, loading, profiles.length])

  return (
    <section className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-4xl px-6 pb-16">
        <div className="space-y-4 p-6 text-center sm:p-8 md:space-y-6">
          <h1 className="text-3xl font-semibold tracking-tight md:text-4xl">
            Who’s using this?
          </h1>
          <p className="mt-2 text-sm text-gray-500/60 md:text-base">
            {subtitle}
          </p>
        </div>

        {loading ? (
          <div className="mt-8 flex flex-col items-center justify-center gap-3 text-gray-500">
            <div className="h-10 w-10 animate-spin rounded-full border-2 border-gray-300 border-t-black" />
            <span className="text-sm">Loading profiles…</span>
          </div>
        ) : error ? null : (
          <div className="mt-10 grid grid-cols-2 place-content-center justify-items-center gap-6 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
            {profiles.map((p) => {
              return (
                <Link
                  key={p.id}
                  href={`/profiles/${p.id}`}
                  className="group flex flex-col items-center"
                >
                  <div
                    className={[
                      'relative flex h-24 w-24 flex-col items-center justify-center',
                      'rounded-2xl bg-black',
                      'shadow-[0_10px_30px_rgba(0,0,0,0.45)]',
                      'ring-1 ring-white/15',
                      'transition-all duration-200 ease-out',
                      'group-hover:scale-[1.06] group-hover:ring-white/35',
                      'md:h-28 md:w-28',
                    ].join(' ')}
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="white"
                      strokeWidth="1.4"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className="h-14 w-14 opacity-90 md:h-16 md:w-16"
                    >
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                      <circle cx="12" cy="7" r="4" />
                    </svg>

                    {/* subtle focus outline for keyboard nav */}
                    <span className="pointer-events-none absolute inset-0 rounded-2xl ring-0 group-focus-visible:ring-4 group-focus-visible:ring-white/40" />
                  </div>

                  {/* Name */}
                  <div className="mt-3 max-w-40 truncate text-center text-sm font-medium text-black/85 group-hover:text-black">
                    {p.displayName}
                  </div>

                  {/* Senior • 8 yrs */}
                  <div className="mt-1 text-center text-xs text-black/55">
                    {expLine(
                      p.summary?.yearsOfExperience,
                      p.summary?.experienceLevel,
                    )}
                  </div>

                  {/* Skill chips (max 2) */}
                  {(p.summary?.skills?.length ?? 0) > 0 && (
                    <div className="mt-2 flex max-w-44 flex-wrap items-center justify-center gap-1.5">
                      {p.summary!.skills!.slice(0, 2).map((s) => (
                        <span
                          key={s}
                          className="rounded-full bg-black/10 px-2 py-0.5 text-[11px] text-black/75 ring-1 ring-black/10"
                        >
                          {s}
                        </span>
                      ))}
                      {(p.summary!.skills!.length ?? 0) > 2 && (
                        <span className="rounded-full bg-black/5 px-2 py-0.5 text-[11px] text-black/55 ring-1 ring-black/10">
                          +{p.summary!.skills!.length - 2}
                        </span>
                      )}
                    </div>
                  )}
                </Link>
              )
            })}
            <Link
              href="/profiles/new"
              className="group flex flex-col items-center"
            >
              <div
                className={[
                  'relative flex h-24 w-24 items-center justify-center rounded-2xl',
                  'bg-black ring-1 ring-white/15',
                  'shadow-[0_10px_30px_rgba(0,0,0,0.45)]',
                  'transition-all duration-200 ease-out',
                  'group-hover:scale-[1.06] group-hover:ring-white/35',
                  'md:h-28 md:w-28',
                ].join(' ')}
              >
                <span className="text-4xl font-light text-white/80">+</span>
              </div>

              <div className="mt-3 text-sm font-medium text-black/75 group-hover:text-black">
                Add Profile
              </div>
            </Link>
          </div>
        )}

        {!loading && error && (
          <div className="mt-8 text-center">
            <p className="text-sm text-gray-600">{error}</p>
            <button
              className="mt-4 rounded-lg bg-black px-4 py-2 text-sm font-semibold text-white hover:bg-black/90"
              onClick={() => {
                setLoading(true)
                setRetryToken((value) => value + 1)
              }}
            >
              Try again
            </button>
          </div>
        )}

        {!loading && !error && profiles.length === 0 && (
          <div className="mt-10 text-center">
            <button
              className="rounded-lg bg-white px-4 py-2 text-sm font-semibold text-black hover:bg-white/90"
              onClick={() => router.push('/profiles/new')}
            >
              Create your first profile
            </button>
          </div>
        )}
      </div>
    </section>
  )
}
