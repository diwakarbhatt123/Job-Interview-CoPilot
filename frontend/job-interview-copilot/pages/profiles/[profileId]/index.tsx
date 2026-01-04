import { useRouter } from 'next/router'
import { ReactNode, useEffect, useMemo, useState } from 'react'
import { Profile } from '@/types/Profile'

type AnalysedJob = {
  id: string
  title: string
  fitScore: number
  updatedAt: string
}

type Application = {
  id: string
  company: string
  role: string
  status: 'Applied' | 'Interview' | 'Offer' | 'Rejected'
  updatedAt: string
}

type PrepPlan = {
  id: string
  title: string
  completionPct: number
  status: 'Active' | 'Paused' | 'Done'
  updatedAt: string
}

type ActivityEvent = {
  id: string
  type: 'job' | 'application' | 'plan'
  title: string
  meta: string
  at: string
}

function prettyExperience(level?: string) {
  if (!level) return ''
  const normalized = level
    .replace(/_LEVEL$/, '')
    .replace(/_/g, ' ')
    .toLowerCase()
  return normalized.charAt(0).toUpperCase() + normalized.slice(1)
}

function formatDate(iso: string) {
  const d = new Date(iso)
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

function clamp(n: number, min = 0, max = 100) {
  return Math.max(min, Math.min(max, n))
}

function SkeletonLine({ w = 'w-40' }: { w?: string }) {
  return <div className={`h-4 ${w} animate-pulse rounded bg-black/10`} />
}

function SkeletonCard() {
  return (
    <div className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-black/5">
      <div className="space-y-3">
        <div className="h-5 w-48 animate-pulse rounded bg-black/10" />
        <div className="h-4 w-28 animate-pulse rounded bg-black/10" />
        <div className="h-4 w-32 animate-pulse rounded bg-black/10" />
      </div>
    </div>
  )
}

function EmptyState({
  title,
  description,
  ctaLabel,
  onCta,
}: {
  title: string
  description: string
  ctaLabel: string
  onCta: () => void
}) {
  return (
    <div className="rounded-xl bg-white p-6 text-center shadow-sm ring-1 ring-black/5">
      <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-black text-white">
        <span className="text-lg leading-none">+</span>
      </div>
      <div className="text-sm font-semibold text-gray-900">{title}</div>
      <div className="mt-1 text-sm text-gray-500">{description}</div>
      <button
        onClick={onCta}
        className="mt-4 inline-flex items-center justify-center rounded-lg bg-black px-4 py-2 text-sm font-medium text-white hover:bg-gray-900"
        type="button"
      >
        {ctaLabel}
      </button>
    </div>
  )
}

function Section({
  title,
  rightAction,
  children,
}: {
  title: string
  rightAction?: ReactNode
  children: ReactNode
}) {
  return (
    <section>
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-sm font-semibold text-gray-900">{title}</h2>
        {rightAction}
      </div>
      {children}
    </section>
  )
}

function ProgressBar({ value }: { value: number }) {
  const v = clamp(value)
  return (
    <div className="mt-3 h-2 w-full rounded-full bg-black/10">
      <div className="h-2 rounded-full bg-black" style={{ width: `${v}%` }} />
    </div>
  )
}

function TimelineDot({ type }: { type: ActivityEvent['type'] }) {
  const label = type === 'job' ? 'J' : type === 'application' ? 'A' : 'P'
  return (
    <div className="flex h-7 w-7 items-center justify-center rounded-full bg-black text-xs font-semibold text-white">
      {label}
    </div>
  )
}

export default function ProfileDashboard() {
  const router = useRouter()

  const [profile, setProfile] = useState<Profile>()

  const [analysedJobs, setAnalysedJobs] = useState<AnalysedJob[]>([])
  const [applications, setApplications] = useState<Application[]>([])
  const [prepPlans, setPrepPlans] = useState<PrepPlan[]>([])

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const profileId =
    typeof router.query.profileId === 'string'
      ? router.query.profileId
      : undefined

  useEffect(() => {
    if (!router.isReady || !profileId) return

    let cancelled = false

    ;(async () => {
      setLoading(true)
      setError(null)

      try {
        const res = await fetch(`/api/profile/${profileId}`, { method: 'GET' })

        if (cancelled) return

        if (res.ok) {
          const json = await res.json()
          setProfile(json ?? null)
        } else if (res.status === 401) {
          await router.push('/login')
        } else if (res.status === 404) {
          await router.push('/profiles')
        } else {
          setError('We couldn’t load your profile. Please try again.')
        }
      } catch {
        if (!cancelled)
          setError('We couldn’t load your profile. Please try again.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [profileId, router])

  const experienceLine = useMemo(() => {
    if (!profile?.summary) return ''
    const level = prettyExperience(profile.summary.experienceLevel)
    const years =
      typeof profile.summary.yearsOfExperience === 'number'
        ? `${profile.summary.yearsOfExperience} yrs`
        : ''
    return [level, years].filter(Boolean).join(' • ')
  }, [profile])

  const activity: ActivityEvent[] = useMemo(() => {
    const events: ActivityEvent[] = []

    return events.sort((a, b) => +new Date(b.at) - +new Date(a.at)).slice(0, 8)
  }, [])

  return (
    <section className="min-h-screen bg-gray-50">
      <div className="border-b bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
          <div>
            {loading ? (
              <div className="space-y-2">
                <div className="h-7 w-64 animate-pulse rounded bg-black/10" />
                <div className="h-4 w-40 animate-pulse rounded bg-black/10" />
              </div>
            ) : (
              <>
                <h1 className="text-xl font-semibold text-gray-900">
                  {profile?.displayName ?? 'Your profile'}
                </h1>
                <p className="mt-1 text-sm text-gray-500">{experienceLine}</p>
              </>
            )}

            {error && (
              <div className="mt-2 text-sm text-red-600">
                <p>{error}</p>
                <div className="mt-2 flex flex-wrap gap-3 text-sm">
                  <button
                    className="underline"
                    type="button"
                    onClick={() => router.reload()}
                  >
                    Retry
                  </button>
                  <button
                    className="underline"
                    type="button"
                    onClick={() => router.push('/profiles')}
                  >
                    Back to profiles
                  </button>
                </div>
              </div>
            )}
          </div>

          <button
            type="button"
            className="text-sm font-medium text-blue-600 hover:underline"
            onClick={() => router.push('/profiles')}
          >
            Switch profile
          </button>
        </div>
      </div>

      <div className="mx-auto grid max-w-6xl grid-cols-1 gap-8 px-6 py-8 lg:grid-cols-3">
        <div className="space-y-8 lg:col-span-2">
          <div className="flex items-center justify-between">
            <button
              type="button"
              className="inline-flex items-center gap-2 rounded-lg bg-black px-5 py-3 text-sm font-medium text-white hover:bg-gray-900"
              onClick={() => router.push('/jobs/new')}
            >
              + Analyse new job
            </button>
          </div>

          <Section
            title="Analysed jobs"
            rightAction={
              analysedJobs.length > 0 ? (
                <button
                  className="text-sm text-blue-600 hover:underline"
                  onClick={() => router.push('/jobs')}
                  type="button"
                >
                  View all
                </button>
              ) : null
            }
          >
            {loading ? (
              <div className="grid gap-4 sm:grid-cols-2">
                <SkeletonCard />
                <SkeletonCard />
              </div>
            ) : analysedJobs.length === 0 ? (
              <EmptyState
                title="No analysed jobs yet"
                description="Analyse a job description to get a fit score and gaps."
                ctaLabel="Analyse a job"
                onCta={() => router.push('/jobs/new')}
              />
            ) : (
              <div className="grid gap-4 sm:grid-cols-2">
                {analysedJobs.map((j) => (
                  <div
                    key={j.id}
                    className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-black/5"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="font-medium text-gray-900">
                          {j.title}
                        </div>
                        <div className="mt-1 text-sm text-gray-500">
                          Fit score: {clamp(j.fitScore)}%
                        </div>
                      </div>
                      <div className="text-xs text-gray-400">
                        {formatDate(j.updatedAt)}
                      </div>
                    </div>

                    <div className="mt-3 h-2 w-full rounded-full bg-black/10">
                      <div
                        className="h-2 rounded-full bg-black"
                        style={{ width: `${clamp(j.fitScore)}%` }}
                      />
                    </div>

                    <button
                      className="mt-3 text-sm font-medium text-blue-600 hover:underline"
                      type="button"
                      onClick={() => router.push(`/jobs/${j.id}`)}
                    >
                      View details →
                    </button>
                  </div>
                ))}
              </div>
            )}
          </Section>

          {/* Applications */}
          <Section
            title="Applications"
            rightAction={
              applications.length > 0 ? (
                <button
                  className="text-sm text-blue-600 hover:underline"
                  onClick={() => router.push('/applications')}
                  type="button"
                >
                  View all
                </button>
              ) : null
            }
          >
            {loading ? (
              <div className="grid gap-4 sm:grid-cols-2">
                <SkeletonCard />
                <SkeletonCard />
              </div>
            ) : applications.length === 0 ? (
              <EmptyState
                title="No applications tracked yet"
                description="Track status changes so you always know what’s next."
                ctaLabel="Add application"
                onCta={() => router.push('/applications/new')}
              />
            ) : (
              <div className="grid gap-4 sm:grid-cols-2">
                {applications.map((a) => (
                  <div
                    key={a.id}
                    className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-black/5"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="font-medium text-gray-900">
                          {a.company} – {a.role}
                        </div>
                        <div className="mt-1 text-sm text-gray-500">
                          Status: {a.status}
                        </div>
                      </div>
                      <div className="text-xs text-gray-400">
                        {formatDate(a.updatedAt)}
                      </div>
                    </div>

                    <button
                      className="mt-3 text-sm font-medium text-blue-600 hover:underline"
                      type="button"
                      onClick={() => router.push(`/applications/${a.id}`)}
                    >
                      Open application →
                    </button>
                  </div>
                ))}
              </div>
            )}
          </Section>

          {/* Preparation Plans */}
          <Section
            title="Preparation plans"
            rightAction={
              prepPlans.length > 0 ? (
                <button
                  className="text-sm text-blue-600 hover:underline"
                  onClick={() => router.push('/prep')}
                  type="button"
                >
                  View all
                </button>
              ) : null
            }
          >
            {loading ? (
              <div className="grid gap-4 sm:grid-cols-2">
                <SkeletonCard />
                <SkeletonCard />
              </div>
            ) : prepPlans.length === 0 ? (
              <EmptyState
                title="No preparation plans yet"
                description="Create a plan and track completion over time."
                ctaLabel="Create plan"
                onCta={() => router.push('/prep/new')}
              />
            ) : (
              <div className="grid gap-4 sm:grid-cols-2">
                {prepPlans.map((p) => (
                  <div
                    key={p.id}
                    className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-black/5"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="font-medium text-gray-900">
                          {p.title}
                        </div>
                        <div className="mt-1 text-sm text-gray-500">
                          Status: {p.status}
                        </div>
                      </div>
                      <div className="text-xs text-gray-400">
                        {formatDate(p.updatedAt)}
                      </div>
                    </div>

                    <ProgressBar value={p.completionPct} />

                    <div className="mt-2 text-xs text-gray-500">
                      {clamp(p.completionPct)}% complete
                    </div>

                    <button
                      className="mt-3 text-sm font-medium text-blue-600 hover:underline"
                      type="button"
                      onClick={() => router.push(`/prep/${p.id}`)}
                    >
                      Open plan →
                    </button>
                  </div>
                ))}
              </div>
            )}
          </Section>
        </div>

        {/* RIGHT: secondary */}
        <div className="space-y-6">
          {/* Profile overview */}
          <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-black/5">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-gray-900">
                Profile overview
              </h3>
              {!loading && profile?.summary?.domain && (
                <span className="rounded-full bg-black/5 px-2 py-1 text-xs text-gray-600">
                  {profile.summary.domain}
                </span>
              )}
            </div>

            <div className="mt-4 space-y-2 text-sm text-gray-600">
              {loading ? (
                <div className="space-y-2">
                  <SkeletonLine w="w-56" />
                  <SkeletonLine w="w-44" />
                  <SkeletonLine w="w-52" />
                </div>
              ) : (
                <>
                  <div>
                    <span className="text-gray-500">Experience:</span>{' '}
                    {experienceLine || '—'}
                  </div>
                  <div>
                    <span className="text-gray-500">Skills:</span>{' '}
                    {(profile?.summary?.skills ?? []).slice(0, 4).join(', ') ||
                      '—'}
                    {(profile?.summary?.skills?.length ?? 0) > 4
                      ? ` +${(profile?.summary?.skills?.length ?? 0) - 4}`
                      : ''}
                  </div>
                </>
              )}
            </div>
          </div>

          {/* Activity timeline */}
          <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-black/5">
            <h3 className="text-sm font-semibold text-gray-900">Activity</h3>

            <div className="mt-4">
              {loading ? (
                <div className="space-y-4">
                  <div className="flex gap-3">
                    <div className="h-7 w-7 animate-pulse rounded-full bg-black/10" />
                    <div className="flex-1 space-y-2">
                      <div className="h-4 w-40 animate-pulse rounded bg-black/10" />
                      <div className="h-4 w-56 animate-pulse rounded bg-black/10" />
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <div className="h-7 w-7 animate-pulse rounded-full bg-black/10" />
                    <div className="flex-1 space-y-2">
                      <div className="h-4 w-44 animate-pulse rounded bg-black/10" />
                      <div className="h-4 w-52 animate-pulse rounded bg-black/10" />
                    </div>
                  </div>
                </div>
              ) : activity.length === 0 ? (
                <div className="text-sm text-gray-500">
                  No activity yet. Analyse a job or start a prep plan to see
                  updates here.
                </div>
              ) : (
                <ol className="space-y-4">
                  {activity.map((e) => (
                    <li key={e.id} className="flex gap-3">
                      <div className="mt-0.5">
                        <TimelineDot type={e.type} />
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center justify-between gap-3">
                          <div className="truncate text-sm font-medium text-gray-900">
                            {e.title}
                          </div>
                          <div className="shrink-0 text-xs text-gray-400">
                            {formatDate(e.at)}
                          </div>
                        </div>
                        <div className="mt-1 truncate text-sm text-gray-600">
                          {e.meta}
                        </div>
                      </div>
                    </li>
                  ))}
                </ol>
              )}
            </div>
          </div>

          <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-black/5">
            <h3 className="text-sm font-semibold text-gray-900">
              Quick actions
            </h3>
            <div className="mt-4 space-y-2">
              <button
                type="button"
                onClick={() => router.push('/jobs/new')}
                className="w-full rounded-lg bg-black px-4 py-2 text-sm font-medium text-white hover:bg-gray-900"
              >
                Analyse a job
              </button>
              <button
                type="button"
                onClick={() => router.push('/applications/new')}
                className="w-full rounded-lg bg-black/5 px-4 py-2 text-sm font-medium text-gray-900 hover:bg-black/10"
              >
                Add application
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
