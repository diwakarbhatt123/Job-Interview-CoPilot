import { apiFetch } from '@/lib/api'

type HealthProps = {
  status: 'ok' | 'error'
  message: string
}

export default function HealthPage({ status, message }: HealthProps) {
  return (
    <div>
      <h2>Gateway Health Check</h2>

      <p>
        Status:{' '}
        <strong style={{ color: status === 'ok' ? 'green' : 'red' }}>
          {status.toUpperCase()}
        </strong>
      </p>

      <pre>{message}</pre>
    </div>
  )
}

export async function getServerSideProps() {
  try {
    const result = await apiFetch<string>('/healthz')

    return {
      props: {
        status: 'ok',
        message: result,
      },
    }
  } catch (err) {
    return {
      props: {
        status: 'error',
        message:
          err instanceof Error
            ? err.message
            : 'Unknown error contacting gateway',
      },
    }
  }
}
