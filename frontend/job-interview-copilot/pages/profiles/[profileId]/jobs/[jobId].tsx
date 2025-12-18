import { useRouter } from "next/router";

export default function JobDetails() {
  const router = useRouter();
  return <div>Profile {router.query.profileId} Job {router.query.jobId} Details Page</div>;
}
