import { useRouter } from 'next/router'

export default function ApplicationDetail() {
  const router = useRouter()
  return (
    <div>
      Profile {router.query.profileId} Application {router.query.applicationId}{' '}
      Detail Page
    </div>
  )
}
