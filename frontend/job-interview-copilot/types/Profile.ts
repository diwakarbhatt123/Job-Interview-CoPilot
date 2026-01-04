export type Profile = {
  id: string
  displayName: string
  summary?: {
    yearsOfExperience?: number
    experienceLevel?: string
    skills?: string[]
    domain?: string
  }
}
