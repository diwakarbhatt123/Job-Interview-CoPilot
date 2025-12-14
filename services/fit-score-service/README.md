# Fit Score Service

Status: Skeleton (Milestone 0.1). No implementation yet.

Purpose: Compute role/candidate fit scores using rule-based signals first; LLM enrichment is optional and gated.

Responsibilities
- Ingest candidate profile signals and job attributes (from profile-service and job-analyzer-service).
- Apply deterministic scoring rules and weighting (skills overlap, must-haves, seniority, domain/stack).
- Serve fit scores and breakdowns via APIs exposed through the gateway.

Non-Goals
- Owning candidate profile data (relies on profile-service).
- Owning job description parsing (relies on job-analyzer-service).
- Shared database with other services.

Interfaces (planned)
- REST APIs via the gateway for requesting scores.
- Optional async events for score updates (TBD).

Data Store (MVP)
- None required; stateless computation (caching optional later).

Tech Stack
- Java + Spring Boot

Build/Test/Run (when implemented)
- Build: `./gradlew :services:fit-score-service:build`
- Test: `./gradlew :services:fit-score-service:test`
- Run: `./gradlew :services:fit-score-service:bootRun`

Configuration (placeholder)
- Env/secrets: Feature flags for any optional LLM enrichment.
- Depends on gateway routing and upstream services for data inputs.
