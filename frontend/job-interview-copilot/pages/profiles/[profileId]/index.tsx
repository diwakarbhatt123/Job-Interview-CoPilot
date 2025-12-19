import { useRouter } from 'next/router'

export default function ProfileDashboard() {
  const router = useRouter()
  return <div>Profile {router.query.profileId} Dashboard Page</div>
}
